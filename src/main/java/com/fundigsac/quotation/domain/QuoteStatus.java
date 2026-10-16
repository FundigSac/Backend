package com.fundigsac.quotation.domain;

import java.util.EnumSet;
import java.util.Set;

/**
 * Estados de atención (arquitectura §11.2, a validar con Ventas). Solo se permiten las transiciones declaradas;
 * CLOSED y CANCELLED son finales.
 */
public enum QuoteStatus {

	RECEIVED, ASSIGNED, IN_REVIEW, RESPONDED, CLOSED, CANCELLED;

	public Set<QuoteStatus> next() {
		return switch (this) {
			case RECEIVED -> EnumSet.of(ASSIGNED, IN_REVIEW, CANCELLED);
			case ASSIGNED -> EnumSet.of(IN_REVIEW, CANCELLED);
			case IN_REVIEW -> EnumSet.of(RESPONDED, CANCELLED);
			case RESPONDED -> EnumSet.of(CLOSED, IN_REVIEW);
			case CLOSED, CANCELLED -> EnumSet.noneOf(QuoteStatus.class);
		};
	}

	public boolean isFinal() {
		return this == CLOSED || this == CANCELLED;
	}

}
