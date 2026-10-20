package com.fundigsac.identity.domain;

import org.jspecify.annotations.Nullable;

/**
 * Cómo se creó la cuenta. La cuenta de Google guarda además su identificador estable (sub).
 *
 * @param method EMAIL (correo y contraseña o invitación del personal), GOOGLE o FACEBOOK
 * @param googleSubject identificador de la cuenta de Google vinculada
 */
public record SignupProfile(Method method, @Nullable String googleSubject) {

	public enum Method {
		EMAIL, GOOGLE, FACEBOOK
	}

	public static SignupProfile email() {
		return new SignupProfile(Method.EMAIL, null);
	}

	SignupProfile withGoogle(String subject) {
		return new SignupProfile(this.method, subject);
	}

}
