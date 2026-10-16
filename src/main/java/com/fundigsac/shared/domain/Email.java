package com.fundigsac.shared.domain;

import java.util.Locale;
import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;

/** Correo electrónico normalizado (minúsculas, sin espacios) y con forma válida. */
public record Email(String value) {

	private static final Pattern FORMAT = Pattern.compile(
			"^[a-z0-9.!#$%&*+/=?^_{|}~-]+@[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?(?:\\.[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?)*\\.[a-z]{2,}$");

	public Email {
		value = value == null ? "" : value.strip().toLowerCase(Locale.ROOT);
		int at = value.indexOf('@');
		if (value.length() > 254 || at < 1 || at > 64 || !FORMAT.matcher(value).matches()) {
			throw new ValidationException("email", "INVALID_EMAIL", "Ingrese un correo electrónico válido.");
		}
	}

	public static Email of(String field, @Nullable String value) {
		if (value == null || value.isBlank()) {
			throw new ValidationException(field, "REQUIRED", "Este campo es obligatorio.");
		}
		try {
			return new Email(value);
		}
		catch (ValidationException ex) {
			throw new ValidationException(field, "INVALID_EMAIL", "Ingrese un correo electrónico válido.");
		}
	}

	/** Versión enmascarada para constancias y registros: c***@empresa.pe. */
	public String masked() {
		int at = this.value.indexOf('@');
		return this.value.charAt(0) + "***" + this.value.substring(at);
	}

	@Override
	public String toString() {
		return masked();
	}

}
