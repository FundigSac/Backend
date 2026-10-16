package com.fundigsac.quotation.application;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fundigsac.catalog.api.CatalogLookup;
import com.fundigsac.catalog.api.CatalogLookup.ProductSnapshot;
import com.fundigsac.quotation.api.QuoteSubmission;
import com.fundigsac.quotation.domain.ProjectDetails;
import com.fundigsac.quotation.domain.QuoteItem;
import com.fundigsac.quotation.domain.QuoteOrigin;
import com.fundigsac.quotation.domain.QuoteRepository;
import com.fundigsac.quotation.domain.QuoteRequest;
import com.fundigsac.quotation.domain.QuoteUnit;
import com.fundigsac.quotation.domain.Requester;
import com.fundigsac.shared.application.DomainEvents;
import com.fundigsac.shared.application.FolioGenerator;
import com.fundigsac.shared.application.Idempotency;
import com.fundigsac.shared.application.ReceiptTokens;
import com.fundigsac.shared.domain.Email;
import com.fundigsac.shared.domain.Folio;
import com.fundigsac.shared.domain.Ids;
import com.fundigsac.shared.domain.PhoneNumber;
import com.fundigsac.shared.domain.Ruc;
import com.fundigsac.shared.domain.ValidationException;

/**
 * Registro de solicitudes: en una sola transacción valida contra el catálogo publicado, asigna folio, guarda la
 * solicitud con su historial, registra el evento (outbox) y la clave de idempotencia. El correo sale después.
 */
@Service
class QuoteSubmissionService implements QuoteSubmission {

	static final String RECEIPT_PURPOSE = "quote";

	private final QuoteRepository quotes;

	private final CatalogLookup catalog;

	private final FolioGenerator folios;

	private final Idempotency idempotency;

	private final ReceiptTokens receipts;

	private final DomainEvents events;

	private final Clock clock;

	QuoteSubmissionService(QuoteRepository quotes, CatalogLookup catalog, FolioGenerator folios, Idempotency idempotency,
			ReceiptTokens receipts, DomainEvents events, Clock clock) {
		this.quotes = quotes;
		this.catalog = catalog;
		this.folios = folios;
		this.idempotency = idempotency;
		this.receipts = receipts;
		this.events = events;
		this.clock = clock;
	}

	@Override
	@Transactional
	public Receipt submit(Request request, @Nullable String idempotencyKey) {
		return this.idempotency.execute("quote", idempotencyKey, request, () -> register(request), Receipt::folio,
				(folio) -> receiptOf(this.quotes.findByFolio(folio).orElseThrow()));
	}

	private Receipt register(Request request) {
		Instant now = this.clock.instant();
		QuoteOrigin origin = origin(request.origin());
		Contact c = request.contact();
		Requester requester = new Requester(c.companyName(), Ruc.of("contact.ruc", c.ruc()), c.contactName(), c.position(),
				Email.of("contact.email", c.email()), PhoneNumber.of("contact.phone", c.phone()));
		Project p = request.project();
		ProjectDetails project = new ProjectDetails(p.projectType(), p.deliveryDepartment(), p.deliveryPlace(),
				p.requestedDeliveryDate(), p.message());
		List<QuoteItem> items = items(request.lines());
		Folio folio = this.folios.next(origin.folioPrefix());
		QuoteRequest quote = QuoteRequest.submit(Ids.newId(), folio, origin, requester, project, items,
				request.privacyNoticeVersion(), request.customerId(), now);
		this.quotes.insert(quote);
		this.events.publishFrom(quote);
		return receiptOf(quote);
	}

	private List<QuoteItem> items(@Nullable List<Line> lines) {
		List<QuoteItem> items = new ArrayList<>();
		if (lines == null) {
			return items;
		}
		if (lines.size() > QuoteRequest.MAX_ITEMS) {
			throw new ValidationException("items", "TOO_MANY", "Se admiten como máximo " + QuoteRequest.MAX_ITEMS + " productos.");
		}
		int lineNo = 1;
		for (Line line : lines) {
			QuoteUnit unit = QuoteUnit.parse(line.unit());
			if (line.productSlug() != null && !line.productSlug().isBlank()) {
				ProductSnapshot product = this.catalog.findPublished(line.productSlug().strip())
					.orElseThrow(() -> new ValidationException("items.productSlug", "UNKNOWN_PRODUCT",
							"El producto no existe o no está publicado: " + line.productSlug()));
				if (!product.acceptsVariant(line.variantLabel())) {
					throw new ValidationException("items.variantLabel", "UNKNOWN_VARIANT",
							"La medida o variante no corresponde al producto " + product.name() + ".");
				}
				items.add(new QuoteItem(lineNo++, product.id(), product.slug(), product.name(), product.sku(),
						line.variantLabel(), line.quantity(), unit, line.addons(), line.notes()));
			}
			else {
				if (line.productName() == null || line.productName().isBlank()) {
					throw new ValidationException("items.productName", "REQUIRED", "Describa el producto que necesita.");
				}
				items.add(new QuoteItem(lineNo++, null, null, line.productName(), null, line.variantLabel(), line.quantity(),
						unit, line.addons(), line.notes()));
			}
		}
		return items;
	}

	private Receipt receiptOf(QuoteRequest quote) {
		String folio = quote.folio().value();
		return new Receipt(folio, quote.status().name(), quote.submittedAt(), this.receipts.issue(RECEIPT_PURPOSE, folio));
	}

	private static QuoteOrigin origin(String value) {
		try {
			return QuoteOrigin.valueOf(value == null ? "" : value.strip().toUpperCase(Locale.ROOT));
		}
		catch (IllegalArgumentException ex) {
			throw new ValidationException("origin", "INVALID_ORIGIN", "Origen no válido.");
		}
	}

}
