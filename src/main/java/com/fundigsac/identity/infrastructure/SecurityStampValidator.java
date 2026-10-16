package com.fundigsac.identity.infrastructure;

import java.util.Optional;
import java.util.UUID;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import com.fundigsac.identity.application.IdentityProperties;
import com.fundigsac.identity.domain.AccountStatus;
import com.fundigsac.identity.domain.UserAccountRepository;

/**
 * Revocación efectiva de JWT: el token lleva el sello de seguridad (claim sv) de la cuenta; si la cuenta se desactiva,
 * cambia de roles o de contraseña, el sello sube y los tokens anteriores dejan de valer (en segundos, no al expirar).
 */
@Component
class SecurityStampValidator implements OAuth2TokenValidator<Jwt> {

	private static final OAuth2Error REVOKED = new OAuth2Error("invalid_token", "El token fue revocado", null);

	private final UserAccountRepository accounts;

	private final Cache<UUID, Optional<UserAccountRepository.SecurityState>> cache;

	SecurityStampValidator(UserAccountRepository accounts, IdentityProperties properties) {
		this.accounts = accounts;
		this.cache = Caffeine.newBuilder()
			.maximumSize(50_000)
			.expireAfterWrite(properties.jwt().revocationCacheTtl())
			.build();
	}

	@Override
	public OAuth2TokenValidatorResult validate(Jwt jwt) {
		UUID userId;
		try {
			userId = UUID.fromString(jwt.getSubject());
		}
		catch (RuntimeException ex) {
			return OAuth2TokenValidatorResult.failure(REVOKED);
		}
		Object claim = jwt.getClaim("sv");
		if (!(claim instanceof Number stamp)) {
			return OAuth2TokenValidatorResult.failure(REVOKED);
		}
		Optional<UserAccountRepository.SecurityState> state = this.cache.get(userId, this.accounts::findSecurityState);
		boolean valid = state.isPresent() && state.get().status() == AccountStatus.ACTIVE
				&& state.get().securityStamp() == stamp.intValue();
		return valid ? OAuth2TokenValidatorResult.success() : OAuth2TokenValidatorResult.failure(REVOKED);
	}

}
