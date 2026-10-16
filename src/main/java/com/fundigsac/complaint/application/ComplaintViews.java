package com.fundigsac.complaint.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import com.fundigsac.complaint.domain.Complaint;

/** Vistas del Libro de Reclamaciones. */
public final class ComplaintViews {

	private ComplaintViews() {
	}

	public record AmountView(BigDecimal amount, String currency) {
	}

	public record HistoryView(String type, @Nullable String note, @Nullable UUID actorId, Instant occurredAt) {
	}

	/** Copia de la hoja para el consumidor (constancia) y para Atención Legal. */
	public record ComplaintSheet(UUID id, String folio, String kind, String status, String consumerName, String document,
			String email, String phone, String address, boolean minor, @Nullable String guardianName,
			@Nullable String guardianDocument, @Nullable String goodType, String goodDescription,
			@Nullable AmountView claimedAmount, @Nullable String serviceLocation, LocalDate incidentDate,
			@Nullable String relatedReference, String facts, String consumerRequest, String responseChannel,
			Instant filedAt, LocalDate responseDueOn, boolean overdue, @Nullable String responseText,
			@Nullable Instant respondedAt, @Nullable Instant closedAt, @Nullable UUID assigneeId,
			List<HistoryView> history, long version) {

		static ComplaintSheet of(Complaint c, List<Complaint.Event> events, Instant now, boolean forStaff) {
			var con = c.consumer();
			var d = c.details();
			var g = c.guardian();
			return new ComplaintSheet(c.id(), c.folio().value(), c.kind().name(), c.status().name(), con.fullName(),
					forStaff ? con.document().type() + " " + con.document().number() : con.document().masked(),
					forStaff ? con.email().value() : con.email().masked(), forStaff ? con.phone().value() : con.phone().toString(),
					forStaff ? con.address() : "***", g != null, g == null ? null : g.fullName(),
					g == null ? null : (forStaff ? g.document().type() + " " + g.document().number() : g.document().masked()),
					d.goodType() == null ? null : d.goodType().name(), d.goodDescription(),
					d.claimedAmount() == null ? null
							: new AmountView(d.claimedAmount().amount(), d.claimedAmount().currency().name()),
					d.serviceLocation(), d.incidentDate(), d.relatedReference(), d.facts(), d.consumerRequest(),
					c.responseChannel().name(), c.filedAt(), c.responseDueOn(), c.isOverdue(now), c.responseText(),
					c.respondedAt(), c.closedAt(), forStaff ? c.assigneeId() : null,
					forStaff ? events.stream().map((e) -> new HistoryView(e.type().name(), e.note(), e.actorId(), e.occurredAt())).toList()
							: List.of(),
					forStaff ? c.version() : 0);
		}

	}

	public record ComplaintSummary(UUID id, String folio, String kind, String status, String consumerName, Instant filedAt,
			LocalDate responseDueOn, boolean overdue, @Nullable UUID assigneeId) {
	}

	public record Receipt(String folio, String kind, String status, Instant filedAt, LocalDate responseDueOn,
			String receiptToken) {
	}

}
