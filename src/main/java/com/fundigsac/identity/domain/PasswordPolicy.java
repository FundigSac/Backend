package com.fundigsac.identity.domain;

import java.util.Locale;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import com.fundigsac.shared.domain.ValidationException;

/**
 * Política de contraseñas según NIST SP 800-63B / OWASP ASVS 5: longitud mínima 12 y máxima 128, cualquier carácter
 * (frases de paso), sin reglas de composición forzadas, pero rechaza contraseñas comunes o que contengan el correo.
 */
public final class PasswordPolicy {

	public static final int MIN_LENGTH = 12;

	public static final int MAX_LENGTH = 128;

	private static final Set<String> COMMON = Set.of("123456789012", "1234567890123", "contraseña123", "contrasena123",
			"password1234", "password12345", "qwertyuiop12", "fundigsac123", "fundigsac2026", "administrador",
			"administrador1", "iloveyou1234", "abcdefghijkl", "000000000000", "111111111111", "aaaaaaaaaaaa",
			"passwordpassword", "welcome12345", "bienvenido123", "peru12345678", "lima12345678");

	private PasswordPolicy() {
	}

	public static void check(String field, @Nullable String password, @Nullable String email) {
		if (password == null || password.isEmpty()) {
			throw new ValidationException(field, "REQUIRED", "Este campo es obligatorio.");
		}
		int length = password.codePointCount(0, password.length());
		if (length < MIN_LENGTH) {
			throw new ValidationException(field, "PASSWORD_TOO_SHORT",
					"La contraseña debe tener al menos " + MIN_LENGTH + " caracteres.");
		}
		if (length > MAX_LENGTH) {
			throw new ValidationException(field, "PASSWORD_TOO_LONG",
					"La contraseña debe tener como máximo " + MAX_LENGTH + " caracteres.");
		}
		String lower = password.toLowerCase(Locale.ROOT);
		if (COMMON.contains(lower) || lower.chars().distinct().count() < 4) {
			throw new ValidationException(field, "PASSWORD_TOO_COMMON", "Elija una contraseña menos predecible.");
		}
		if (email != null) {
			String local = email.toLowerCase(Locale.ROOT).split("@", 2)[0];
			if (local.length() >= 4 && lower.contains(local)) {
				throw new ValidationException(field, "PASSWORD_CONTAINS_EMAIL", "La contraseña no debe contener su correo.");
			}
		}
	}

}
