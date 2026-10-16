package com.fundigsac.shared.domain;

import java.util.List;

/** Uno o más valores no cumplen las reglas del dominio (HTTP 400). */
public final class ValidationException extends DomainException {

	public record FieldViolation(String field, String code, String message) {
	}

	private final List<FieldViolation> violations;

	public ValidationException(String field, String code, String message) {
		this(List.of(new FieldViolation(field, code, message)));
	}

	public ValidationException(List<FieldViolation> violations) {
		super("VALIDATION_ERROR", "Revise los campos indicados.");
		this.violations = List.copyOf(violations);
	}

	public List<FieldViolation> violations() {
		return this.violations;
	}

}
