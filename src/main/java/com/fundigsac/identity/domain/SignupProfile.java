package com.fundigsac.identity.domain;

import java.time.Instant;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import com.fundigsac.shared.domain.ValidationException;

/**
 * Cómo y desde dónde se creó la cuenta, y si aceptó el boletín (con la fecha, como constancia del consentimiento de la
 * Ley 29733). La cuenta de Google guarda además su identificador estable (sub).
 *
 * @param method EMAIL (correo y contraseña o invitación del personal), GOOGLE o FACEBOOK
 * @param source página del sitio desde la que se registró (registro, carrito, cotizar o login)
 * @param newsletterOptInAt cuándo aceptó recibir el boletín; nulo si no lo aceptó
 * @param googleSubject identificador de la cuenta de Google vinculada
 */
public record SignupProfile(Method method, @Nullable String source, @Nullable Instant newsletterOptInAt,
		@Nullable String googleSubject) {

	public enum Method {
		EMAIL, GOOGLE, FACEBOOK
	}

	private static final Set<String> SOURCES = Set.of("registro", "carrito", "cotizar", "login");

	public static SignupProfile email() {
		return new SignupProfile(Method.EMAIL, null, null, null);
	}

	/** Origen admitido o nulo; cualquier otro valor se rechaza para no guardar texto libre del navegador. */
	public static @Nullable String source(@Nullable String raw) {
		if (raw == null || raw.isBlank()) {
			return null;
		}
		String value = raw.strip();
		if (!SOURCES.contains(value)) {
			throw new ValidationException("source", "INVALID_SOURCE", "Origen de registro no válido.");
		}
		return value;
	}

	public boolean newsletter() {
		return this.newsletterOptInAt != null;
	}

	SignupProfile withContext(@Nullable String newSource, boolean newsletter, Instant now) {
		return new SignupProfile(this.method, newSource, newsletter ? now : null, this.googleSubject);
	}

	SignupProfile withGoogle(String subject) {
		return new SignupProfile(this.method, this.source, this.newsletterOptInAt, subject);
	}

}
