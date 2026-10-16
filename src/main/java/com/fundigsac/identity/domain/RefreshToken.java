package com.fundigsac.identity.domain;

import java.time.Instant;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

/**
 * Sesión renovable. Rotación obligatoria: cada uso emite un token nuevo de la misma familia y revoca el anterior; si
 * un token ya revocado se vuelve a presentar (robo probable), se revoca la familia entera.
 */
public record RefreshToken(UUID id, UUID userId, UUID familyId, String tokenHash, Instant issuedAt, Instant expiresAt,
		@Nullable Instant revokedAt, @Nullable UUID replacedBy) {

	public boolean isExpired(Instant now) {
		return !this.expiresAt.isAfter(now);
	}

	public boolean isRevoked() {
		return this.revokedAt != null;
	}

}
