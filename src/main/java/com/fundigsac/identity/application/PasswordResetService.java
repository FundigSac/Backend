package com.fundigsac.identity.application;

import java.time.Clock;
import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fundigsac.identity.api.IdentityEvents.PasswordResetRequested;
import com.fundigsac.identity.domain.AccountStatus;
import com.fundigsac.identity.domain.OneTimeToken;
import com.fundigsac.identity.domain.PasswordHasher;
import com.fundigsac.identity.domain.PasswordPolicy;
import com.fundigsac.identity.domain.RefreshTokenRepository;
import com.fundigsac.identity.domain.UserAccount;
import com.fundigsac.identity.domain.UserAccountRepository;
import com.fundigsac.shared.application.DomainEvents;
import com.fundigsac.shared.domain.Email;

/**
 * Recuperación de contraseña por correo (enlace de 30 minutos, un solo uso) y aceptación de invitaciones del
 * personal. Tras el cambio se cierran todas las sesiones abiertas.
 */
@Service
public class PasswordResetService {

	private final UserAccountRepository accounts;

	private final RefreshTokenRepository refreshTokens;

	private final PasswordHasher hasher;

	private final OneTimeTokens tokens;

	private final DomainEvents events;

	private final IdentityProperties properties;

	private final Clock clock;

	PasswordResetService(UserAccountRepository accounts, RefreshTokenRepository refreshTokens, PasswordHasher hasher,
			OneTimeTokens tokens, DomainEvents events, IdentityProperties properties, Clock clock) {
		this.accounts = accounts;
		this.refreshTokens = refreshTokens;
		this.hasher = hasher;
		this.tokens = tokens;
		this.events = events;
		this.properties = properties;
		this.clock = clock;
	}

	/** Siempre responde igual; solo envía el enlace si la cuenta existe y puede recuperarse. */
	@Transactional
	public void requestReset(String rawEmail) {
		Instant now = this.clock.instant();
		Email email;
		try {
			email = new Email(rawEmail);
		}
		catch (RuntimeException ex) {
			return;
		}
		this.accounts.findByEmail(email)
			.filter((a) -> a.status() == AccountStatus.ACTIVE
					|| (a.status() == AccountStatus.PENDING_VERIFICATION && a.passwordHash() != null))
			.ifPresent((account) -> {
				OneTimeTokens.Issued issued = this.tokens.issue(account, OneTimeToken.Purpose.PASSWORD_RESET,
						this.properties.tokens().passwordResetTtl(), now);
				this.events.publish(new PasswordResetRequested(account.id(), account.email().value(), account.fullName(),
						issued.rawToken(), issued.expiresAt(), now));
			});
	}

	@Transactional
	public void resetPassword(String rawToken, String newPassword) {
		Instant now = this.clock.instant();
		OneTimeToken token = this.tokens.redeem(rawToken, OneTimeToken.Purpose.PASSWORD_RESET, now);
		UserAccount account = this.accounts.findById(token.userId()).orElseThrow();
		PasswordPolicy.check("password", newPassword, account.email().value());
		account.resetPassword(this.hasher.hash(newPassword), now);
		this.accounts.update(account);
		this.refreshTokens.revokeAllForUser(account.id(), now);
		this.events.publishFrom(account);
	}

	@Transactional
	public void acceptInvitation(String rawToken, String newPassword) {
		Instant now = this.clock.instant();
		OneTimeToken token = this.tokens.redeem(rawToken, OneTimeToken.Purpose.STAFF_INVITATION, now);
		UserAccount account = this.accounts.findById(token.userId()).orElseThrow();
		PasswordPolicy.check("password", newPassword, account.email().value());
		account.acceptInvitation(this.hasher.hash(newPassword), now);
		this.accounts.update(account);
		this.events.publishFrom(account);
	}

}
