package com.fundigsac.identity.application;

import java.time.Clock;
import java.time.Duration;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fundigsac.identity.domain.RefreshTokenRepository;
import com.fundigsac.identity.domain.StaffLoginCodeRepository;

/**
 * Limpieza de sesiones vencidas hace más de 30 días (se conservan un tiempo para detectar reutilización) y de los
 * códigos de acceso del personal vencidos hace más de un día.
 */
@Component
class IdentityMaintenance {

	private final RefreshTokenRepository refreshTokens;

	private final StaffLoginCodeRepository loginCodes;

	private final Clock clock;

	IdentityMaintenance(RefreshTokenRepository refreshTokens, StaffLoginCodeRepository loginCodes, Clock clock) {
		this.refreshTokens = refreshTokens;
		this.loginCodes = loginCodes;
		this.clock = clock;
	}

	@Scheduled(initialDelayString = "PT5M", fixedDelayString = "PT6H")
	void purgeExpiredSessions() {
		this.refreshTokens.purgeExpired(this.clock.instant().minus(Duration.ofDays(30)));
		this.loginCodes.deleteExpired(this.clock.instant().minus(Duration.ofDays(1)));
	}

}
