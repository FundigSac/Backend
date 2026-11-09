package com.fundigsac.identity;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.web.servlet.ResultActions;

import com.fundigsac.identity.application.IdentityProperties;
import com.fundigsac.support.IntegrationTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Validación real de los JWT de acceso. Cada caso firma un token con la clave de la aplicación y altera un solo dato, o
 * cambia la firma. El caso de control demuestra que, sin esa alteración, el mismo token se acepta.
 */
class JwtValidationIT extends IntegrationTest {

	private static final String PROTECTED = "/api/v1/staff/profile";

	@Autowired
	JwtEncoder encoder;

	@Autowired
	IdentityProperties properties;

	private int securityStamp() {
		return this.jdbc.sql("SELECT security_stamp FROM user_accounts WHERE email = :e").param("e", ADMIN_EMAIL).query(Integer.class).single();
	}

	/** Mismos datos que emite JwtAccessTokenIssuer para el administrador. */
	private JwtClaimsSet.Builder validClaims() {
		Instant now = Instant.now();
		return JwtClaimsSet.builder()
			.issuer(this.properties.jwt().issuer())
			.audience(List.of(this.properties.jwt().audience()))
			.subject(userId(ADMIN_EMAIL))
			.issuedAt(now)
			.notBefore(now)
			.expiresAt(now.plus(Duration.ofMinutes(10)))
			.id(UUID.randomUUID().toString())
			.claim("kind", "STAFF")
			.claim("roles", List.of("ADMINISTRATOR"))
			.claim("sv", securityStamp());
	}

	private static JwtEncoderParameters parameters(JwtClaimsSet claims) {
		return JwtEncoderParameters.from(JwsHeader.with(SignatureAlgorithm.RS256).type("JWT").build(), claims);
	}

	private String sign(JwtClaimsSet claims) {
		return this.encoder.encode(parameters(claims)).getTokenValue();
	}

	private ResultActions call(String token) throws Exception {
		return this.mvc.perform(get(PROTECTED).with(randomIp()).header(HttpHeaders.AUTHORIZATION, "Bearer " + token));
	}

	private void assertRejected(String token) throws Exception {
		call(token).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("INVALID_TOKEN"));
	}

	private void assertRejected(JwtClaimsSet claims) throws Exception {
		assertRejected(sign(claims));
	}

	private static String base64Url(String text) {
		return Base64.getUrlEncoder().withoutPadding().encodeToString(text.getBytes(StandardCharsets.UTF_8));
	}

	@Test
	void aTokenWithEveryClaimCorrectIsAccepted() throws Exception {
		call(sign(validClaims().build())).andExpect(status().isOk()).andExpect(jsonPath("$.email").value(ADMIN_EMAIL));
	}

	@Test
	void anotherIssuerIsRejected() throws Exception {
		assertRejected(validClaims().issuer("https://emisor-ajeno.example").build());
	}

	@Test
	void aTokenWithoutIssuerIsRejected() throws Exception {
		assertRejected(validClaims().claims((claims) -> claims.remove("iss")).build());
	}

	@Test
	void anotherAudienceIsRejected() throws Exception {
		assertRejected(validClaims().audience(List.of("otra-api")).build());
	}

	@Test
	void aTokenWithoutAudienceIsRejected() throws Exception {
		assertRejected(validClaims().claims((claims) -> claims.remove("aud")).build());
	}

	@Test
	void anExpiredTokenIsRejected() throws Exception {
		Instant now = Instant.now();
		assertRejected(validClaims().issuedAt(now.minus(Duration.ofMinutes(20)))
			.notBefore(now.minus(Duration.ofMinutes(20)))
			.expiresAt(now.minus(Duration.ofMinutes(2)))
			.build());
	}

	@Test
	void theClockToleranceIsSixtySeconds() throws Exception {
		Instant now = Instant.now();
		call(sign(validClaims().issuedAt(now.minus(Duration.ofMinutes(20)))
			.notBefore(now.minus(Duration.ofMinutes(20)))
			.expiresAt(now.minusSeconds(30))
			.build())).andExpect(status().isOk());
	}

	@Test
	void aTokenNotYetValidIsRejected() throws Exception {
		assertRejected(validClaims().notBefore(Instant.now().plus(Duration.ofMinutes(5))).build());
	}

	@Test
	void aTokenSignedWithAnotherKeyIsRejected() throws Exception {
		RSAKey foreign = new RSAKeyGenerator(2048).keyID("clave-ajena").generate();
		JwtEncoder foreignEncoder = new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(foreign)));
		assertRejected(foreignEncoder.encode(parameters(validClaims().build())).getTokenValue());
	}

	@Test
	void anUnsignedTokenIsRejected() throws Exception {
		String[] parts = sign(validClaims().build()).split("\\.");
		assertRejected(base64Url("{\"alg\":\"none\",\"typ\":\"JWT\"}") + "." + parts[1] + ".");
	}

	@Test
	void anotherAlgorithmIsRejected() throws Exception {
		SignedJWT hmac = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), new JWTClaimsSet.Builder().subject(userId(ADMIN_EMAIL))
			.issuer(this.properties.jwt().issuer())
			.audience(this.properties.jwt().audience())
			.expirationTime(java.util.Date.from(Instant.now().plus(Duration.ofMinutes(10))))
			.claim("roles", List.of("ADMINISTRATOR"))
			.claim("sv", securityStamp())
			.build());
		hmac.sign(new MACSigner("clave-simetrica-de-32-bytes-como-minimo".getBytes(StandardCharsets.UTF_8)));
		assertRejected(hmac.serialize());
	}

	@Test
	void aTamperedPayloadIsRejected() throws Exception {
		String[] parts = sign(validClaims().claim("roles", List.of("READ_ONLY")).build()).split("\\.");
		String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8).replace("READ_ONLY", "ADMINISTRATOR");
		assertRejected(parts[0] + "." + base64Url(payload) + "." + parts[2]);
	}

	@Test
	void aRevokedSecurityStampIsRejected() throws Exception {
		assertRejected(validClaims().claim("sv", securityStamp() - 1).build());
	}

	@Test
	void aTokenWithoutSecurityStampIsRejected() throws Exception {
		assertRejected(validClaims().claims((claims) -> claims.remove("sv")).build());
	}

	@Test
	void aSubjectThatIsNotAnAccountIsRejected() throws Exception {
		assertRejected(validClaims().subject("no-es-un-uuid").build());
		assertRejected(validClaims().subject(UUID.randomUUID().toString()).build());
	}

	@Test
	void anUnknownRoleGrantsNothing() throws Exception {
		call(sign(validClaims().claim("roles", List.of("SUPERUSUARIO")).build())).andExpect(status().isForbidden())
			.andExpect(jsonPath("$.code").value("FORBIDDEN"));
	}

}
