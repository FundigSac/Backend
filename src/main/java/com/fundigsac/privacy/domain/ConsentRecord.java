package com.fundigsac.privacy.domain;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.fundigsac.shared.domain.ValidationException;

/**
 * Evidencia de una decisión sobre cookies: qué eligió, con qué versión de la política, desde dónde y cuándo. Solo el
 * identificador aleatorio de la cookie de consentimiento; nada de IP, agente de usuario ni identificadores de
 * seguimiento (minimización).
 */
public record ConsentRecord(UUID id, UUID consentId, Map<String, Boolean> choices, String policyVersion, Source source,
		Instant recordedAt) {

	/** Categorías que el sitio pide hoy: las técnicas (siempre activas) y el contenido de terceros (mapa). */
	public static final Set<String> CATEGORIES = Set.of("necessary", "thirdParty");

	public enum Source {
		BANNER, PREFERENCES, POLICY, EMBED
	}

	public ConsentRecord {
		if (choices == null || !choices.keySet().equals(CATEGORIES)) {
			throw new ValidationException("choices", "INVALID_CHOICES", "Indique exactamente las categorías necessary y thirdParty.");
		}
		if (!Boolean.TRUE.equals(choices.get("necessary"))) {
			throw new ValidationException("choices.necessary", "MUST_BE_TRUE", "Las cookies técnicas son siempre necesarias.");
		}
		choices = new LinkedHashMap<>(choices);
		if (policyVersion == null || !policyVersion.matches("^[0-9A-Za-z._-]{1,20}$")) {
			throw new ValidationException("policyVersion", "INVALID_VERSION", "Versión de política no válida.");
		}
	}

}
