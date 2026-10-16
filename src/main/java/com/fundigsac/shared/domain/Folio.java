package com.fundigsac.shared.domain;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;

/**
 * Número correlativo visible para el cliente: PPP-AAAA-NNNNNN (p. ej. COT-2026-000042). Lo asigna la base de datos
 * dentro de la misma transacción que el registro, así no hay huecos ni folios de solicitudes que no se guardaron.
 */
public record Folio(String prefix, int year, int sequence) {

	private static final Pattern FORMAT = Pattern.compile("^([A-Z]{3})-(\\d{4})-(\\d{6})$");

	public Folio {
		if (prefix == null || !prefix.matches("[A-Z]{3}") || year < 2000 || year > 9999 || sequence < 1 || sequence > 999_999) {
			throw new IllegalArgumentException("Folio fuera de rango");
		}
	}

	public static Folio parse(@Nullable String value) {
		Matcher m = FORMAT.matcher(value == null ? "" : value.strip());
		if (!m.matches()) {
			throw new ValidationException("folio", "INVALID_FOLIO", "El número de registro no tiene un formato válido.");
		}
		return new Folio(m.group(1), Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)));
	}

	public String value() {
		return "%s-%04d-%06d".formatted(this.prefix, this.year, this.sequence);
	}

	@Override
	public String toString() {
		return value();
	}

}
