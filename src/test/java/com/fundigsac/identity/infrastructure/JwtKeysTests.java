package com.fundigsac.identity.infrastructure;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

import org.junit.jupiter.api.Test;
import org.mockito.Answers;

import com.fundigsac.identity.application.IdentityProperties;
import com.fundigsac.shared.infrastructure.SecretsProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Carga de la clave JWT tal como llega en producción: PEM PKCS#8, o error claro si falta o es débil. */
class JwtKeysTests {

	private static JwtKeys keys(String pem, boolean allowEphemeral) {
		IdentityProperties properties = mock(IdentityProperties.class, Answers.RETURNS_DEEP_STUBS);
		when(properties.jwt().privateKey()).thenReturn(pem);
		return new JwtKeys(properties, new SecretsProperties(null, allowEphemeral));
	}

	private static KeyPair rsa(int bits) throws Exception {
		KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
		generator.initialize(bits);
		return generator.generateKeyPair();
	}

	private static String pem(KeyPair pair) {
		return "-----BEGIN PRIVATE KEY-----\n" + Base64.getMimeEncoder().encodeToString(pair.getPrivate().getEncoded()) + "\n-----END PRIVATE KEY-----\n";
	}

	@Test
	void loadsThePemKeyAndDerivesItsPublicKey() throws Exception {
		KeyPair pair = rsa(2048);
		JwtKeys keys = keys(pem(pair), false);
		assertThat(keys.publicKey()).isEqualTo(pair.getPublic());
		assertThat(keys.rsaKey().isPrivate()).isTrue();
		assertThat(keys.rsaKey().getKeyID()).isNotBlank();
	}

	@Test
	void theSameKeyAlwaysGetsTheSameIdentifier() throws Exception {
		String pem = pem(rsa(2048));
		assertThat(keys(pem, false).rsaKey().getKeyID()).isEqualTo(keys(pem, true).rsaKey().getKeyID());
	}

	@Test
	void refusesToStartWithoutAKeyOutsideDevelopment() {
		for (String missing : new String[] { null, "  " }) {
			assertThatThrownBy(() -> keys(missing, false)).isInstanceOf(IllegalStateException.class).hasMessageContaining("jwt_private_key");
		}
	}

	@Test
	void generatesATemporaryKeyOnlyWhenAllowed() {
		assertThat(keys(null, true).rsaKey().size()).isEqualTo(3072);
		assertThat(keys("", true).publicKey()).isNotNull();
	}

	@Test
	void rejectsKeysShorterThan2048Bits() throws Exception {
		String weak = pem(rsa(1024));
		assertThatThrownBy(() -> keys(weak, false)).isInstanceOf(IllegalStateException.class).hasMessageContaining("2048");
	}

	@Test
	void rejectsContentThatIsNotAPrivateKey() {
		String notAKey = "-----BEGIN PRIVATE KEY-----\n" + Base64.getEncoder().encodeToString(new byte[64]) + "\n-----END PRIVATE KEY-----";
		assertThatThrownBy(() -> keys(notAKey, false)).isInstanceOf(IllegalStateException.class).hasMessageContaining("No se pudo cargar");
	}

}
