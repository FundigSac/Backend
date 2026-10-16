package com.fundigsac.shared.domain;

import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;

/** Teléfono con 7 a 15 dígitos (E.164), con + opcional; se guarda sin separadores. */
public record PhoneNumber(String value) {

	private static final Pattern ALLOWED = Pattern.compile("^\\+?[0-9 ().-]{7,24}$");

	private static final Pattern NORMALIZED = Pattern.compile("^\\+?[0-9]{7,15}$");

	public PhoneNumber {
		String raw = value == null ? "" : value.strip();
		if (!ALLOWED.matcher(raw).matches()) {
			throw new ValidationException("phone", "INVALID_PHONE", "Ingrese un teléfono válido.");
		}
		value = (raw.startsWith("+") ? "+" : "") + raw.replaceAll("[^0-9]", "");
		if (!NORMALIZED.matcher(value).matches()) {
			throw new ValidationException("phone", "INVALID_PHONE", "Ingrese un teléfono válido.");
		}
	}

	public static PhoneNumber of(String field, @Nullable String value) {
		if (value == null || value.isBlank()) {
			throw new ValidationException(field, "REQUIRED", "Este campo es obligatorio.");
		}
		try {
			return new PhoneNumber(value);
		}
		catch (ValidationException ex) {
			throw new ValidationException(field, "INVALID_PHONE", "Ingrese un teléfono válido.");
		}
	}

	public static @Nullable PhoneNumber optional(String field, @Nullable String value) {
		return value == null || value.isBlank() ? null : of(field, value);
	}

	@Override
	public String toString() {
		return this.value.length() <= 4 ? "****" : "***" + this.value.substring(this.value.length() - 3);
	}

}
