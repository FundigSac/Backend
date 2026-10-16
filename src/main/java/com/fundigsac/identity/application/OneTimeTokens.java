package com.fundigsac.identity.application;

import java.time.Duration;
import java.time.Instant;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import com.fundigsac.identity.domain.OneTimeToken;
import com.fundigsac.identity.domain.OneTimeTokenRepository;
import com.fundigsac.identity.domain.UserAccount;
import com.fundigsac.shared.domain.Ids;
import com.fundigsac.shared.domain.SecureTokens;
import com.fundigsac.shared.domain.ValidationException;

/** Emite y canjea tokens de un solo uso (verificación, recuperación, invitación). */
@Component
class OneTimeTokens {

	record Issued(String rawToken, Instant expiresAt) {
	}

	private final OneTimeTokenRepository repository;

	OneTimeTokens(OneTimeTokenRepository repository) {
		this.repository = repository;
	}

	/** Invalida los pendientes del mismo propósito y emite uno nuevo. */
	Issued issue(UserAccount account, OneTimeToken.Purpose purpose, Duration ttl, Instant now) {
		this.repository.invalidatePending(account.id(), purpose, now);
		String raw = SecureTokens.random();
		Instant expiresAt = now.plus(ttl);
		this.repository.insert(new OneTimeToken(Ids.newId(), account.id(), purpose, SecureTokens.sha256(raw), now,
				expiresAt, null));
		return new Issued(raw, expiresAt);
	}

	/** Canjea el token o lanza un error genérico (no revela si existió, venció o ya se usó). */
	OneTimeToken redeem(@Nullable String rawToken, OneTimeToken.Purpose purpose, Instant now) {
		if (rawToken == null || rawToken.isBlank() || rawToken.length() > 128) {
			throw invalid();
		}
		OneTimeToken token = this.repository.findByHash(SecureTokens.sha256(rawToken.strip()))
			.filter((t) -> t.isUsable(purpose, now))
			.orElseThrow(OneTimeTokens::invalid);
		if (!this.repository.markUsed(token.id(), now)) {
			throw invalid();
		}
		return token;
	}

	private static ValidationException invalid() {
		return new ValidationException("token", "INVALID_TOKEN", "El enlace no es válido o ya expiró. Solicite uno nuevo.");
	}

}
