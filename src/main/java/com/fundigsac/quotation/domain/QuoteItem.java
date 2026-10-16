package com.fundigsac.quotation.domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import com.fundigsac.shared.domain.Guard;
import com.fundigsac.shared.domain.ValidationException;

/**
 * Línea solicitada con copia (snapshot) del producto: si la ficha cambia o se archiva, la solicitud conserva lo que se
 * pidió. Sin producto del catálogo es una necesidad descrita libremente.
 */
public record QuoteItem(int lineNo, @Nullable UUID productId, @Nullable String productSlug, String productName,
		@Nullable String sku, @Nullable String variantLabel, BigDecimal quantity, QuoteUnit unit, List<String> addons,
		@Nullable String notes) {

	private static final BigDecimal MAX_QUANTITY = new BigDecimal("1000000");

	public QuoteItem {
		if (lineNo < 1 || lineNo > QuoteRequest.MAX_ITEMS) {
			throw new IllegalArgumentException("Número de línea fuera de rango");
		}
		productName = Guard.text("items.productName", productName, 2, 200);
		variantLabel = Guard.optionalText("items.variantLabel", variantLabel, 120);
		Guard.notNull("items.quantity", quantity);
		if (quantity.signum() <= 0 || quantity.compareTo(MAX_QUANTITY) > 0 || quantity.stripTrailingZeros().scale() > 3) {
			throw new ValidationException("items.quantity", "OUT_OF_RANGE", "La cantidad debe ser mayor que 0 (hasta 3 decimales).");
		}
		Guard.notNull("items.unit", unit);
		addons = Guard.maxSize("items.addons", addons == null ? List.<String>of() : addons, 10)
			.stream()
			.map((a) -> Guard.text("items.addons", a, 1, 120))
			.distinct()
			.toList();
		notes = Guard.optionalMultiline("items.notes", notes, 1000);
	}

}
