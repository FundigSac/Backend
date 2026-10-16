package com.fundigsac.shared.application;

/** Huella estable (SHA-256) del contenido de una petición, para detectar reutilización de claves de idempotencia. */
public interface RequestFingerprints {

	String of(Object payload);

}
