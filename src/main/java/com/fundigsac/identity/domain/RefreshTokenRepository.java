package com.fundigsac.identity.domain;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/** Puerto de persistencia de sesiones renovables. */
public interface RefreshTokenRepository {

	void insert(RefreshToken token);

	Optional<RefreshToken> findByHash(String tokenHash);

	/** Revoca un token vigente y lo enlaza con su reemplazo; false si ya estaba revocado (uso concurrente o robo). */
	boolean rotate(UUID id, UUID replacedBy, Instant now);

	void revokeFamily(UUID familyId, Instant now);

	void revokeAllForUser(UUID userId, Instant now);

	int purgeExpired(Instant before);

}
