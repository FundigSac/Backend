package com.fundigsac.shared.domain;

import java.util.Collection;
import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;

/**
 * Comprobaciones de invariantes para constructores de agregados y valores. Normaliza el texto (quita caracteres de
 * control y espacios sobrantes) antes de validar longitudes, así lo guardado es exactamente lo validado.
 */
public final class Guard {

	private static final Pattern CONTROL = Pattern.compile("[\\p{Cc}&&[^\\n\\t]]");

	private static final Pattern SPACES = Pattern.compile("\\s+");

	private Guard() {
	}

	/** Texto de una línea obligatorio. */
	public static String text(String field, @Nullable String value, int min, int max) {
		String clean = singleLine(value);
		if (clean.isEmpty()) {
			throw new ValidationException(field, "REQUIRED", "Este campo es obligatorio.");
		}
		return length(field, clean, min, max);
	}

	/** Texto de una línea opcional: devuelve null si viene vacío. */
	public static @Nullable String optionalText(String field, @Nullable String value, int max) {
		String clean = singleLine(value);
		return clean.isEmpty() ? null : length(field, clean, 1, max);
	}

	/** Texto multilínea obligatorio (mensajes, descripciones). Conserva saltos de línea. */
	public static String multiline(String field, @Nullable String value, int min, int max) {
		String clean = multilineClean(value);
		if (clean.isEmpty()) {
			throw new ValidationException(field, "REQUIRED", "Este campo es obligatorio.");
		}
		return length(field, clean, min, max);
	}

	public static @Nullable String optionalMultiline(String field, @Nullable String value, int max) {
		String clean = multilineClean(value);
		return clean.isEmpty() ? null : length(field, clean, 1, max);
	}

	public static <T> T notNull(String field, @Nullable T value) {
		if (value == null) {
			throw new ValidationException(field, "REQUIRED", "Este campo es obligatorio.");
		}
		return value;
	}

	public static void isTrue(boolean condition, String field, String code, String message) {
		if (!condition) {
			throw new ValidationException(field, code, message);
		}
	}

	public static <C extends Collection<?>> C maxSize(String field, C values, int max) {
		if (values.size() > max) {
			throw new ValidationException(field, "TOO_MANY", "Se admiten como máximo " + max + " elementos.");
		}
		return values;
	}

	static String singleLine(@Nullable String value) {
		if (value == null) {
			return "";
		}
		return SPACES.matcher(CONTROL.matcher(value).replaceAll("")).replaceAll(" ").strip();
	}

	static String multilineClean(@Nullable String value) {
		if (value == null) {
			return "";
		}
		String noControl = CONTROL.matcher(value.replace("\r\n", "\n").replace('\r', '\n')).replaceAll("");
		return noControl.replaceAll("[ \\t]+\\n", "\n").replaceAll("\\n{3,}", "\n\n").strip();
	}

	private static String length(String field, String value, int min, int max) {
		int length = value.codePointCount(0, value.length());
		if (length < min) {
			throw new ValidationException(field, "TOO_SHORT", "Debe tener al menos " + min + " caracteres.");
		}
		if (length > max) {
			throw new ValidationException(field, "TOO_LONG", "Debe tener como máximo " + max + " caracteres.");
		}
		return value;
	}

}
