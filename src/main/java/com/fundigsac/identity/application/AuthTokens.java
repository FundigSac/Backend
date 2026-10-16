package com.fundigsac.identity.application;

/**
 * Par de tokens de una sesión.
 *
 * @param accessToken JWT para la cabecera Authorization
 * @param expiresIn segundos de vida del token de acceso
 * @param refreshToken token opaco para renovar (rota en cada uso)
 * @param refreshExpiresIn segundos de vida del refresh token
 */
public record AuthTokens(String accessToken, long expiresIn, String refreshToken, long refreshExpiresIn) {

	@Override
	public String toString() {
		return "AuthTokens[expiresIn=" + this.expiresIn + "]";
	}

}
