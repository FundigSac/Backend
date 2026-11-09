package com.fundigsac.identity.infrastructure;

import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.fundigsac.identity.domain.PasswordHasher;

/**
 * Argon2id con los parámetros mínimos recomendados por OWASP (m = 19 MiB, t = 2, p = 1), sal de 16 bytes y hash de 32.
 */
@Component
class Argon2PasswordHasher implements PasswordHasher {

	private final PasswordEncoder encoder;

	/** Hash de referencia para igualar el tiempo de respuesta cuando la cuenta no existe o no tiene contraseña. */
	private final String dummyHash;

	@Autowired
	Argon2PasswordHasher() {
		this(new Argon2PasswordEncoder(16, 32, 1, 19_456, 2));
	}

	Argon2PasswordHasher(PasswordEncoder encoder) {
		this.encoder = encoder;
		this.dummyHash = encoder.encode("fundigsac-timing-equalizer-not-a-password");
	}

	@Override
	public String hash(String rawPassword) {
		return this.encoder.encode(rawPassword);
	}

	/**
	 * Una cuenta sin contraseña (personal, cuentas solo Google) cuesta lo mismo que una con contraseña: sin el cálculo de
	 * Argon2 respondería antes y revelaría que el correo existe.
	 */
	@Override
	public boolean matches(String rawPassword, @Nullable String hash) {
		if (hash == null || rawPassword == null) {
			simulateVerification(rawPassword);
			return false;
		}
		return this.encoder.matches(rawPassword, hash);
	}

	@Override
	public void simulateVerification(String rawPassword) {
		this.encoder.matches(rawPassword == null ? "" : rawPassword, this.dummyHash);
	}

}
