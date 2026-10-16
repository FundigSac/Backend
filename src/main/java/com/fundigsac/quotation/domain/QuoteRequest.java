package com.fundigsac.quotation.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import com.fundigsac.quotation.api.QuoteEvents.QuoteStatusChanged;
import com.fundigsac.quotation.api.QuoteEvents.QuoteSubmitted;
import com.fundigsac.shared.domain.AggregateRoot;
import com.fundigsac.shared.domain.ConflictException;
import com.fundigsac.shared.domain.Folio;
import com.fundigsac.shared.domain.Guard;
import com.fundigsac.shared.domain.Ids;
import com.fundigsac.shared.domain.RuleViolationException;

/**
 * Solicitud de cotización (agregado). Las líneas y los datos del solicitante son inmutables una vez registrados; solo
 * cambian el estado de atención y el responsable, siempre con historial.
 */
public final class QuoteRequest extends AggregateRoot {

	public static final int MAX_ITEMS = 50;

	private final UUID id;

	private final Folio folio;

	private final QuoteOrigin origin;

	private QuoteStatus status;

	private final Requester requester;

	private final ProjectDetails project;

	private final List<QuoteItem> items;

	private final String privacyNoticeVersion;

	private final @Nullable UUID customerId;

	private @Nullable UUID assigneeId;

	private @Nullable String statusReason;

	private final Instant submittedAt;

	private Instant updatedAt;

	private final long version;

	private final List<StatusChange> newHistory = new ArrayList<>();

	private QuoteRequest(UUID id, Folio folio, QuoteOrigin origin, QuoteStatus status, Requester requester,
			ProjectDetails project, List<QuoteItem> items, String privacyNoticeVersion, @Nullable UUID customerId,
			@Nullable UUID assigneeId, @Nullable String statusReason, Instant submittedAt, Instant updatedAt,
			long version) {
		this.id = id;
		this.folio = folio;
		this.origin = origin;
		this.status = status;
		this.requester = requester;
		this.project = project;
		this.items = List.copyOf(items);
		this.privacyNoticeVersion = privacyNoticeVersion;
		this.customerId = customerId;
		this.assigneeId = assigneeId;
		this.statusReason = statusReason;
		this.submittedAt = submittedAt;
		this.updatedAt = updatedAt;
		this.version = version;
	}

	/** Registra una solicitud. Se admite una cotización general sin productos si describe la necesidad. */
	public static QuoteRequest submit(UUID id, Folio folio, QuoteOrigin origin, Requester requester,
			ProjectDetails project, List<QuoteItem> items, String privacyNoticeVersion, @Nullable UUID customerId,
			Instant now) {
		Guard.maxSize("items", items, MAX_ITEMS);
		if (items.isEmpty() && (project.message() == null || project.message().length() < 10)) {
			throw new RuleViolationException("EMPTY_QUOTE",
					"Agregue al menos un producto o describa su necesidad en el mensaje (mínimo 10 caracteres).");
		}
		project.checkDeliveryDate(com.fundigsac.shared.domain.PeruTime.dateOf(now));
		QuoteRequest quote = new QuoteRequest(id, folio, origin, QuoteStatus.RECEIVED, requester, project, items,
				Guard.text("privacyNoticeVersion", privacyNoticeVersion, 1, 20), customerId, null, null, now, now, 0);
		quote.newHistory.add(new StatusChange(Ids.newId(), null, QuoteStatus.RECEIVED, null, customerId, now));
		quote.register(new QuoteSubmitted(id, folio.value(), origin.name(), requester.companyName(),
				requester.contactName(), requester.email().value(), items.size(), customerId, now));
		return quote;
	}

	public static QuoteRequest rehydrate(UUID id, Folio folio, QuoteOrigin origin, QuoteStatus status,
			Requester requester, ProjectDetails project, List<QuoteItem> items, String privacyNoticeVersion,
			@Nullable UUID customerId, @Nullable UUID assigneeId, @Nullable String statusReason, Instant submittedAt,
			Instant updatedAt, long version) {
		return new QuoteRequest(id, folio, origin, status, requester, project, items, privacyNoticeVersion, customerId,
				assigneeId, statusReason, submittedAt, updatedAt, version);
	}

	/** Asigna (o reasigna) el responsable de Ventas. Una solicitud recién recibida pasa a ASSIGNED. */
	public void assign(UUID assignee, UUID actorId, Instant now) {
		if (this.status.isFinal() || this.status == QuoteStatus.RESPONDED) {
			throw new ConflictException("INVALID_TRANSITION", "No se reasigna una solicitud respondida, cerrada o cancelada.");
		}
		this.assigneeId = assignee;
		QuoteStatus to = this.status == QuoteStatus.RECEIVED ? QuoteStatus.ASSIGNED : this.status;
		apply(to, "Asignada", actorId, now);
	}

	public void changeStatus(QuoteStatus to, @Nullable String note, UUID actorId, Instant now) {
		if (!this.status.next().contains(to)) {
			throw new ConflictException("INVALID_TRANSITION",
					"No se puede pasar de " + this.status + " a " + to + ".");
		}
		String clean = Guard.optionalMultiline("note", note, 1000);
		if (to == QuoteStatus.CANCELLED && (clean == null || clean.length() < 5)) {
			throw new RuleViolationException("REASON_REQUIRED", "Indique el motivo de la cancelación.");
		}
		if (to == QuoteStatus.CANCELLED) {
			this.statusReason = clean.length() > 500 ? clean.substring(0, 500) : clean;
		}
		apply(to, clean, actorId, now);
	}

	private void apply(QuoteStatus to, @Nullable String note, UUID actorId, Instant now) {
		QuoteStatus from = this.status;
		this.status = to;
		this.updatedAt = now;
		this.newHistory.add(new StatusChange(Ids.newId(), from, to, note, actorId, now));
		register(new QuoteStatusChanged(this.id, this.folio.value(), from.name(), to.name(), this.assigneeId, note, actorId, now));
	}

	/** Historial nuevo pendiente de guardar (lo consume el repositorio). */
	public List<StatusChange> drainNewHistory() {
		List<StatusChange> pending = List.copyOf(this.newHistory);
		this.newHistory.clear();
		return pending;
	}

	public boolean belongsTo(UUID customer) {
		return customer.equals(this.customerId);
	}

	public UUID id() {
		return this.id;
	}

	public Folio folio() {
		return this.folio;
	}

	public QuoteOrigin origin() {
		return this.origin;
	}

	public QuoteStatus status() {
		return this.status;
	}

	public Requester requester() {
		return this.requester;
	}

	public ProjectDetails project() {
		return this.project;
	}

	public List<QuoteItem> items() {
		return this.items;
	}

	public String privacyNoticeVersion() {
		return this.privacyNoticeVersion;
	}

	public @Nullable UUID customerId() {
		return this.customerId;
	}

	public @Nullable UUID assigneeId() {
		return this.assigneeId;
	}

	public @Nullable String statusReason() {
		return this.statusReason;
	}

	public Instant submittedAt() {
		return this.submittedAt;
	}

	public Instant updatedAt() {
		return this.updatedAt;
	}

	public long version() {
		return this.version;
	}

}
