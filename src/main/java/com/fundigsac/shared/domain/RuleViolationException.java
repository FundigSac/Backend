package com.fundigsac.shared.domain;

/** La petición es válida en forma pero rompe una regla de negocio (HTTP 422). */
public final class RuleViolationException extends DomainException {

	public RuleViolationException(String code, String message) {
		super(code, message);
	}

}
