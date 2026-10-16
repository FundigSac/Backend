package com.fundigsac.quotation.api;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import com.fundigsac.shared.domain.AuditableEvent;

/** Eventos de cotizaciones. */
public final class QuoteEvents {

	private QuoteEvents() {
	}

	/** Solicitud registrada: se envía la constancia al solicitante y el aviso a Ventas. */
	public record QuoteSubmitted(UUID quoteId, String folio, String origin, String companyName, String contactName,
			String email, int lineCount, @Nullable UUID customerId, Instant occurredAt) implements AuditableEvent {

		@Override
		public String auditAction() {
			return "QUOTE_SUBMITTED";
		}

		@Override
		public String auditResourceType() {
			return "QUOTE";
		}

		@Override
		public String auditResourceId() {
			return this.quoteId.toString();
		}

		@Override
		public @Nullable UUID actorId() {
			return this.customerId;
		}

		@Override
		public Map<String, String> auditDetails() {
			return Map.of("folio", this.folio, "origin", this.origin, "lines", Integer.toString(this.lineCount));
		}

		@Override
		public String toString() {
			return "QuoteSubmitted[folio=" + this.folio + "]";
		}

	}

	public record QuoteStatusChanged(UUID quoteId, String folio, @Nullable String fromStatus, String toStatus,
			@Nullable UUID assigneeId, @Nullable String note, @Nullable UUID actorId, Instant occurredAt)
			implements AuditableEvent {

		@Override
		public String auditAction() {
			return "QUOTE_" + this.toStatus;
		}

		@Override
		public String auditResourceType() {
			return "QUOTE";
		}

		@Override
		public String auditResourceId() {
			return this.quoteId.toString();
		}

		@Override
		public Map<String, String> auditDetails() {
			Map<String, String> details = new java.util.HashMap<>();
			details.put("folio", this.folio);
			if (this.fromStatus != null) {
				details.put("from", this.fromStatus);
			}
			if (this.assigneeId != null) {
				details.put("assignee", this.assigneeId.toString());
			}
			return details;
		}

	}

}
