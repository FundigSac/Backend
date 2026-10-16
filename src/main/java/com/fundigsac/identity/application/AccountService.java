package com.fundigsac.identity.application;

import java.time.Clock;
import java.time.Instant;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fundigsac.identity.domain.PasswordHasher;
import com.fundigsac.identity.domain.PasswordPolicy;
import com.fundigsac.identity.domain.RefreshTokenRepository;
import com.fundigsac.identity.domain.UserAccount;
import com.fundigsac.identity.domain.UserAccountRepository;
import com.fundigsac.shared.application.CurrentActor;
import com.fundigsac.shared.application.DomainEvents;
import com.fundigsac.shared.domain.NotFoundException;
import com.fundigsac.shared.domain.PhoneNumber;
import com.fundigsac.shared.domain.Ruc;
import com.fundigsac.shared.domain.ValidationException;

/** Autoservicio de la cuenta propia: ver y editar perfil, cambiar contraseña y eliminar la cuenta. */
@Service
public class AccountService {

	public record UpdateProfile(String fullName, @Nullable String companyName, @Nullable String ruc,
			@Nullable String phone) {
	}

	private final UserAccountRepository accounts;

	private final RefreshTokenRepository refreshTokens;

	private final PasswordHasher hasher;

	private final CurrentActor actor;

	private final DomainEvents events;

	private final Clock clock;

	AccountService(UserAccountRepository accounts, RefreshTokenRepository refreshTokens, PasswordHasher hasher,
			CurrentActor actor, DomainEvents events, Clock clock) {
		this.accounts = accounts;
		this.refreshTokens = refreshTokens;
		this.hasher = hasher;
		this.actor = actor;
		this.events = events;
		this.clock = clock;
	}

	@Transactional(readOnly = true)
	public AccountView me() {
		return AccountView.of(current());
	}

	@Transactional
	public AccountView updateProfile(UpdateProfile command) {
		UserAccount account = current();
		account.updateProfile(command.fullName(), command.companyName(), Ruc.optional("ruc", command.ruc()),
				PhoneNumber.optional("phone", command.phone()), this.clock.instant());
		this.accounts.update(account);
		return AccountView.of(this.accounts.findById(account.id()).orElseThrow());
	}

	/** Cambia la contraseña y cierra todas las sesiones (también invalida los JWT emitidos por el sello). */
	@Transactional
	public void changePassword(String currentPassword, String newPassword) {
		Instant now = this.clock.instant();
		UserAccount account = current();
		requirePassword(account, currentPassword, "currentPassword");
		PasswordPolicy.check("newPassword", newPassword, account.email().value());
		account.changePassword(this.hasher.hash(newPassword), now);
		this.accounts.update(account);
		this.refreshTokens.revokeAllForUser(account.id(), now);
		this.events.publishFrom(account);
	}

	/** Eliminación de la cuenta a pedido del titular (confirmada con su contraseña). */
	@Transactional
	public void deleteAccount(String password) {
		Instant now = this.clock.instant();
		UserAccount account = current();
		requirePassword(account, password, "password");
		account.delete(now);
		this.accounts.update(account);
		this.refreshTokens.revokeAllForUser(account.id(), now);
		this.events.publishFrom(account);
	}

	private void requirePassword(UserAccount account, String password, String field) {
		if (!this.hasher.matches(password, account.passwordHash())) {
			throw new ValidationException(field, "WRONG_PASSWORD", "La contraseña actual no es correcta.");
		}
	}

	private UserAccount current() {
		return this.accounts.findById(this.actor.requireUserId())
			.orElseThrow(() -> new NotFoundException("ACCOUNT_NOT_FOUND", "La cuenta no existe."));
	}

}
