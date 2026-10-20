package com.fundigsac.identity.application;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fundigsac.identity.api.IdentityEvents.PasswordResetRequested;
import com.fundigsac.identity.api.IdentityEvents.VerificationEmailRequested;
import com.fundigsac.identity.domain.AccountKind;
import com.fundigsac.identity.domain.AccountStatus;
import com.fundigsac.identity.domain.OneTimeToken;
import com.fundigsac.identity.domain.RefreshTokenRepository;
import com.fundigsac.identity.domain.UserAccount;
import com.fundigsac.identity.domain.UserAccountRepository;
import com.fundigsac.shared.application.CurrentActor;
import com.fundigsac.shared.application.DomainEvents;
import com.fundigsac.shared.domain.NotFoundException;
import com.fundigsac.shared.domain.PhoneNumber;
import com.fundigsac.shared.domain.Ruc;
import com.fundigsac.shared.domain.RuleViolationException;
import com.fundigsac.shared.domain.StaleVersionException;
import com.fundigsac.shared.security.Authz;

/**
 * Administración de cualquier cuenta (clientes y personal) desde el panel interno, con permiso users:manage: corregir
 * datos, desactivar y reactivar, reenviar la verificación, enviar el enlace de contraseña y anonimizar la cuenta al
 * atender una solicitud de supresión. Mismas reglas que con el personal: nadie se desactiva ni se elimina a sí mismo
 * y siempre queda un administrador activo. Todo exige la versión leída (If-Match).
 */
@Service
public class AccountAdministrationService {

	public record ProfileCorrection(String fullName, @Nullable String companyName, @Nullable String ruc,
			@Nullable String phone) {
	}

	private final UserAccountRepository accounts;

	private final RefreshTokenRepository refreshTokens;

	private final OneTimeTokens tokens;

	private final CurrentActor actor;

	private final DomainEvents events;

	private final IdentityProperties properties;

	private final Clock clock;

	AccountAdministrationService(UserAccountRepository accounts, RefreshTokenRepository refreshTokens,
			OneTimeTokens tokens, CurrentActor actor, DomainEvents events, IdentityProperties properties, Clock clock) {
		this.accounts = accounts;
		this.refreshTokens = refreshTokens;
		this.tokens = tokens;
		this.actor = actor;
		this.events = events;
		this.properties = properties;
		this.clock = clock;
	}

	@PreAuthorize(Authz.USERS_MANAGE)
	@Transactional
	public AccountView correctProfile(UUID id, ProfileCorrection command, long expectedVersion) {
		UserAccount account = forUpdate(id, expectedVersion);
		account.updateProfileByStaff(command.fullName(), command.companyName(), Ruc.optional("ruc", command.ruc()),
				PhoneNumber.optional("phone", command.phone()), this.actor.requireUserId(), this.clock.instant());
		return save(account);
	}

	@PreAuthorize(Authz.USERS_MANAGE)
	@Transactional
	public AccountView disable(UUID id, long expectedVersion) {
		UUID actorId = this.actor.requireUserId();
		UserAccount account = forUpdate(id, expectedVersion);
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
		UserAccount account = forUpdate(id, expectedVersion);
		account.enable(this.actor.requireUserId(), this.clock.instant());
		return save(account);
	}

	/** Reenvía el enlace de verificación a un cliente que aún no confirmó su correo. */
	@PreAuthorize(Authz.USERS_MANAGE)
	@Transactional
	public void resendVerification(UUID id) {
		UserAccount account = live(id);
		if (account.kind() != AccountKind.CUSTOMER || account.status() != AccountStatus.PENDING_VERIFICATION) {
			throw new RuleViolationException("NOT_PENDING", "La cuenta no está pendiente de verificación.");
		}
		Instant now = this.clock.instant();
		OneTimeTokens.Issued issued = this.tokens.issue(account, OneTimeToken.Purpose.EMAIL_VERIFICATION,
				this.properties.tokens().emailVerificationTtl(), now);
		this.events.publish(new VerificationEmailRequested(account.id(), account.email().value(), account.fullName(),
				issued.rawToken(), issued.expiresAt(), now));
	}

	/** Envía al cliente el enlace para definir una contraseña nueva. El personal no usa contraseña: entra con código. */
	@PreAuthorize(Authz.USERS_MANAGE)
	@Transactional
	public void sendPasswordReset(UUID id) {
		UserAccount account = live(id);
		if (account.kind() == AccountKind.STAFF) {
			throw new RuleViolationException("STAFF_PASSWORDLESS", "El personal ingresa con un código, sin contraseña.");
		}
		if (account.status() == AccountStatus.DISABLED) {
			throw new RuleViolationException("ACCOUNT_DISABLED", "La cuenta está desactivada.");
		}
		Instant now = this.clock.instant();
		OneTimeTokens.Issued issued = this.tokens.issue(account, OneTimeToken.Purpose.PASSWORD_RESET,
				this.properties.tokens().passwordResetTtl(), now);
		this.events.publish(new PasswordResetRequested(account.id(), account.email().value(), account.fullName(),
				issued.rawToken(), issued.expiresAt(), now));
	}

	/** Anonimiza la cuenta y cierra sus sesiones (supresión de datos, Ley 29733). */
	@PreAuthorize(Authz.USERS_MANAGE)
	@Transactional
	public void delete(UUID id, long expectedVersion) {
		UUID actorId = this.actor.requireUserId();
		UserAccount account = forUpdate(id, expectedVersion);
		if (account.id().equals(actorId)) {
			throw new RuleViolationException("CANNOT_DELETE_SELF", "No puede eliminar su propia cuenta desde el panel.");
		}
		if (account.isAdministrator() && account.status() == AccountStatus.ACTIVE) {
			requireAnotherAdministrator();
		}
		Instant now = this.clock.instant();
		account.deleteByStaff(actorId, now);
		this.accounts.update(account);
		this.refreshTokens.revokeAllForUser(account.id(), now);
		this.events.publishFrom(account);
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

	private UserAccount forUpdate(UUID id, long expectedVersion) {
		UserAccount account = live(id);
		if (account.version() != expectedVersion) {
			throw new StaleVersionException();
		}
		return account;
	}

	private UserAccount live(UUID id) {
		return this.accounts.findById(id)
			.filter((a) -> a.status() != AccountStatus.DELETED)
			.orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "La cuenta no existe."));
	}

}
