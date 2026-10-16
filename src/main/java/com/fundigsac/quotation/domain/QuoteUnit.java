package com.fundigsac.quotation.domain;

import java.util.Arrays;
import java.util.Locale;

import com.fundigsac.shared.domain.ValidationException;

/** Unidades aceptadas (las del formulario del sitio: und, juego, lote; y metrado para tuberías). */
public enum QuoteUnit {

	UNIT("und", "Unidades"), SET("juego", "Juego completo"), LOT("lote", "Lote"), METER("m", "Metros lineales"),
	KILOGRAM("kg", "Kilogramos");

	private final String code;

	private final String label;

	QuoteUnit(String code, String label) {
		this.code = code;
		this.label = label;
	}

	public String code() {
		return this.code;
	}

	public String label() {
		return this.label;
	}

	/** Acepta el nombre (UNIT) o el código del formulario (und). */
	public static QuoteUnit parse(String value) {
		String v = value == null ? "" : value.strip();
		return Arrays.stream(values())
			.filter((u) -> u.name().equalsIgnoreCase(v) || u.code.equals(v.toLowerCase(Locale.ROOT)))
			.findFirst()
			.orElseThrow(() -> new ValidationException("items.unit", "INVALID_UNIT", "Unidad no válida (und, juego, lote, m o kg)."));
	}

}
