package com.fundigsac.catalog.domain;

import java.util.Arrays;

import com.fundigsac.shared.domain.ValidationException;

/** Homologaciones que el sitio permite filtrar. Publicarlas exige respaldo documental (data_verified). */
public enum Homologation {

	SEDAPAL("sedapal", "SEDAPAL"), OTASS("otass", "OTASS"), WRAS("wras", "WRAS");

	private final String code;

	private final String label;

	Homologation(String code, String label) {
		this.code = code;
		this.label = label;
	}

	public String code() {
		return this.code;
	}

	public String label() {
		return this.label;
	}

	public static Homologation fromCode(String code) {
		return Arrays.stream(values())
			.filter((h) -> h.code.equals(code))
			.findFirst()
			.orElseThrow(() -> new ValidationException("homologations", "INVALID_HOMOLOGATION", "Homologación no reconocida: " + code));
	}

}
