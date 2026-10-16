package com.fundigsac.shared.domain;

/** Autorización por objeto: el actor no puede operar sobre este recurso concreto (HTTP 403). */
public final class ForbiddenException extends DomainException {

	public ForbiddenException(String code, String message) {
		super(code, message);
	}

}
