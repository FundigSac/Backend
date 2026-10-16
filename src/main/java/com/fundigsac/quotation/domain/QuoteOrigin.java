package com.fundigsac.quotation.domain;

/** Canal de la solicitud; define el prefijo del folio (REQ para requerimientos de compra, COT para el resto). */
public enum QuoteOrigin {

	GENERAL_FORM("COT"), PRODUCT_FORM("COT"), CART("COT"), PROCUREMENT("REQ");

	private final String folioPrefix;

	QuoteOrigin(String folioPrefix) {
		this.folioPrefix = folioPrefix;
	}

	public String folioPrefix() {
		return this.folioPrefix;
	}

}
