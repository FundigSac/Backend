package com.fundigsac.shared.domain;

import java.util.Locale;

import org.jspecify.annotations.Nullable;

/** Documento de identidad aceptado en el Perú: DNI, carné de extranjería, pasaporte o RUC. */
public record IdentityDocument(Type type, String number) {

	public enum Type {

		DNI("\\d{8}"), CE("[A-Z0-9]{9,12}"), PASSPORT("[A-Z0-9]{6,12}"), RUC("\\d{11}");

		private final String pattern;

		Type(String pattern) {
			this.pattern = pattern;
		}

	}

	public IdentityDocument {
		Guard.notNull("documentType", type);
		number = number == null ? "" : number.replaceAll("[\\s.-]", "").toUpperCase(Locale.ROOT);
		boolean valid = type == Type.RUC ? Ruc.isValid(number) : number.matches(type.pattern);
		if (!valid) {
			throw new ValidationException("documentNumber", "INVALID_DOCUMENT", "El número no corresponde al tipo de documento.");
		}
	}

	public static IdentityDocument of(@Nullable Type type, @Nullable String number) {
		Guard.notNull("documentType", type);
		if (number == null || number.isBlank()) {
			throw new ValidationException("documentNumber", "REQUIRED", "Este campo es obligatorio.");
		}
		return new IdentityDocument(type, number);
	}

	/** Versión enmascarada para listados: DNI ****5678. */
	public String masked() {
		return this.type + " ****" + this.number.substring(Math.max(0, this.number.length() - 4));
	}

}
