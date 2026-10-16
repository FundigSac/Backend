package com.fundigsac.quotation.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import com.fundigsac.quotation.domain.QuoteItem;
import com.fundigsac.quotation.domain.QuoteRequest;
import com.fundigsac.quotation.domain.StatusChange;

/** Vistas de cotizaciones para la API. */
public final class QuoteViews {

	private QuoteViews() {
	}

	public record ItemView(int lineNo, @Nullable String productSlug, String productName, @Nullable String sku,
			@Nullable String variantLabel, BigDecimal quantity, String unit, String unitLabel, List<String> addons,
			@Nullable String notes) {

		static ItemView of(QuoteItem i) {
			return new ItemView(i.lineNo(), i.productSlug(), i.productName(), i.sku(), i.variantLabel(),
					i.quantity().stripTrailingZeros(), i.unit().code(), i.unit().label(), i.addons(), i.notes());
		}

	}

	public record HistoryView(@Nullable String from, String to, @Nullable String note, @Nullable UUID actorId,
			Instant occurredAt) {

		static HistoryView of(StatusChange c) {
			return new HistoryView(c.from() == null ? null : c.from().name(), c.to().name(), c.note(), c.actorId(),
					c.occurredAt());
		}

	}

	public record QuoteSummaryView(UUID id, String folio, String origin, String status, String companyName,
			String contactName, String email, int lineCount, @Nullable UUID assigneeId, Instant submittedAt,
			Instant updatedAt) {
	}

	public record ContactView(String companyName, String ruc, String contactName, @Nullable String position, String email,
			String phone) {
	}

	public record ProjectView(@Nullable String projectType, @Nullable String deliveryDepartment,
			@Nullable String deliveryPlace, @Nullable LocalDate requestedDeliveryDate, @Nullable String message) {
	}

	public record QuoteDetailView(UUID id, String folio, String origin, String status, ContactView contact,
			ProjectView project, List<ItemView> items, String privacyNoticeVersion, @Nullable UUID customerId,
			@Nullable UUID assigneeId, @Nullable String statusReason, List<HistoryView> history, Instant submittedAt,
			Instant updatedAt, long version) {

		static QuoteDetailView of(QuoteRequest q, List<StatusChange> history) {
			var r = q.requester();
			var p = q.project();
			return new QuoteDetailView(q.id(), q.folio().value(), q.origin().name(), q.status().name(),
					new ContactView(r.companyName(), r.ruc().value(), r.contactName(), r.position(), r.email().value(),
							r.phone().value()),
					new ProjectView(p.projectType(), p.deliveryDepartment(), p.deliveryPlace(), p.requestedDeliveryDate(),
							p.message()),
					q.items().stream().map(ItemView::of).toList(), q.privacyNoticeVersion(), q.customerId(), q.assigneeId(),
					q.statusReason(), history.stream().map(HistoryView::of).toList(), q.submittedAt(), q.updatedAt(),
					q.version());
		}

	}

	/** Constancia mínima: confirma el registro sin exponer datos completos (correo y teléfono enmascarados). */
	public record QuoteReceiptView(String folio, String status, Instant receivedAt, String companyName, String contactName,
			String email, List<ItemView> items) {

		static QuoteReceiptView of(QuoteRequest q) {
			return new QuoteReceiptView(q.folio().value(), q.status().name(), q.submittedAt(), q.requester().companyName(),
					q.requester().contactName(), q.requester().email().masked(), q.items().stream().map(ItemView::of).toList());
		}

	}

}
