package com.fundigsac.catalog.domain;

import java.util.Arrays;

import org.jspecify.annotations.Nullable;

import com.fundigsac.shared.domain.ValidationException;

/** Tipo de unión o conexión (misma taxonomía que los filtros del sitio). */
public enum JointType {

	FLANGED("bridada", "Bridada"),
	FLANGED_F4("bridada-f4", "Bridada F4 (DIN 3202)"),
	FLANGED_F5("bridada-f5", "Bridada F5 (DIN 3202)"),
	GROOVED("ranurada", "Ranurada"),
	THREADED("roscada", "Roscada"),
	TYTON("tyton", "Campana Tyton"),
	RESTRAINED("acerrojada", "Acerrojada"),
	MECHANICAL("mecanica", "Mecánica"),
	BUTT_FUSION("termofusion", "Termofusión"),
	NONE("ninguna", "Sin unión");

	private final String code;

	private final String label;

	JointType(String code, String label) {
		this.code = code;
		this.label = label;
	}

	public String code() {
		return this.code;
	}

	public String label() {
		return this.label;
	}

	public static @Nullable JointType fromCode(@Nullable String code) {
		if (code == null || code.isBlank()) {
			return null;
		}
		return Arrays.stream(values())
			.filter((j) -> j.code.equals(code))
			.findFirst()
			.orElseThrow(() -> new ValidationException("joint", "INVALID_JOINT", "Tipo de unión no reconocido."));
	}

}
