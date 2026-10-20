package com.fundigsac.identity.domain;

import java.time.Instant;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

/**
 * Código de acceso de un solo uso para el personal (6 dígitos enviados al correo). Solo se guarda su hash; cada
 * intento fallido descuenta uno y al llegar a cero el código deja de servir.
 */
public record StaffLoginCode(UUID id, UUID userId, String codeHash, Instant createdAt, Instant expiresAt,
		int attemptsLeft, @Nullable Instant usedAt) {

	public boolean isUsable(Instant now) {
		return this.usedAt == null && this.attemptsLeft > 0 && this.expiresAt.isAfter(now);
	}

}
