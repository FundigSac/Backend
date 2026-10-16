package com.fundigsac.shared.domain;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;

/** Identificador de URL en kebab-case (a-z, 0-9 y guiones), como /productos/valvulas/valvula-compuerta. */
public record Slug(String value) {

	private static final Pattern FORMAT = Pattern.compile("^[a-z0-9]+(?:-[a-z0-9]+)*$");

	public Slug {
		if (value == null || value.length() > 120 || !FORMAT.matcher(value).matches()) {
			throw new ValidationException("slug", "INVALID_SLUG", "Use solo minúsculas, números y guiones (máx. 120).");
		}
	}

	public static Slug of(String field, @Nullable String value) {
		try {
			return new Slug(value == null ? "" : value.strip());
		}
		catch (ValidationException ex) {
			throw new ValidationException(field, "INVALID_SLUG", "Use solo minúsculas, números y guiones (máx. 120).");
		}
	}

	/** Texto sin tildes ni mayúsculas, útil para búsquedas: "Válvula Ñ" → "valvula n". */
	public static String fold(@Nullable String text) {
		if (text == null) {
			return "";
		}
		String noMarks = Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
		return noMarks.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ").strip();
	}

	@Override
	public String toString() {
		return this.value;
	}

}
