package com.fundigsac.shared.web;

import org.jspecify.annotations.Nullable;

import com.fundigsac.shared.domain.ValidationException;

/**
 * Control de concurrencia HTTP: GET devuelve ETag con la versión del registro y las modificaciones del personal
 * exigen If-Match con esa versión (428 si falta, 412 si cambió).
 */
public final class ETags {

	private ETags() {
	}

	public static String of(long version) {
		return "\"" + version + "\"";
	}

	public static long requireVersion(@Nullable String ifMatch) {
		if (ifMatch == null || ifMatch.isBlank()) {
			throw new PreconditionRequiredException();
		}
		String value = ifMatch.strip();
		if (value.startsWith("W/")) {
			value = value.substring(2);
		}
		value = value.replace("\"", "");
		try {
			return Long.parseLong(value);
		}
		catch (NumberFormatException ex) {
			throw new ValidationException("If-Match", "INVALID_ETAG", "La cabecera If-Match no es válida.");
		}
	}

	/** Falta If-Match en una modificación (HTTP 428). */
	public static final class PreconditionRequiredException extends RuntimeException {

		PreconditionRequiredException() {
			super("If-Match required");
		}

	}

}
