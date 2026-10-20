package com.fundigsac.identity.domain;

import java.util.Optional;

/**
 * Puerto que valida un ID token de Google (firma con las claves públicas de Google, emisor, audiencia = nuestro
 * cliente OAuth y vigencia) y devuelve la identidad solo si el correo está verificado por Google.
 */
public interface GoogleIdTokenVerifier {

	record GoogleIdentity(String subject, String email, String name) {
	}

	/** false si no hay cliente de Google configurado: el ingreso con Google queda deshabilitado. */
	boolean isEnabled();

	Optional<GoogleIdentity> verify(String idToken);

}
