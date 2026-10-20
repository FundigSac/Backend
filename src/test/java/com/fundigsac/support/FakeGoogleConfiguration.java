package com.fundigsac.support;

import java.util.Optional;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import com.fundigsac.identity.domain.GoogleIdTokenVerifier;

/**
 * Google de prueba: acepta tokens "google-test|sub|correo|nombre" (como si Google los hubiera firmado) y rechaza
 * cualquier otro. Así las pruebas no dependen de Internet ni de un cliente OAuth real.
 */
@TestConfiguration(proxyBeanMethods = false)
public class FakeGoogleConfiguration {

	public static String idToken(String subject, String email, String name) {
		return "google-test|" + subject + "|" + email + "|" + name;
	}

	@Bean
	@Primary
	GoogleIdTokenVerifier fakeGoogleIdTokenVerifier() {
		return new GoogleIdTokenVerifier() {

			@Override
			public boolean isEnabled() {
				return true;
			}

			@Override
			public Optional<GoogleIdentity> verify(String idToken) {
				String[] parts = idToken.split("\\|");
				if (parts.length != 4 || !"google-test".equals(parts[0])) {
					return Optional.empty();
				}
				return Optional.of(new GoogleIdentity(parts[1], parts[2], parts[3]));
			}

		};
	}

}
