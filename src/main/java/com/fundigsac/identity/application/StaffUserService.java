package com.fundigsac.identity.application;

import java.time.Clock;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fundigsac.identity.api.IdentityEvents.StaffInvited;
import com.fundigsac.identity.domain.AccountKind;
import com.fundigsac.identity.domain.AccountStatus;
import com.fundigsac.identity.domain.OneTimeToken;
import com.fundigsac.identity.domain.RefreshTokenRepository;
import com.fundigsac.identity.domain.Role;
import com.fundigsac.identity.domain.UserAccount;
import com.fundigsac.identity.domain.UserAccountRepository;
import com.fundigsac.shared.application.CurrentActor;
import com.fundigsac.shared.application.DomainEvents;
import com.fundigsac.shared.application.PageQuery;
import com.fundigsac.shared.application.PageResult;
import com.fundigsac.shared.domain.ConflictException;
import com.fundigsac.shared.domain.Email;
import com.fundigsac.shared.domain.Ids;
import com.fundigsac.shared.domain.NotFoundException;
import com.fundigsac.shared.domain.RuleViolationException;
import com.fundigsac.shared.domain.StaleVersionException;
import com.fundigsac.shared.security.Authz;

/**
 * Administración del personal: invitar, cambiar roles, desactivar y reactivar. Reglas: nadie se desactiva ni se quita
 * el rol de administrador a sí mismo, y siempre queda al menos un administrador activo.
 */
@Service
public class StaffUserService {

	private final UserAccountRepository accounts;

	private final RefreshTokenRepository refreshTokens;

	private final OneTimeTokens tokens;

	private final CurrentActor actor;

	private final DomainEvents events;

	private final IdentityProperties properties;

	private final Clock clock;

	StaffUserService(UserAccountRepository accounts, RefreshTokenRepository refreshTokens, OneTimeTokens tokens,
			CurrentActor actor, DomainEvents events, IdentityProperties properties, Clock clock) {
		this.accounts = accounts;
		this.refreshTokens = refreshTokens;
		this.tokens = tokens;
		this.actor = actor;
		this.events = events;
		this.properties = properties;
		this.clock = clock;
	}

	@PreAuthorize(Authz.USERS_MANAGE)
	@Transactional(readOnly = true)
	public PageResult<AccountView> list(@Nullable String query, PageQuery page) {
		String q = query == null || query.isBlank() ? null : query.strip();
		var items = this.accounts.findStaff(q, page.size(), page.offset()).stream().map(AccountView::of).toList();
		return PageResult.of(items, page, this.accounts.countStaff(q));
	}

	@PreAuthorize(Authz.USERS_MANAGE)
	@Transactional(readOnly = true)
	public AccountView get(UUID id) {
		return AccountView.of(staff(id));
	}

	/** Perfil propio del personal autenticado (cualquier rol interno). */
	@Transactional(readOnly = true)
	public AccountView self() {
		return AccountView.of(staff(this.actor.requireUserId()));
	}

	@PreAuthorize(Authz.USERS_MANAGE)
	@Transactional
	public AccountView invite(String rawEmail, String fullName, Set<Role> roles) {
		Instant now = this.clock.instant();
		Email email = Email.of("email", rawEmail);
		if (this.accounts.existsByEmail(email)) {
			throw new ConflictException("EMAIL_IN_USE", "Ya existe una cuenta con ese correo.");
		}
		UserAccount account = UserAccount.inviteStaff(Ids.newId(), email, fullName, roles, now);
		this.accounts.insert(account);
		OneTimeTokens.Issued issued = this.tokens.issue(account, OneTimeToken.Purpose.STAFF_INVITATION,
				this.properties.tokens().staffInvitationTtl(), now);
		this.events.publish(new StaffInvited(account.id(), email.value(), account.fullName(), issued.rawToken(),
				issued.expiresAt(), this.actor.userId().orElse(null), now));
		return AccountView.of(this.accounts.findById(account.id()).orElseThrow());
	}

	@PreAuthorize(Authz.USERS_MANAGE)
	@Transactional
	public AccountView changeRoles(UUID id, Set<Role> roles, long expectedVersion) {
		UUID actorId = this.actor.requireUserId();
		UserAccount account = staffForUpdate(id, expectedVersion);
		boolean losesAdmin = account.isAdministrator() && !roles.contains(Role.ADMINISTRATOR);
		if (losesAdmin && account.id().equals(actorId)) {
			throw new RuleViolationException("CANNOT_DEMOTE_SELF", "No puede quitarse su propio rol de administrador.");
		}
		if (losesAdmin && account.status() == AccountStatus.ACTIVE) {
			requireAnotherAdministrator();
		}
		account.changeRoles(roles.isEmpty() ? EnumSet.noneOf(Role.class) : EnumSet.copyOf(roles), actorId,
				this.clock.instant());
		return save(account);
	}

	@PreAuthorize(Authz.USERS_MANAGE)
	@Transactional
	public AccountView disable(UUID id, long expectedVersion) {
		UUID actorId = this.actor.requireUserId();
		UserAccount account = staffForUpdate(id, expectedVersion);
		if (account.id().equals(actorId)) {
			throw new RuleViolationException("CANNOT_DISABLE_SELF", "No puede desactivar su propia cuenta.");
		}
		if (account.isAdministrator() && account.status() == AccountStatus.ACTIVE) {
			requireAnotherAdministrator();
		}
		Instant now = this.clock.instant();
		account.disable(actorId, now);
		this.refreshTokens.revokeAllForUser(account.id(), now);
		return save(account);
	}

	@PreAuthorize(Authz.USERS_MANAGE)
	@Transactional
	public AccountView enable(UUID id, long expectedVersion) {
		UserAccount account = staffForUpdate(id, expectedVersion);
		account.enable(this.actor.requireUserId(), this.clock.instant());
		return save(account);
	}

	private AccountView save(UserAccount account) {
		this.accounts.update(account);
		this.events.publishFrom(account);
		return AccountView.of(this.accounts.findById(account.id()).orElseThrow());
	}

	private void requireAnotherAdministrator() {
		if (this.accounts.countActiveAdministrators() <= 1) {
			throw new RuleViolationException("LAST_ADMINISTRATOR", "Debe quedar al menos un administrador activo.");
		}
	}

	private UserAccount staffForUpdate(UUID id, long expectedVersion) {
		UserAccount account = staff(id);
		if (account.version() != expectedVersion) {
			throw new StaleVersionException();
		}
		return account;
	}

	private UserAccount staff(UUID id) {
		return this.accounts.findById(id)
			.filter((a) -> a.kind() == AccountKind.STAFF && a.status() != AccountStatus.DELETED)
			.orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "El usuario no existe."));
	}

}
