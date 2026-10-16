package com.fundigsac.notification.domain;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

/** Puerto de la bandeja de salida. */
public interface EmailOutboxRepository {

	/** Inserta si no existe otro con la misma clave de deduplicación; false si ya estaba. */
	boolean enqueue(OutgoingEmail email);

	/** Reserva correos pendientes vencidos (FOR UPDATE SKIP LOCKED: seguro con varias instancias). */
	List<OutgoingEmail> claimDue(Instant now, int limit);

	void update(OutgoingEmail email);

	Optional<OutgoingEmail> findById(UUID id);

	List<OutgoingEmail> search(@Nullable String status, int limit, long offset);

	long count(@Nullable String status);

}
