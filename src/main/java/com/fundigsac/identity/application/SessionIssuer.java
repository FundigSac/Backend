package com.fundigsac.identity.application;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.fundigsac.identity.domain.AccountKind;
import com.fundigsac.identity.domain.RefreshToken;
import com.fundigsac.identity.domain.RefreshTokenRepository;
import com.fundigsac.identity.domain.UserAccount;
import com.fundigsac.shared.domain.SecureTokens;

/** Emite el par token de acceso + refresh token (se guarda solo el hash del refresh). */
@Component
class SessionIssuer {

	private final AccessTokenIssuer accessTokens;

	private final RefreshTokenRepository refreshTokens;

	private final IdentityProperties properties;

	SessionIssuer(AccessTokenIssuer accessTokens, RefreshTokenRepository refreshTokens, IdentityProperties properties) {
		this.accessTokens = accessTokens;
		this.refreshTokens = refreshTokens;
		this.properties = properties;
	}

	AuthTokens issue(UserAccount account, UUID familyId, UUID refreshId, Instant now) {
		Duration ttl = account.kind() == AccountKind.STAFF ? this.properties.sessions().staffRefreshTtl()
				: this.properties.sessions().customerRefreshTtl();
		String raw = SecureTokens.random();
		this.refreshTokens.insert(new RefreshToken(refreshId, account.id(), familyId, SecureTokens.sha256(raw), now,
				now.plus(ttl), null, null));
		AccessTokenIssuer.IssuedToken access = this.accessTokens.issue(account, now);
		return new AuthTokens(access.value(), Duration.between(now, access.expiresAt()).toSeconds(), raw, ttl.toSeconds());
	}

}
