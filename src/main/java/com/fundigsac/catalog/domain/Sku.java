package com.fundigsac.catalog.domain;

import java.util.Locale;

import org.jspecify.annotations.Nullable;

import com.fundigsac.shared.domain.ValidationException;

/**
 * Código comercial. Se conservan guiones y ceros significativos; solo se normaliza a mayúsculas. Nunca se deriva del
 * nombre ni del slug (arquitectura §5.2).
 */
public record Sku(String value) {

	public Sku {
		value = value == null ? "" : value.strip().toUpperCase(Locale.ROOT);
		if (!value.matches("^[A-Z0-9][A-Z0-9.-]{1,39}$")) {
			throw new ValidationException("sku", "INVALID_SKU", "El código debe tener 2 a 40 letras, números, puntos o guiones.");
		}
	}

	public static @Nullable Sku optional(@Nullable String value) {
		return value == null || value.isBlank() ? null : new Sku(value);
	}

	@Override
	public String toString() {
		return this.value;
	}

}
