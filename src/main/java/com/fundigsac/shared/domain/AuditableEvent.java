package com.fundigsac.shared.domain;

import java.util.Map;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

/** Evento que debe quedar en la auditoría (quién hizo qué, sobre qué recurso). Sin secretos ni datos de más. */
public interface AuditableEvent extends DomainEvent {

	String auditAction();

	String auditResourceType();

	String auditResourceId();

	/** Usuario que provocó el cambio; nulo cuando lo origina una persona anónima (formularios públicos). */
	@Nullable UUID actorId();

	default Map<String, String> auditDetails() {
		return Map.of();
	}

}
