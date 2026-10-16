package com.fundigsac.identity.infrastructure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.fundigsac.identity.application.IdentityProperties;
import com.fundigsac.identity.domain.Role;

/**
 * Emisión y validación de JWT RS256: firma, emisor, audiencia, vigencia (60 s de tolerancia de reloj) y sello de
 * seguridad. Las autoridades se derivan de los roles en el servidor; el token no puede inventar permisos.
 */
@Configuration(proxyBeanMethods = false)
class JwtConfig {

	@Bean
	JwtEncoder jwtEncoder(JwtKeys keys) {
		return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(keys.rsaKey())));
	}

	@Bean
	JwtDecoder jwtDecoder(JwtKeys keys, IdentityProperties properties, SecurityStampValidator stampValidator) {
		NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(keys.publicKey())
			.signatureAlgorithm(SignatureAlgorithm.RS256)
			.build();
		String audience = properties.jwt().audience();
		decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
				JwtValidators.createDefaultWithIssuer(properties.jwt().issuer()),
				new JwtClaimValidator<List<String>>(JwtClaimNames.AUD, (aud) -> aud != null && aud.contains(audience)),
				stampValidator));
		return decoder;
	}

	@Bean
	Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
		return (jwt) -> new JwtAuthenticationToken(jwt, authorities(jwt), jwt.getSubject());
	}

	private static Collection<GrantedAuthority> authorities(Jwt jwt) {
		List<GrantedAuthority> authorities = new ArrayList<>();
		List<String> roles = jwt.getClaimAsStringList("roles");
		boolean staff = false;
		for (String name : roles == null ? List.<String>of() : roles) {
			Role role;
			try {
				role = Role.valueOf(name);
			}
			catch (IllegalArgumentException ex) {
				continue;
			}
			authorities.add(new SimpleGrantedAuthority("ROLE_" + role.name()));
			role.permissions().forEach((p) -> authorities.add(new SimpleGrantedAuthority(p.authority())));
			staff |= role.isStaff();
		}
		if (staff) {
			authorities.add(new SimpleGrantedAuthority("ROLE_STAFF"));
		}
		return authorities;
	}

}
