package com.fundigsac.identity.application;

import java.time.Instant;

import com.fundigsac.identity.domain.UserAccount;

/** Puerto de emisión del token de acceso (JWT firmado en infraestructura). */
public interface AccessTokenIssuer {

	record IssuedToken(String value, Instant expiresAt) {
	}

	IssuedToken issue(UserAccount account, Instant now);

}
