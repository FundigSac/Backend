package com.fundigsac.shared.domain;

/** El recurso no existe o no es visible para quien lo pide (HTTP 404). */
public final class NotFoundException extends DomainException {

	public NotFoundException(String code, String message) {
		super(code, message);
	}

}
