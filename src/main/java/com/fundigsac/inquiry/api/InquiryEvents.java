package com.fundigsac.inquiry.api;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import com.fundigsac.shared.domain.AuditableEvent;
import com.fundigsac.shared.domain.DomainEvent;

/** Eventos de consultas de contacto. */
public final class InquiryEvents {

	private InquiryEvents() {
	}

	public record InquiryReceived(UUID inquiryId, String folio, String reason, String fullName, String companyName,
			String email, Instant occurredAt) implements AuditableEvent {

		@Override
		public String auditAction() {
			return "INQUIRY_RECEIVED";
		}

		@Override
		public String auditResourceType() {
			return "INQUIRY";
		}

		@Override
		public String auditResourceId() {
			return this.inquiryId.toString();
		}

		@Override
		public @Nullable UUID actorId() {
			return null;
		}

		@Override
		public Map<String, String> auditDetails() {
			return Map.of("folio", this.folio, "reason", this.reason);
		}

		@Override
		public String toString() {
			return "InquiryReceived[folio=" + this.folio + "]";
		}

	}

	/** Respuesta del personal a una consulta: se envía por correo a quien la presentó. */
	public record InquiryAnswered(UUID inquiryId, String folio, String fullName, String email, String responseText,
			Instant occurredAt) implements DomainEvent {

		@Override
		public String toString() {
			return "InquiryAnswered[folio=" + this.folio + "]";
		}

	}

	public record InquiryStatusChanged(UUID inquiryId, String folio, String fromStatus, String toStatus,
			@Nullable UUID assigneeId, @Nullable UUID actorId, Instant occurredAt) implements AuditableEvent {

		@Override
		public String auditAction() {
			return "INQUIRY_" + this.toStatus;
		}

		@Override
		public String auditResourceType() {
			return "INQUIRY";
		}

		@Override
		public String auditResourceId() {
			return this.inquiryId.toString();
		}

		@Override
		public Map<String, String> auditDetails() {
			return Map.of("folio", this.folio, "from", this.fromStatus);
		}

	}

}
