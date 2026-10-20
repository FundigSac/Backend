package com.fundigsac.identity.domain;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/** Puerto de persistencia de los códigos de acceso del personal. */
public interface StaffLoginCodeRepository {

	void insert(StaffLoginCode code);

	/** El código más reciente de la cuenta, se pueda usar o no. */
	Optional<StaffLoginCode> findLatest(UUID userId);

	/** Anula los códigos pendientes de la cuenta (al emitir uno nuevo). */
	void invalidatePending(UUID userId, Instant now);

	/** Descuenta un intento; devuelve los que quedan. */
	int consumeAttempt(UUID codeId);

	/** Marca el código como usado solo si seguía disponible (evita usarlo dos veces en paralelo). */
	boolean markUsed(UUID codeId, Instant now);

	int deleteExpired(Instant before);

}
