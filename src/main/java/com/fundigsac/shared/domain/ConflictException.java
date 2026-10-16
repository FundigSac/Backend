package com.fundigsac.shared.domain;

/** El estado actual impide la operación: transición no permitida, duplicado o versión desactualizada (HTTP 409). */
public final class ConflictException extends DomainException {

	public ConflictException(String code, String message) {
		super(code, message);
	}

}
