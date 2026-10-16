package com.fundigsac.catalog.domain;

import com.fundigsac.shared.domain.Guard;

/** Especificación mostrada en la ficha (etiqueta y valor), en el orden de la ficha. */
public record TechnicalSpec(String label, String value) {

	public TechnicalSpec {
		label = Guard.text("specs.label", label, 1, 80);
		value = Guard.text("specs.value", value, 1, 300);
	}

}
