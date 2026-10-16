package com.fundigsac.shared.domain;

import org.jspecify.annotations.Nullable;

/**
 * RUC peruano: 11 dígitos, prefijo de contribuyente válido (10, 15, 16, 17 o 20) y dígito verificador de SUNAT
 * (módulo 11 con pesos 5-4-3-2-7-6-5-4-3-2).
 */
public record Ruc(String value) {

	private static final int[] WEIGHTS = { 5, 4, 3, 2, 7, 6, 5, 4, 3, 2 };

	public Ruc {
		value = value == null ? "" : value.replaceAll("\\s", "");
		if (!isValid(value)) {
			throw new ValidationException("ruc", "INVALID_RUC", "El RUC no es válido (11 dígitos con dígito verificador).");
		}
	}

	public static Ruc of(String field, @Nullable String value) {
		if (value == null || value.isBlank()) {
			throw new ValidationException(field, "REQUIRED", "Este campo es obligatorio.");
		}
		try {
			return new Ruc(value);
		}
		catch (ValidationException ex) {
			throw new ValidationException(field, "INVALID_RUC", "El RUC no es válido (11 dígitos con dígito verificador).");
		}
	}

	public static @Nullable Ruc optional(String field, @Nullable String value) {
		return value == null || value.isBlank() ? null : of(field, value);
	}

	static boolean isValid(String digits) {
		if (!digits.matches("\\d{11}")) {
			return false;
		}
		String prefix = digits.substring(0, 2);
		if (!(prefix.equals("10") || prefix.equals("15") || prefix.equals("16") || prefix.equals("17") || prefix.equals("20"))) {
			return false;
		}
		int sum = 0;
		for (int i = 0; i < 10; i++) {
			sum += (digits.charAt(i) - '0') * WEIGHTS[i];
		}
		int check = 11 - (sum % 11);
		if (check == 10) {
			check = 0;
		}
		else if (check == 11) {
			check = 1;
		}
		return check == digits.charAt(10) - '0';
	}

	@Override
	public String toString() {
		return this.value;
	}

}
