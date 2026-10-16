package com.fundigsac.shared.domain;

/** Error de negocio con un código estable que la API expone tal cual (nunca detalles internos). */
public abstract class DomainException extends RuntimeException {

	private final String code;

	protected DomainException(String code, String message) {
		super(message);
		this.code = code;
	}

	public String code() {
		return this.code;
	}

}
