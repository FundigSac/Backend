package com.fundigsac.quotation;

import java.util.Map;

/** Solicitud de cotización válida para las pruebas de otros módulos. */
public final class QuotationFixtures {

	private QuotationFixtures() {
	}

	public static Map<String, Object> quote() {
		return QuotationIT.quote("vcp-f4-01", null);
	}

}
