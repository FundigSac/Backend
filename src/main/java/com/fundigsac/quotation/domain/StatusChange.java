package com.fundigsac.quotation.domain;

import java.time.Instant;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

/** Registro del historial de atención (solo inserción). */
public record StatusChange(UUID id, @Nullable QuoteStatus from, QuoteStatus to, @Nullable String note,
		@Nullable UUID actorId, Instant occurredAt) {
}
