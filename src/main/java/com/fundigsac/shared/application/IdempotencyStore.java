package com.fundigsac.shared.application;

import java.time.Instant;
import java.util.Optional;

import org.jspecify.annotations.Nullable;

/** Registro de claves Idempotency-Key ya usadas, con la referencia del recurso que crearon. */
public interface IdempotencyStore {

	record Entry(String fingerprint, @Nullable String resourceRef) {
	}

	/** Reserva la clave; false si ya existe una reserva vigente (espera a que la transacción que la tiene termine). */
	boolean reserve(String scope, String key, String fingerprint, Instant now, Instant expiresAt);

	Optional<Entry> find(String scope, String key);

	void complete(String scope, String key, String resourceRef);

	int purgeExpired(Instant now);

}
