package com.fundigsac.inquiry.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import com.fundigsac.inquiry.api.InquiryEvents.InquiryAnswered;
import com.fundigsac.inquiry.api.InquiryEvents.InquiryReceived;
import com.fundigsac.inquiry.api.InquiryEvents.InquiryStatusChanged;
import com.fundigsac.shared.domain.AggregateRoot;
import com.fundigsac.shared.domain.ConflictException;
import com.fundigsac.shared.domain.Email;
import com.fundigsac.shared.domain.Folio;
import com.fundigsac.shared.domain.Guard;
import com.fundigsac.shared.domain.Ids;
import com.fundigsac.shared.domain.PhoneNumber;
import com.fundigsac.shared.domain.Ruc;

/** Consulta de contacto (agregado). Lo enviado no cambia; solo el estado de atención y el responsable. */
public final class Inquiry extends AggregateRoot {

	public enum Reason {
		GENERAL, TECHNICAL, COMMERCIAL, DOCUMENTATION, MEETING, AFTER_SALES, OTHER
	}

	public enum Status {

		RECEIVED, IN_PROGRESS, ANSWERED, CLOSED;

		Set<Status> next() {
			return switch (this) {
				case RECEIVED -> EnumSet.of(IN_PROGRESS, ANSWERED, CLOSED);
				case IN_PROGRESS -> EnumSet.of(ANSWERED, CLOSED);
				case ANSWERED -> EnumSet.of(CLOSED, IN_PROGRESS);
				case CLOSED -> EnumSet.noneOf(Status.class);
			};
		}

	}

	/** Datos de quien consulta. */
	public record Sender(String fullName, String companyName, @Nullable Ruc ruc, @Nullable String position, Email email,
			PhoneNumber phone) {

		public Sender {
			fullName = Guard.text("fullName", fullName, 2, 120);
			companyName = Guard.text("companyName", companyName, 2, 160);
			position = Guard.optionalText("position", position, 120);
			Guard.notNull("email", email);
			Guard.notNull("phone", phone);
		}

	}

	public record Event(UUID id, @Nullable Status from, Status to, @Nullable String note, @Nullable UUID actorId,
			Instant occurredAt) {
	}

	private final UUID id;

	private final Folio folio;

	private final Reason reason;

	private final Sender sender;

	private final String message;

	private final @Nullable String requestedDocument;

	private final String privacyNoticeVersion;

	private Status status;

	private @Nullable UUID assigneeId;

	private final Instant receivedAt;

	private Instant updatedAt;

	private final long version;

	private final List<Event> newEvents = new ArrayList<>();

	private Inquiry(UUID id, Folio folio, Reason reason, Sender sender, String message, @Nullable String requestedDocument,
			String privacyNoticeVersion, Status status, @Nullable UUID assigneeId, Instant receivedAt, Instant updatedAt,
			long version) {
		this.id = id;
		this.folio = folio;
		this.reason = reason;
		this.sender = sender;
		this.message = message;
		this.requestedDocument = requestedDocument;
		this.privacyNoticeVersion = privacyNoticeVersion;
		this.status = status;
		this.assigneeId = assigneeId;
		this.receivedAt = receivedAt;
		this.updatedAt = updatedAt;
		this.version = version;
	}

	public static Inquiry receive(UUID id, Folio folio, Reason reason, Sender sender, String message,
			@Nullable String requestedDocument, String privacyNoticeVersion, Instant now) {
		Inquiry inquiry = new Inquiry(id, folio, Guard.notNull("reason", reason), sender,
				Guard.multiline("message", message, 10, 4000), Guard.optionalText("requestedDocument", requestedDocument, 200),
				Guard.text("privacyNoticeVersion", privacyNoticeVersion, 1, 20), Status.RECEIVED, null, now, now, 0);
		inquiry.newEvents.add(new Event(Ids.newId(), null, Status.RECEIVED, null, null, now));
		inquiry.register(new InquiryReceived(id, folio.value(), reason.name(), sender.fullName(), sender.companyName(),
				sender.email().value(), now));
		return inquiry;
	}

	public static Inquiry rehydrate(UUID id, Folio folio, Reason reason, Sender sender, String message,
			@Nullable String requestedDocument, String privacyNoticeVersion, Status status, @Nullable UUID assigneeId,
			Instant receivedAt, Instant updatedAt, long version) {
		return new Inquiry(id, folio, reason, sender, message, requestedDocument, privacyNoticeVersion, status, assigneeId,
				receivedAt, updatedAt, version);
	}

	public void assign(UUID assignee, UUID actorId, Instant now) {
		if (this.status == Status.CLOSED) {
			throw new ConflictException("INVALID_TRANSITION", "La consulta está cerrada.");
		}
		this.assigneeId = assignee;
		Status to = this.status == Status.RECEIVED ? Status.IN_PROGRESS : this.status;
		apply(to, "Asignada", actorId, now);
	}

	public void changeStatus(Status to, @Nullable String note, UUID actorId, Instant now) {
		if (!this.status.next().contains(to)) {
			throw new ConflictException("INVALID_TRANSITION", "No se puede pasar de " + this.status + " a " + to + ".");
		}
		apply(to, Guard.optionalMultiline("note", note, 1000), actorId, now);
	}

	private void apply(Status to, @Nullable String note, UUID actorId, Instant now) {
		Status from = this.status;
		this.status = to;
		this.updatedAt = now;
		this.newEvents.add(new Event(Ids.newId(), from, to, note, actorId, now));
		register(new InquiryStatusChanged(this.id, this.folio.value(), from.name(), to.name(), this.assigneeId, actorId, now));
		// La nota al marcarla como respondida es la respuesta: se envía por correo a quien escribió.
		if (to == Status.ANSWERED && note != null) {
			register(new InquiryAnswered(this.id, this.folio.value(), this.sender.fullName(), this.sender.email().value(), note,
					now));
		}
	}

	public List<Event> drainNewEvents() {
		List<Event> pending = List.copyOf(this.newEvents);
		this.newEvents.clear();
		return pending;
	}

	public UUID id() {
		return this.id;
	}

	public Folio folio() {
		return this.folio;
	}

	public Reason reason() {
		return this.reason;
	}

	public Sender sender() {
		return this.sender;
	}

	public String message() {
		return this.message;
	}

	public @Nullable String requestedDocument() {
		return this.requestedDocument;
	}

	public String privacyNoticeVersion() {
		return this.privacyNoticeVersion;
	}

	public Status status() {
		return this.status;
	}

	public @Nullable UUID assigneeId() {
		return this.assigneeId;
	}

	public Instant receivedAt() {
		return this.receivedAt;
	}

	public Instant updatedAt() {
		return this.updatedAt;
	}

	public long version() {
		return this.version;
	}

}
