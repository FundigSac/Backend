package com.fundigsac.catalog.domain;

import java.util.Map;

/** Nombres legibles de las familias técnicas (mismos textos que el sitio). Una familia sin nombre usa su código. */
public final class Families {

	private static final Map<String, String> LABELS = Map.ofEntries(Map.entry("compuerta", "Válvulas de compuerta"),
			Map.entry("mariposa", "Válvulas mariposa"), Map.entry("retencion", "Válvulas de retención"),
			Map.entry("regulacion", "Regulación hidráulica y alivio"), Map.entry("ventosa", "Aire y purga (ventosas)"),
			Map.entry("tuberia", "Tuberías de hierro dúctil"), Map.entry("marco-tapa", "Marcos y tapas de calzada"),
			Map.entry("rejilla", "Rejillas de sumidero"), Map.entry("hdpe", "Accesorios HDPE"),
			Map.entry("equipos", "Equipos de termofusión"), Map.entry("acoples", "Acoples y uniones"),
			Map.entry("hierro-ductil", "Hierro dúctil"), Map.entry("medidores", "Medidores"),
			Map.entry("valvulas", "Válvulas industriales"));

	private Families() {
	}

	public static String label(String code) {
		return LABELS.getOrDefault(code, code);
	}

}
