package com.fundigsac.identity.application;

import java.time.Clock;
import java.time.Instant;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fundigsac.identity.domain.GoogleIdTokenVerifier;
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

	private final GoogleIdTokenVerifier google;

	private final CurrentActor actor;

	private final DomainEvents events;

	private final Clock clock;

	AccountService(UserAccountRepository accounts, RefreshTokenRepository refreshTokens, PasswordHasher hasher,
			GoogleIdTokenVerifier google, CurrentActor actor, DomainEvents events, Clock clock) {
		this.accounts = accounts;
		this.refreshTokens = refreshTokens;
		this.hasher = hasher;
		this.google = google;
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

	/**
	 * Cambia la contraseña y cierra todas las sesiones (también invalida los JWT emitidos por el sello). Una cuenta solo
	 * Google define aquí su primera contraseña confirmando su identidad con un ID token de Google reciente.
	 */
	@Transactional
	public void changePassword(@Nullable String currentPassword, String newPassword, @Nullable String googleIdToken) {
		Instant now = this.clock.instant();
		UserAccount account = current();
		confirmIdentity(account, currentPassword, "currentPassword", googleIdToken);
		PasswordPolicy.check("newPassword", newPassword, account.email().value());
		account.changePassword(this.hasher.hash(newPassword), now);
		this.accounts.update(account);
		this.refreshTokens.revokeAllForUser(account.id(), now);
		this.events.publishFrom(account);
	}

	/** Eliminación de la cuenta a pedido del titular (confirmada con su contraseña o, si no tiene, con Google). */
	@Transactional
	public void deleteAccount(@Nullable String password, @Nullable String googleIdToken) {
		Instant now = this.clock.instant();
		UserAccount account = current();
		confirmIdentity(account, password, "password", googleIdToken);
		account.delete(now);
		this.accounts.update(account);
		this.refreshTokens.revokeAllForUser(account.id(), now);
		this.events.publishFrom(account);
	}

	/**
	 * Confirma que quien opera es el titular. Una cuenta creada con Google no tiene contraseña, así que la prueba es un ID
	 * token de Google del mismo sujeto vinculado a la cuenta; sin esto no podría ejercer su derecho de supresión.
	 */
	private void confirmIdentity(UserAccount account, @Nullable String password, String field,
			@Nullable String googleIdToken) {
		if (account.hasPassword()) {
			if (password == null || password.isBlank()) {
				throw new ValidationException(field, "REQUIRED", "Ingrese su contraseña.");
			}
			if (!this.hasher.matches(password, account.passwordHash())) {
				throw new ValidationException(field, "WRONG_PASSWORD", "La contraseña actual no es correcta.");
			}
			return;
		}
		String linked = account.signup().googleSubject();
		boolean confirmed = linked != null && googleIdToken != null && !googleIdToken.isBlank()
				&& this.google.verify(googleIdToken).filter((identity) -> linked.equals(identity.subject())).isPresent();
		if (!confirmed) {
			throw new ValidationException("googleIdToken", "GOOGLE_REAUTH_REQUIRED",
					"Confirme su identidad volviendo a ingresar con Google.");
		}
	}

	private UserAccount current() {
		return this.accounts.findById(this.actor.requireUserId())
			.orElseThrow(() -> new NotFoundException("ACCOUNT_NOT_FOUND", "La cuenta no existe."));
	}

}
