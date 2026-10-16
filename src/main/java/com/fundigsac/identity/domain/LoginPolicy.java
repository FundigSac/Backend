package com.fundigsac.identity.domain;

import java.time.Duration;

/**
 * Bloqueo temporal tras intentos fallidos consecutivos (protección contra fuerza bruta sin bloquear para siempre).
 *
 * @param maxFailedAttempts intentos fallidos que activan el bloqueo
 * @param lockDuration duración del bloqueo
 */
public record LoginPolicy(int maxFailedAttempts, Duration lockDuration) {

	public LoginPolicy {
		if (maxFailedAttempts < 1 || lockDuration.isNegative() || lockDuration.isZero()) {
			throw new IllegalArgumentException("Política de inicio de sesión inválida");
		}
	}

}
