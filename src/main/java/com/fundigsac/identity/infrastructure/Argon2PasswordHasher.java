package com.fundigsac.identity.infrastructure;

import org.jspecify.annotations.Nullable;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Component;

import com.fundigsac.identity.domain.PasswordHasher;

/**
 * Argon2id con los parámetros mínimos recomendados por OWASP (m = 19 MiB, t = 2, p = 1), sal de 16 bytes y hash de 32.
 */
@Component
class Argon2PasswordHasher implements PasswordHasher {

	private final Argon2PasswordEncoder encoder = new Argon2PasswordEncoder(16, 32, 1, 19_456, 2);

	/** Hash de referencia para igualar el tiempo de respuesta cuando la cuenta no existe. */
	private final String dummyHash = this.encoder.encode("fundigsac-timing-equalizer-not-a-password");

	@Override
	public String hash(String rawPassword) {
		return this.encoder.encode(rawPassword);
	}

	@Override
	public boolean matches(String rawPassword, @Nullable String hash) {
		return hash != null && rawPassword != null && this.encoder.matches(rawPassword, hash);
	}

	@Override
	public void simulateVerification(String rawPassword) {
		this.encoder.matches(rawPassword == null ? "" : rawPassword, this.dummyHash);
	}

}
