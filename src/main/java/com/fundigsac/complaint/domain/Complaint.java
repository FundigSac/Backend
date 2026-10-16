package com.fundigsac.complaint.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import com.fundigsac.complaint.api.ComplaintEvents.ComplaintAnswered;
import com.fundigsac.complaint.api.ComplaintEvents.ComplaintFiled;
import com.fundigsac.complaint.api.ComplaintEvents.ComplaintUpdated;
import com.fundigsac.shared.domain.AggregateRoot;
import com.fundigsac.shared.domain.ConflictException;
import com.fundigsac.shared.domain.Email;
import com.fundigsac.shared.domain.Folio;
import com.fundigsac.shared.domain.Guard;
import com.fundigsac.shared.domain.IdentityDocument;
import com.fundigsac.shared.domain.Ids;
import com.fundigsac.shared.domain.Money;
import com.fundigsac.shared.domain.PeruTime;
import com.fundigsac.shared.domain.PhoneNumber;
import com.fundigsac.shared.domain.RuleViolationException;
import com.fundigsac.shared.domain.ValidationException;

/**
 * Hoja de reclamación (agregado). Lo presentado por el consumidor es inmutable; la empresa solo asigna, anota,
 * responde (una vez) y cierra. Reclamo (CLAIM): disconformidad con el producto o servicio. Queja (GRIEVANCE): malestar
 * por la atención recibida.
 */
public final class Complaint extends AggregateRoot {

	public enum Kind {
		CLAIM, GRIEVANCE
	}

	public enum GoodType {
		PRODUCT, SERVICE
	}

	public enum ResponseChannel {
		EMAIL, POSTAL_ADDRESS
	}

	public enum Status {
		FILED, IN_REVIEW, ANSWERED, CLOSED
	}

	public enum EventType {
		FILED, ASSIGNED, IN_REVIEW, ANSWERED, CLOSED, NOTE
	}

	/** Consumidor que presenta la hoja. */
	public record Consumer(String fullName, IdentityDocument document, Email email, PhoneNumber phone, String address) {

		public Consumer {
			fullName = Guard.text("consumer.fullName", fullName, 2, 160);
			Guard.notNull("consumer.document", document);
			Guard.notNull("consumer.email", email);
			Guard.notNull("consumer.phone", phone);
			address = Guard.text("consumer.address", address, 5, 300);
		}

	}

	/** Padre, madre o representante cuando quien reclama es menor de edad. */
	public record Guardian(String fullName, IdentityDocument document) {

		public Guardian {
			fullName = Guard.text("guardian.fullName", fullName, 2, 160);
			Guard.notNull("guardian.document", document);
			if (document.type() == IdentityDocument.Type.RUC) {
				throw new ValidationException("guardian.documentType", "INVALID_DOCUMENT", "El representante se identifica con DNI, CE o pasaporte.");
			}
		}

	}

	/** Bien contratado y detalle de lo sucedido. */
	public record Details(@Nullable GoodType goodType, String goodDescription, @Nullable Money claimedAmount,
			@Nullable String serviceLocation, LocalDate incidentDate, @Nullable String relatedReference, String facts,
			String consumerRequest) {

		public Details {
			goodDescription = Guard.text("goodDescription", goodDescription, 5, 1000);
			serviceLocation = Guard.optionalText("serviceLocation", serviceLocation, 200);
			Guard.notNull("incidentDate", incidentDate);
			relatedReference = Guard.optionalText("relatedReference", relatedReference, 60);
			facts = Guard.multiline("facts", facts, 20, 4000);
			consumerRequest = Guard.multiline("consumerRequest", consumerRequest, 5, 2000);
		}

	}

	public record Event(UUID id, EventType type, @Nullable String note, @Nullable UUID actorId, Instant occurredAt) {
	}

	private final UUID id;

	private final Folio folio;

	private final Kind kind;

	private final Consumer consumer;

	private final @Nullable Guardian guardian;

	private final Details details;

	private final ResponseChannel responseChannel;

	private final String privacyNoticeVersion;

	private Status status;

	private final Instant filedAt;

	private final LocalDate responseDueOn;

	private @Nullable UUID assigneeId;

	private @Nullable String responseText;

	private @Nullable Instant respondedAt;

	private @Nullable UUID respondedBy;

	private @Nullable Instant closedAt;

	private Instant updatedAt;

	private final long version;

	private final List<Event> newEvents = new ArrayList<>();

	private Complaint(UUID id, Folio folio, Kind kind, Consumer consumer, @Nullable Guardian guardian, Details details,
			ResponseChannel responseChannel, String privacyNoticeVersion, Status status, Instant filedAt,
			LocalDate responseDueOn, @Nullable UUID assigneeId, @Nullable String responseText,
			@Nullable Instant respondedAt, @Nullable UUID respondedBy, @Nullable Instant closedAt, Instant updatedAt,
			long version) {
		this.id = id;
		this.folio = folio;
		this.kind = kind;
		this.consumer = consumer;
		this.guardian = guardian;
		this.details = details;
		this.responseChannel = responseChannel;
		this.privacyNoticeVersion = privacyNoticeVersion;
		this.status = status;
		this.filedAt = filedAt;
		this.responseDueOn = responseDueOn;
		this.assigneeId = assigneeId;
		this.responseText = responseText;
		this.respondedAt = respondedAt;
		this.respondedBy = respondedBy;
		this.closedAt = closedAt;
		this.updatedAt = updatedAt;
		this.version = version;
	}

	public static Complaint file(UUID id, Folio folio, Kind kind, Consumer consumer, boolean minor,
			@Nullable Guardian guardian, Details details, ResponseChannel channel, String privacyNoticeVersion,
			ResponseDeadlinePolicy deadline, Instant now) {
		Guard.notNull("kind", kind);
		Guard.notNull("responseChannel", channel);
		if (minor && guardian == null) {
			throw new ValidationException("guardian", "REQUIRED", "Si es menor de edad, indique los datos de su padre, madre o representante.");
		}
		if (kind == Kind.CLAIM && details.goodType() == null) {
			throw new ValidationException("goodType", "REQUIRED", "Indique si el reclamo es sobre un producto o un servicio.");
		}
		LocalDate today = PeruTime.dateOf(now);
		if (details.incidentDate().isAfter(today) || details.incidentDate().isBefore(today.minusYears(5))) {
			throw new ValidationException("incidentDate", "INVALID_DATE", "La fecha del suceso no puede ser futura ni de hace más de 5 años.");
		}
		Complaint complaint = new Complaint(id, folio, kind, consumer, minor ? guardian : null, details, channel,
				Guard.text("privacyNoticeVersion", privacyNoticeVersion, 1, 20), Status.FILED, now, deadline.dueDate(today),
				null, null, null, null, null, now, 0);
		complaint.newEvents.add(new Event(Ids.newId(), EventType.FILED, null, null, now));
		complaint.register(new ComplaintFiled(id, folio.value(), kind.name(), consumer.fullName(), consumer.email().value(),
				complaint.responseDueOn, now));
		return complaint;
	}

	public static Complaint rehydrate(UUID id, Folio folio, Kind kind, Consumer consumer, @Nullable Guardian guardian,
			Details details, ResponseChannel channel, String privacyNoticeVersion, Status status, Instant filedAt,
			LocalDate responseDueOn, @Nullable UUID assigneeId, @Nullable String responseText,
			@Nullable Instant respondedAt, @Nullable UUID respondedBy, @Nullable Instant closedAt, Instant updatedAt,
			long version) {
		return new Complaint(id, folio, kind, consumer, guardian, details, channel, privacyNoticeVersion, status, filedAt,
				responseDueOn, assigneeId, responseText, respondedAt, respondedBy, closedAt, updatedAt, version);
	}

	public void assign(UUID assignee, UUID actorId, Instant now) {
		requireOpen();
		this.assigneeId = assignee;
		if (this.status == Status.FILED) {
			this.status = Status.IN_REVIEW;
		}
		record(EventType.ASSIGNED, null, actorId, now);
	}

	public void addNote(String note, UUID actorId, Instant now) {
		record(EventType.NOTE, Guard.multiline("note", note, 3, 1000), actorId, now);
	}

	/** Respuesta formal (una sola vez). Se registra si fue fuera de plazo; nunca se oculta el retraso. */
	public void answer(String text, UUID actorId, Instant now) {
		requireOpen();
		this.responseText = Guard.multiline("response", text, 20, 8000);
		this.respondedAt = now;
		this.respondedBy = actorId;
		this.status = Status.ANSWERED;
		this.updatedAt = now;
		this.newEvents.add(new Event(Ids.newId(), EventType.ANSWERED, null, actorId, now));
		register(new ComplaintAnswered(this.id, this.folio.value(), this.consumer.fullName(), this.consumer.email().value(),
				this.responseChannel.name(), this.responseText, isLate(now), actorId, now));
	}

	public void close(UUID actorId, Instant now) {
		if (this.status != Status.ANSWERED) {
			throw new RuleViolationException("NOT_ANSWERED", "Solo se cierra una hoja ya respondida.");
		}
		this.status = Status.CLOSED;
		this.closedAt = now;
		record(EventType.CLOSED, null, actorId, now);
	}

	public boolean isOverdue(Instant now) {
		return (this.status == Status.FILED || this.status == Status.IN_REVIEW) && PeruTime.dateOf(now).isAfter(this.responseDueOn);
	}

	private boolean isLate(Instant at) {
		return PeruTime.dateOf(at).isAfter(this.responseDueOn);
	}

	private void requireOpen() {
		if (this.status == Status.ANSWERED || this.status == Status.CLOSED) {
			throw new ConflictException("ALREADY_ANSWERED", "La hoja ya fue respondida.");
		}
	}

	private void record(EventType type, @Nullable String note, UUID actorId, Instant now) {
		this.updatedAt = now;
		this.newEvents.add(new Event(Ids.newId(), type, note, actorId, now));
		register(new ComplaintUpdated(this.id, this.folio.value(), type.name(), this.assigneeId, actorId, now));
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

	public Kind kind() {
		return this.kind;
	}

	public Consumer consumer() {
		return this.consumer;
	}

	public @Nullable Guardian guardian() {
		return this.guardian;
	}

	public Details details() {
		return this.details;
	}

	public ResponseChannel responseChannel() {
		return this.responseChannel;
	}

	public String privacyNoticeVersion() {
		return this.privacyNoticeVersion;
	}

	public Status status() {
		return this.status;
	}

	public Instant filedAt() {
		return this.filedAt;
	}

	public LocalDate responseDueOn() {
		return this.responseDueOn;
	}

	public @Nullable UUID assigneeId() {
		return this.assigneeId;
	}

	public @Nullable String responseText() {
		return this.responseText;
	}

	public @Nullable Instant respondedAt() {
		return this.respondedAt;
	}

	public @Nullable UUID respondedBy() {
		return this.respondedBy;
	}

	public @Nullable Instant closedAt() {
		return this.closedAt;
	}

	public Instant updatedAt() {
		return this.updatedAt;
	}

	public long version() {
		return this.version;
	}

}
