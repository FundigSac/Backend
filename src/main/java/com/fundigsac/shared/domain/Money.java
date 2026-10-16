package com.fundigsac.shared.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Importe en soles o dólares con dos decimales; nunca negativo. */
public record Money(BigDecimal amount, Currency currency) {

	public enum Currency {
		PEN, USD
	}

	private static final BigDecimal MAX = new BigDecimal("99999999.99");

	public Money {
		Guard.notNull("currency", currency);
		Guard.notNull("amount", amount);
		if (amount.signum() < 0 || amount.compareTo(MAX) > 0 || amount.stripTrailingZeros().scale() > 2) {
			throw new ValidationException("amount", "INVALID_AMOUNT", "Ingrese un monto válido (hasta dos decimales).");
		}
		amount = amount.setScale(2, RoundingMode.UNNECESSARY);
	}

}
