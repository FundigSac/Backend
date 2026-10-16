package com.fundigsac.complaint.api;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import com.fundigsac.shared.domain.AuditableEvent;

/** Eventos del Libro de Reclamaciones. */
public final class ComplaintEvents {

	private ComplaintEvents() {
	}

	/** Hoja registrada: constancia al consumidor y aviso a Atención Legal. */
	public record ComplaintFiled(UUID complaintId, String folio, String kind, String consumerName, String email,
			LocalDate responseDueOn, Instant occurredAt) implements AuditableEvent {

		@Override
		public String auditAction() {
			return "COMPLAINT_FILED";
		}

		@Override
		public String auditResourceType() {
			return "COMPLAINT";
		}

		@Override
		public String auditResourceId() {
			return this.complaintId.toString();
		}

		@Override
		public @Nullable UUID actorId() {
			return null;
		}

		@Override
		public Map<String, String> auditDetails() {
			return Map.of("folio", this.folio, "kind", this.kind, "dueOn", this.responseDueOn.toString());
		}

		@Override
		public String toString() {
			return "ComplaintFiled[folio=" + this.folio + "]";
		}

	}

	/** Respuesta emitida: se envía al consumidor por el medio que eligió. */
	public record ComplaintAnswered(UUID complaintId, String folio, String consumerName, String email,
			String responseChannel, String responseText, boolean late, UUID actorId, Instant occurredAt)
			implements AuditableEvent {

		@Override
		public String auditAction() {
			return "COMPLAINT_ANSWERED";
		}

		@Override
		public String auditResourceType() {
			return "COMPLAINT";
		}

		@Override
		public String auditResourceId() {
			return this.complaintId.toString();
		}

		@Override
		public Map<String, String> auditDetails() {
			return Map.of("folio", this.folio, "late", Boolean.toString(this.late), "channel", this.responseChannel);
		}

		@Override
		public String toString() {
			return "ComplaintAnswered[folio=" + this.folio + "]";
		}

	}

	public record ComplaintUpdated(UUID complaintId, String folio, String eventType, @Nullable UUID assigneeId,
			UUID actorId, Instant occurredAt) implements AuditableEvent {

		@Override
		public String auditAction() {
			return "COMPLAINT_" + this.eventType;
		}

		@Override
		public String auditResourceType() {
			return "COMPLAINT";
		}

		@Override
		public String auditResourceId() {
			return this.complaintId.toString();
		}

		@Override
		public Map<String, String> auditDetails() {
			return this.assigneeId == null ? Map.of("folio", this.folio)
					: Map.of("folio", this.folio, "assignee", this.assigneeId.toString());
		}

	}

}
