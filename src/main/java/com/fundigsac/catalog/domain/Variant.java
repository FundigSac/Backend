package com.fundigsac.catalog.domain;

import java.util.UUID;

import org.jspecify.annotations.Nullable;

import com.fundigsac.shared.domain.Guard;
import com.fundigsac.shared.domain.ValidationException;

/** Variante comercial (medida, presión o modelo) de un producto. */
public record Variant(UUID id, @Nullable Sku sku, String label, @Nullable Integer dnMm, @Nullable Integer pn) {

	public Variant {
		Guard.notNull("variants.id", id);
		label = Guard.text("variants.label", label, 1, 120);
		if (dnMm != null && (dnMm < 1 || dnMm > 4000)) {
			throw new ValidationException("variants.dnMm", "OUT_OF_RANGE", "El DN debe estar entre 1 y 4000 mm.");
		}
		if (pn != null && (pn < 1 || pn > 100)) {
			throw new ValidationException("variants.pn", "OUT_OF_RANGE", "El PN debe estar entre 1 y 100.");
		}
	}

}
