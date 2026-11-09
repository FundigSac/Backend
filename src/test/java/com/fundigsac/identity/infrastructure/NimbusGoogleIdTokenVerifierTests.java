package com.fundigsac.identity.infrastructure;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.function.Consumer;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import com.fundigsac.identity.application.IdentityProperties;
import com.fundigsac.identity.domain.GoogleIdTokenVerifier.GoogleIdentity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Validación real de los ID token de Google (README: emisor, audiencia, email_verified) con una clave local: el único
 * elemento sustituido es el origen de las claves públicas.
 */
class NimbusGoogleIdTokenVerifierTests {

	private static final String CLIENT = "cliente-web.apps.googleusercontent.com";

	private static KeyPair google;

	private static KeyPair attacker;

	private static NimbusGoogleIdTokenVerifier verifier;

	@BeforeAll
	static void keys() throws Exception {
		KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
		generator.initialize(2048);
		google = generator.generateKeyPair();
		attacker = generator.generateKeyPair();
		verifier = new NimbusGoogleIdTokenVerifier(NimbusJwtDecoder.withPublicKey((RSAPublicKey) google.getPublic()).build(), CLIENT);
	}

	private static JWTClaimsSet.Builder validClaims() {
		Instant now = Instant.now();
		return new JWTClaimsSet.Builder().issuer("https://accounts.google.com")
			.audience(CLIENT)
			.subject("g-123")
			.issueTime(Date.from(now.minusSeconds(10)))
			.expirationTime(Date.from(now.plusSeconds(600)))
			.claim("email", "maria@gmail.com")
			.claim("email_verified", true)
			.claim("name", "María Torres");
	}

	private static String sign(JWTClaimsSet claims, KeyPair keys) throws Exception {
		SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claims);
		jwt.sign(new RSASSASigner((RSAPrivateKey) keys.getPrivate()));
		return jwt.serialize();
	}

	private static String token(Consumer<JWTClaimsSet.Builder> change) throws Exception {
		JWTClaimsSet.Builder claims = validClaims();
		change.accept(claims);
		return sign(claims.build(), google);
	}

	@Test
	void acceptsAGenuineToken() throws Exception {
		assertThat(verifier.isEnabled()).isTrue();
		assertThat(verifier.verify(token((c) -> {
		}))).contains(new GoogleIdentity("g-123", "maria@gmail.com", "María Torres"));
	}

	@Test
	void acceptsBothGoogleIssuerSpellingsAndTextualEmailVerified() throws Exception {
		assertThat(verifier.verify(token((c) -> c.issuer("accounts.google.com").claim("email_verified", "true")))).isPresent();
	}

	@Test
	void rejectsAnotherIssuer() throws Exception {
		assertThat(verifier.verify(token((c) -> c.issuer("https://emisor-falso.example")))).isEmpty();
	}

	@Test
	void rejectsATokenIssuedForAnotherApplication() throws Exception {
		assertThat(verifier.verify(token((c) -> c.audience("otra-aplicacion.apps.googleusercontent.com")))).isEmpty();
	}

	@Test
	void rejectsAnExpiredTokenBeyondTheToleranceAndAcceptsOneWithinIt() throws Exception {
		assertThat(verifier.verify(token((c) -> c.expirationTime(Date.from(Instant.now().minusSeconds(120)))))).isEmpty();
		assertThat(verifier.verify(token((c) -> c.issueTime(Date.from(Instant.now().minusSeconds(600)))
			.expirationTime(Date.from(Instant.now().minusSeconds(20)))))).isPresent();
	}

	@Test
	void rejectsAnUnverifiedEmail() throws Exception {
		assertThat(verifier.verify(token((c) -> c.claim("email_verified", false)))).isEmpty();
		assertThat(verifier.verify(token((c) -> c.claim("email_verified", null)))).isEmpty();
	}

	@Test
	void rejectsAMissingEmailOrSubject() throws Exception {
		assertThat(verifier.verify(token((c) -> c.claim("email", null)))).isEmpty();
		assertThat(verifier.verify(token((c) -> c.subject(null)))).isEmpty();
	}

	@Test
	void rejectsASignatureFromAnotherKey() throws Exception {
		assertThat(verifier.verify(sign(validClaims().build(), attacker))).isEmpty();
	}

	@Test
	void rejectsGarbageWithoutCallingTheDecoder() {
		assertThat(verifier.verify(null)).isEmpty();
		assertThat(verifier.verify("   ")).isEmpty();
		assertThat(verifier.verify("x".repeat(4097))).isEmpty();
		assertThat(verifier.verify("esto.no.es-un-jwt")).isEmpty();
	}

	@Test
	void fallsBackFromNameToGivenNameToTheEmailLocalPart() throws Exception {
		assertThat(verifier.verify(token((c) -> c.claim("name", "  ").claim("given_name", "María")))).get()
			.extracting(GoogleIdentity::name)
			.isEqualTo("María");
		assertThat(verifier.verify(token((c) -> c.claim("name", null).claim("given_name", " ")))).get()
			.extracting(GoogleIdentity::name)
			.isEqualTo("maria");
	}

	@Test
	void isDisabledWithoutAClientId() {
		for (String clientId : new String[] { null, "   " }) {
			IdentityProperties properties = mock(IdentityProperties.class, Answers.RETURNS_DEEP_STUBS);
			when(properties.google().clientId()).thenReturn(clientId);
			NimbusGoogleIdTokenVerifier disabled = new NimbusGoogleIdTokenVerifier(properties);
			assertThat(disabled.isEnabled()).isFalse();
			assertThat(disabled.verify("cualquier-token")).isEmpty();
		}
	}

	@Test
	void isEnabledWithAClientIdWithoutContactingGoogle() {
		IdentityProperties properties = mock(IdentityProperties.class, Answers.RETURNS_DEEP_STUBS);
		when(properties.google().clientId()).thenReturn("  " + CLIENT + "  ");
		assertThat(new NimbusGoogleIdTokenVerifier(properties).isEnabled()).isTrue();
	}

}
