package com.fundigsac.identity.infrastructure;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Una cuenta sin contraseña debe costar lo mismo que una con contraseña: de lo contrario se puede enumerar por tiempo. */
class Argon2PasswordHasherTests {

	private static final String DUMMY = "hash-de-referencia";

	private final PasswordEncoder encoder = encoder();

	private final Argon2PasswordHasher hasher = new Argon2PasswordHasher(this.encoder);

	private static PasswordEncoder encoder() {
		PasswordEncoder encoder = mock(PasswordEncoder.class);
		when(encoder.encode(anyString())).thenReturn(DUMMY);
		return encoder;
	}

	@Test
	void anAccountWithoutPasswordRunsTheSameHashCalculation() {
		assertThat(this.hasher.matches("cualquier-clave", null)).isFalse();
		verify(this.encoder).matches("cualquier-clave", DUMMY);
	}

	@Test
	void aMissingCandidateRunsTheCalculationWithAnEmptyValue() {
		assertThat(this.hasher.matches(null, "hash-guardado")).isFalse();
		verify(this.encoder).matches("", DUMMY);
		verify(this.encoder, never()).matches(any(), eq("hash-guardado"));
	}

	@Test
	void aRealHashIsVerifiedAgainstTheStoredValue() {
		when(this.encoder.matches("clave", "hash-guardado")).thenReturn(true);
		assertThat(this.hasher.matches("clave", "hash-guardado")).isTrue();
		assertThat(this.hasher.matches("otra", "hash-guardado")).isFalse();
	}

	@Test
	void realEncoderVerifiesHashesAndNeverAcceptsAMissingOne() {
		Argon2PasswordHasher real = new Argon2PasswordHasher();
		String hash = real.hash("frase-de-paso-muy-larga");
		assertThat(hash).startsWith("$argon2id$");
		assertThat(real.matches("frase-de-paso-muy-larga", hash)).isTrue();
		assertThat(real.matches("otra-frase-de-paso", hash)).isFalse();
		assertThat(real.matches("frase-de-paso-muy-larga", null)).isFalse();
	}

}
