package com.fundigsac.identity.infrastructure;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import com.fundigsac.identity.application.IdentityProperties;
import com.fundigsac.identity.domain.GoogleIdTokenVerifier;

/**
 * Valida ID tokens de Google con sus claves públicas (JWKS, en caché) según OIDC Core §3.1.3.7: firma RS256, emisor
 * accounts.google.com, audiencia igual al cliente configurado, vigencia (60 s de tolerancia) y email_verified.
 */
@Component
class NimbusGoogleIdTokenVerifier implements GoogleIdTokenVerifier {

	private static final Logger log = LoggerFactory.getLogger(NimbusGoogleIdTokenVerifier.class);

	private static final String JWKS = "https://www.googleapis.com/oauth2/v3/certs";

	private static final Set<String> ISSUERS = Set.of("https://accounts.google.com", "accounts.google.com");

	private final @Nullable String clientId;

	private final @Nullable JwtDecoder decoder;

	NimbusGoogleIdTokenVerifier(IdentityProperties properties) {
		String configured = properties.google().clientId();
		this.clientId = configured == null || configured.isBlank() ? null : configured.strip();
		this.decoder = this.clientId == null ? null : decoder(this.clientId);
	}

	private static JwtDecoder decoder(String clientId) {
		NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(JWKS).build();
		OAuth2TokenValidator<Jwt> issuer = (jwt) -> ISSUERS.contains(String.valueOf(jwt.getClaims().get("iss")))
				? OAuth2TokenValidatorResult.success() : failure("Emisor no válido");
		OAuth2TokenValidator<Jwt> audience = (jwt) -> jwt.getAudience() != null && jwt.getAudience().contains(clientId)
				? OAuth2TokenValidatorResult.success() : failure("Audiencia no válida");
		decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
				List.of(new JwtTimestampValidator(Duration.ofSeconds(60)), issuer, audience)));
		return decoder;
	}

	private static OAuth2TokenValidatorResult failure(String description) {
		return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", description, null));
	}

	@Override
	public boolean isEnabled() {
		return this.decoder != null;
	}

	@Override
	public Optional<GoogleIdentity> verify(String idToken) {
		if (this.decoder == null || idToken == null || idToken.isBlank() || idToken.length() > 4096) {
			return Optional.empty();
		}
		try {
			Jwt jwt = this.decoder.decode(idToken.strip());
			Object verified = jwt.getClaims().get("email_verified");
			String email = jwt.getClaimAsString("email");
			boolean emailVerified = Boolean.TRUE.equals(verified) || "true".equals(String.valueOf(verified));
			if (email == null || !emailVerified || jwt.getSubject() == null) {
				return Optional.empty();
			}
			String name = jwt.getClaimAsString("name");
			if (name == null || name.isBlank()) {
				name = jwt.getClaimAsString("given_name");
			}
			if (name == null || name.isBlank()) {
				name = email.substring(0, email.indexOf('@'));
			}
			return Optional.of(new GoogleIdentity(jwt.getSubject(), email, name.strip()));
		}
		catch (JwtException ex) {
			log.info("ID token de Google rechazado: {}", ex.getMessage());
			return Optional.empty();
		}
	}

}
