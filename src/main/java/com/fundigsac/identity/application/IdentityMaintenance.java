package com.fundigsac.identity.application;

import java.time.Clock;
import java.time.Duration;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fundigsac.identity.domain.RefreshTokenRepository;

/** Limpieza de sesiones vencidas hace más de 30 días (se conservan un tiempo para detectar reutilización). */
@Component
class IdentityMaintenance {

	private final RefreshTokenRepository refreshTokens;

	private final Clock clock;

	IdentityMaintenance(RefreshTokenRepository refreshTokens, Clock clock) {
		this.refreshTokens = refreshTokens;
		this.clock = clock;
	}

	@Scheduled(initialDelayString = "PT5M", fixedDelayString = "PT6H")
	void purgeExpiredSessions() {
		this.refreshTokens.purgeExpired(this.clock.instant().minus(Duration.ofDays(30)));
	}

}
