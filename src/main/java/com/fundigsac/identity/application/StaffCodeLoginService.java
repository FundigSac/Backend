package com.fundigsac.identity.application;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fundigsac.identity.api.IdentityEvents.SignedIn;
import com.fundigsac.identity.api.IdentityEvents.StaffLoginCodeRequested;
import com.fundigsac.identity.domain.StaffLoginCode;
import com.fundigsac.identity.domain.StaffLoginCodeRepository;
import com.fundigsac.identity.domain.UserAccount;
import com.fundigsac.identity.domain.UserAccountRepository;
import com.fundigsac.shared.application.DomainEvents;
import com.fundigsac.shared.domain.Email;
import com.fundigsac.shared.domain.Ids;
import com.fundigsac.shared.domain.SecureTokens;
import com.fundigsac.shared.domain.UnauthorizedException;

/**
 * Acceso del personal sin contraseña: un código de 6 dígitos llega al correo, vence a los 10 minutos, sirve una vez y
 * admite 5 intentos. Pedir otro exige esperar 60 segundos. Las respuestas son iguales exista o no la cuenta: no se
 * revela quién es del personal ni si un código venció o se agotó (el panel lleva su propio conteo).
 */
@Service
public class StaffCodeLoginService {

	/** Lo que el panel necesita para mostrar el temporizador; idéntico para cualquier correo. */
	public record CodeIssued(long expiresInSeconds, long resendAfterSeconds) {
	}

	private static final SecureRandom RANDOM = new SecureRandom();

	private final UserAccountRepository accounts;

	private final StaffLoginCodeRepository codes;

	private final SessionIssuer sessions;

	private final DomainEvents events;

	private final IdentityProperties properties;

	private final Clock clock;

	StaffCodeLoginService(UserAccountRepository accounts, StaffLoginCodeRepository codes, SessionIssuer sessions,
			DomainEvents events, IdentityProperties properties, Clock clock) {
		this.accounts = accounts;
		this.codes = codes;
		this.sessions = sessions;
		this.events = events;
		this.properties = properties;
		this.clock = clock;
	}

	@Transactional
	public CodeIssued requestCode(@Nullable String rawEmail) {
		Instant now = this.clock.instant();
		IdentityProperties.Tokens config = this.properties.tokens();
		CodeIssued issued = new CodeIssued(config.staffLoginCodeTtl().toSeconds(), config.staffLoginCodeResend().toSeconds());
		Optional<UserAccount> found = parse(rawEmail).flatMap(this.accounts::findByEmail)
			.filter((account) -> account.canRequestLoginCode(now));
		if (found.isEmpty()) {
			return issued;
		}
		UserAccount account = found.get();
		boolean tooSoon = this.codes.findLatest(account.id())
			.filter((latest) -> latest.isUsable(now))
			.map((latest) -> latest.createdAt().plus(config.staffLoginCodeResend()).isAfter(now))
			.orElse(false);
		if (tooSoon) {
			return issued;
		}
		this.codes.invalidatePending(account.id(), now);
		String code = "%06d".formatted(RANDOM.nextInt(1_000_000));
		Instant expiresAt = now.plus(config.staffLoginCodeTtl());
		this.codes.insert(new StaffLoginCode(Ids.newId(), account.id(), hash(account, code), now, expiresAt,
				config.staffLoginCodeAttempts(), null));
		this.events.publish(new StaffLoginCodeRequested(account.id(), account.email().value(), account.fullName(), code,
				expiresAt, now));
		return issued;
	}

	@Transactional(noRollbackFor = UnauthorizedException.class)
	public AuthTokens verify(@Nullable String rawEmail, @Nullable String rawCode) {
		Instant now = this.clock.instant();
		String code = rawCode == null ? "" : rawCode.strip();
		UserAccount account = parse(rawEmail).flatMap(this.accounts::findByEmail)
			.filter((a) -> a.canRequestLoginCode(now))
			.orElseThrow(StaffCodeLoginService::invalidCode);
		StaffLoginCode current = this.codes.findLatest(account.id())
			.filter((c) -> c.isUsable(now))
			.orElseThrow(StaffCodeLoginService::invalidCode);
		if (!code.matches("\\d{6}") || !SecureTokens.constantTimeEquals(current.codeHash(), hash(account, code))) {
			this.codes.consumeAttempt(current.id());
			throw invalidCode();
		}
		if (!this.codes.markUsed(current.id(), now)) {
			throw invalidCode();
		}
		account.signInWithLoginCode(now);
		this.accounts.update(account);
		this.events.publishFrom(account);
		this.events.publish(new SignedIn(account.id(), "STAFF_CODE", now));
		return this.sessions.issue(account, Ids.newId(), Ids.newId(), now);
	}

	private static String hash(UserAccount account, String code) {
		return SecureTokens.sha256(account.id() + ":" + code);
	}

	private static Optional<Email> parse(@Nullable String rawEmail) {
		try {
			return Optional.of(new Email(rawEmail));
		}
		catch (RuntimeException ex) {
			return Optional.empty();
		}
	}

	private static UnauthorizedException invalidCode() {
		return new UnauthorizedException("INVALID_CODE", "El código no es válido o ya venció.");
	}

}
