package com.fundigsac.quotation.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

/** Registra una solicitud de cotización y devuelve su constancia (folio + token). */
public interface QuoteSubmission {

	record Contact(String companyName, String ruc, String contactName, @Nullable String position, String email,
			String phone) {
	}

	record Project(@Nullable String projectType, @Nullable String deliveryDepartment, @Nullable String deliveryPlace,
			@Nullable LocalDate requestedDeliveryDate, @Nullable String message) {
	}

	/**
	 * Línea solicitada. Con productSlug se valida contra el catálogo publicado y se copian nombre y código; sin él es
	 * una necesidad descrita libremente (productName obligatorio).
	 */
	record Line(@Nullable String productSlug, @Nullable String productName, @Nullable String variantLabel,
			BigDecimal quantity, String unit, @Nullable List<String> addons, @Nullable String notes) {
	}

	/** @param origin GENERAL_FORM, PRODUCT_FORM, CART o PROCUREMENT */
	record Request(String origin, Contact contact, Project project, List<Line> lines, String privacyNoticeVersion,
			@Nullable UUID customerId) {
	}

	record Receipt(String folio, String status, Instant receivedAt, String receiptToken) {
	}

	Receipt submit(Request request, @Nullable String idempotencyKey);

}
