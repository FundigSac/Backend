package com.fundigsac.identity.domain;

import org.jspecify.annotations.Nullable;

/** Puerto de hash de contraseñas (Argon2id en infraestructura). */
public interface PasswordHasher {

	String hash(String rawPassword);

	boolean matches(String rawPassword, @Nullable String hash);

	/**
	 * Verificación contra un hash ficticio cuando la cuenta no existe: el tiempo de respuesta no revela si el correo
	 * está registrado.
	 */
	void simulateVerification(String rawPassword);

}
