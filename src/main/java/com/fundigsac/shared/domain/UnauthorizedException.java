package com.fundigsac.shared.domain;

/** Credenciales o token no válidos (HTTP 401). El mensaje nunca revela si la cuenta existe. */
public final class UnauthorizedException extends DomainException {

	public UnauthorizedException(String code, String message) {
		super(code, message);
	}

}
