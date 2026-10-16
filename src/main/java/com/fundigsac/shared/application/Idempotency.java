package com.fundigsac.shared.application;

import java.time.Clock;
import java.time.Duration;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import com.fundigsac.shared.domain.ConflictException;
import com.fundigsac.shared.domain.ValidationException;

/**
 * Ejecuta una operación de creación una sola vez por clave Idempotency-Key. Debe usarse dentro de la transacción del
 * caso de uso: la reserva, el registro y la referencia se confirman juntos. Misma clave y mismo contenido: misma
 * respuesta (mismo folio). Misma clave con otro contenido: 409.
 */
@Component
public class Idempotency {

	private static final Pattern KEY = Pattern.compile("^[A-Za-z0-9_-]{8,128}$");

	private static final Duration TTL = Duration.ofHours(24);

	private final IdempotencyStore store;

	private final RequestFingerprints fingerprints;

	private final Clock clock;

	public Idempotency(IdempotencyStore store, RequestFingerprints fingerprints, Clock clock) {
		this.store = store;
		this.fingerprints = fingerprints;
		this.clock = clock;
	}

	public <R> R execute(String scope, @Nullable String key, Object payload, Supplier<R> action,
			Function<R, String> referenceOf, Function<String, R> replay) {
		if (key == null || key.isBlank()) {
			return action.get();
		}
		if (!KEY.matcher(key).matches()) {
			throw new ValidationException("Idempotency-Key", "INVALID_IDEMPOTENCY_KEY",
					"La clave de idempotencia debe tener entre 8 y 128 caracteres (letras, números, - o _).");
		}
		String fingerprint = this.fingerprints.of(payload);
		var now = this.clock.instant();
		if (!this.store.reserve(scope, key, fingerprint, now, now.plus(TTL))) {
			IdempotencyStore.Entry entry = this.store.find(scope, key)
				.orElseThrow(() -> new ConflictException("REQUEST_IN_PROGRESS", "La solicitud ya se está procesando."));
			if (!entry.fingerprint().equals(fingerprint)) {
				throw new ConflictException("IDEMPOTENCY_KEY_REUSED",
						"La clave de idempotencia ya se usó con otro contenido.");
			}
			if (entry.resourceRef() == null) {
				throw new ConflictException("REQUEST_IN_PROGRESS", "La solicitud ya se está procesando.");
			}
			return replay.apply(entry.resourceRef());
		}
		R result = action.get();
		this.store.complete(scope, key, referenceOf.apply(result));
		return result;
	}

}
