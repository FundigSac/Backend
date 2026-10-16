package com.fundigsac.identity.application;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fundigsac.identity.domain.AccountStatus;
import com.fundigsac.identity.domain.LoginPolicy;
import com.fundigsac.identity.domain.PasswordHasher;
import com.fundigsac.identity.domain.RefreshToken;
import com.fundigsac.identity.domain.RefreshTokenRepository;
import com.fundigsac.identity.domain.UserAccount;
import com.fundigsac.identity.domain.UserAccountRepository;
import com.fundigsac.shared.application.DomainEvents;
import com.fundigsac.shared.domain.Email;
import com.fundigsac.shared.domain.ForbiddenException;
import com.fundigsac.shared.domain.Ids;
import com.fundigsac.shared.domain.SecureTokens;
import com.fundigsac.shared.domain.UnauthorizedException;

/**
 * Inicio de sesión, renovación con rotación y cierre de sesión. Los fallos dan siempre el mismo mensaje (no revelan si
 * el correo existe ni si la cuenta está bloqueada) y se confirman en la base de datos aunque la petición falle.
 */
@Service
public class AuthenticationService {

	private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);

	private final UserAccountRepository accounts;

	private final RefreshTokenRepository refreshTokens;

	private final PasswordHasher hasher;

	private final SessionIssuer sessions;

	private final DomainEvents events;

	private final LoginPolicy loginPolicy;

	private final Clock clock;

	AuthenticationService(UserAccountRepository accounts, RefreshTokenRepository refreshTokens, PasswordHasher hasher,
			SessionIssuer sessions, DomainEvents events, IdentityProperties properties, Clock clock) {
		this.accounts = accounts;
		this.refreshTokens = refreshTokens;
		this.hasher = hasher;
		this.sessions = sessions;
		this.events = events;
		this.loginPolicy = new LoginPolicy(properties.login().maxFailedAttempts(), properties.login().lockDuration());
		this.clock = clock;
	}

	@Transactional(noRollbackFor = UnauthorizedException.class)
	public AuthTokens login(@Nullable String rawEmail, String password) {
		Instant now = this.clock.instant();
		Optional<UserAccount> found = parse(rawEmail).flatMap(this.accounts::findByEmail);
		if (found.isEmpty()) {
			this.hasher.simulateVerification(password);
			throw invalidCredentials();
		}
		UserAccount account = found.get();
		boolean blocked = account.isLocked(now) || account.status() == AccountStatus.DISABLED
				|| account.status() == AccountStatus.DELETED;
		if (blocked) {
			this.hasher.simulateVerification(password);
			throw invalidCredentials();
		}
		if (!this.hasher.matches(password, account.passwordHash())) {
			account.recordFailedLogin(now, this.loginPolicy);
			this.accounts.update(account);
			this.events.publishFrom(account);
			throw invalidCredentials();
		}
		if (account.status() == AccountStatus.PENDING_VERIFICATION) {
			throw new ForbiddenException("EMAIL_NOT_VERIFIED", "Confirme su correo con el enlace que le enviamos para activar la cuenta.");
		}
		account.recordSuccessfulLogin(now);
		this.accounts.update(account);
		this.events.publishFrom(account);
		return this.sessions.issue(account, Ids.newId(), Ids.newId(), now);
	}

	@Transactional(noRollbackFor = UnauthorizedException.class)
	public AuthTokens refresh(@Nullable String rawRefreshToken) {
		Instant now = this.clock.instant();
		RefreshToken token = find(rawRefreshToken).orElseThrow(AuthenticationService::invalidSession);
		if (token.isRevoked()) {
			// Un token ya rotado se volvió a usar: posible robo. Se cierra toda la familia de sesiones.
			log.warn("Reutilización de refresh token detectada; se revoca la familia {}", token.familyId());
			this.refreshTokens.revokeFamily(token.familyId(), now);
			throw invalidSession();
		}
		if (token.isExpired(now)) {
			throw invalidSession();
		}
		UserAccount account = this.accounts.findById(token.userId())
			.filter((a) -> a.status() == AccountStatus.ACTIVE)
			.orElseThrow(AuthenticationService::invalidSession);
		var replacementId = Ids.newId();
		if (!this.refreshTokens.rotate(token.id(), replacementId, now)) {
			this.refreshTokens.revokeFamily(token.familyId(), now);
			throw invalidSession();
		}
		return this.sessions.issue(account, token.familyId(), replacementId, now);
	}

	/** Cierra la sesión (toda la familia del refresh token). Idempotente: no informa si el token existía. */
	@Transactional
	public void logout(@Nullable String rawRefreshToken) {
		find(rawRefreshToken).ifPresent((token) -> this.refreshTokens.revokeFamily(token.familyId(), this.clock.instant()));
	}

	private Optional<RefreshToken> find(@Nullable String raw) {
		if (raw == null || raw.isBlank() || raw.length() > 128) {
			return Optional.empty();
		}
		return this.refreshTokens.findByHash(SecureTokens.sha256(raw.strip()));
	}

	private static Optional<Email> parse(@Nullable String rawEmail) {
		try {
			return Optional.of(new Email(rawEmail));
		}
		catch (RuntimeException ex) {
			return Optional.empty();
		}
	}

	private static UnauthorizedException invalidCredentials() {
		return new UnauthorizedException("INVALID_CREDENTIALS", "Correo o contraseña incorrectos.");
	}

	private static UnauthorizedException invalidSession() {
		return new UnauthorizedException("INVALID_SESSION", "La sesión no es válida o expiró. Inicie sesión nuevamente.");
	}

}
