package com.fundigsac.identity.infrastructure;

import java.time.Instant;
import java.util.List;

import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import com.fundigsac.identity.application.AccessTokenIssuer;
import com.fundigsac.identity.application.IdentityProperties;
import com.fundigsac.identity.domain.UserAccount;
import com.fundigsac.shared.domain.Ids;

/** JWT de acceso: sujeto = id de la cuenta, roles y sello de seguridad. Sin correo ni datos personales. */
@Component
class JwtAccessTokenIssuer implements AccessTokenIssuer {

	private final JwtEncoder encoder;

	private final JwtKeys keys;

	private final IdentityProperties properties;

	JwtAccessTokenIssuer(JwtEncoder encoder, JwtKeys keys, IdentityProperties properties) {
		this.encoder = encoder;
		this.keys = keys;
		this.properties = properties;
	}

	@Override
	public IssuedToken issue(UserAccount account, Instant now) {
		Instant expiresAt = now.plus(this.properties.jwt().accessTokenTtl());
		JwtClaimsSet claims = JwtClaimsSet.builder()
			.issuer(this.properties.jwt().issuer())
			.audience(List.of(this.properties.jwt().audience()))
			.subject(account.id().toString())
			.issuedAt(now)
			.notBefore(now)
			.expiresAt(expiresAt)
			.id(Ids.newId().toString())
			.claim("kind", account.kind().name())
			.claim("roles", account.roles().stream().map(Enum::name).sorted().toList())
			.claim("sv", account.securityStamp())
			.build();
		JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).keyId(this.keys.rsaKey().getKeyID()).type("JWT").build();
		return new IssuedToken(this.encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue(), expiresAt);
	}

}
