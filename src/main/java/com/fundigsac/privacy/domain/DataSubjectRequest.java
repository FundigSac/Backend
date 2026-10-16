package com.fundigsac.privacy.domain;

import java.time.Instant;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import com.fundigsac.privacy.api.PrivacyEvents.DataSubjectRequestReceived;
import com.fundigsac.privacy.api.PrivacyEvents.DataSubjectRequestUpdated;
import com.fundigsac.shared.domain.AggregateRoot;
import com.fundigsac.shared.domain.ConflictException;
import com.fundigsac.shared.domain.Email;
import com.fundigsac.shared.domain.Folio;
import com.fundigsac.shared.domain.Guard;
import com.fundigsac.shared.domain.IdentityDocument;

/** Solicitud de derechos del titular de datos (ARCO) o de eliminación de datos, con folio ARC. */
public final class DataSubjectRequest extends AggregateRoot {

	public enum Type {
		ACCESS, RECTIFICATION, CANCELLATION, OPPOSITION, ACCOUNT_DELETION
	}

	public enum Status {
		RECEIVED, IN_PROGRESS, RESOLVED, REJECTED
	}

	private final UUID id;

	private final Folio folio;

	private final Type type;

	private final String fullName;

	private final Email email;

	private final @Nullable IdentityDocument document;

	private final String details;

	private Status status;

	private @Nullable String resolutionNote;

	private final Instant receivedAt;

	private @Nullable Instant resolvedAt;

	private @Nullable UUID resolvedBy;

	private Instant updatedAt;

	private final long version;

	private DataSubjectRequest(UUID id, Folio folio, Type type, String fullName, Email email,
			@Nullable IdentityDocument document, String details, Status status, @Nullable String resolutionNote,
			Instant receivedAt, @Nullable Instant resolvedAt, @Nullable UUID resolvedBy, Instant updatedAt, long version) {
		this.id = id;
		this.folio = folio;
		this.type = type;
		this.fullName = fullName;
		this.email = email;
		this.document = document;
		this.details = details;
		this.status = status;
		this.resolutionNote = resolutionNote;
		this.receivedAt = receivedAt;
		this.resolvedAt = resolvedAt;
		this.resolvedBy = resolvedBy;
		this.updatedAt = updatedAt;
		this.version = version;
	}

	public static DataSubjectRequest receive(UUID id, Folio folio, Type type, String fullName, Email email,
			@Nullable IdentityDocument document, String details, Instant now) {
		DataSubjectRequest request = new DataSubjectRequest(id, folio, Guard.notNull("type", type),
				Guard.text("fullName", fullName, 2, 160), Guard.notNull("email", email), document,
				Guard.multiline("details", details, 10, 4000), Status.RECEIVED, null, now, null, null, now, 0);
		request.register(new DataSubjectRequestReceived(id, folio.value(), type.name(), request.fullName,
				email.value(), now));
		return request;
	}

	public static DataSubjectRequest rehydrate(UUID id, Folio folio, Type type, String fullName, Email email,
			@Nullable IdentityDocument document, String details, Status status, @Nullable String resolutionNote,
			Instant receivedAt, @Nullable Instant resolvedAt, @Nullable UUID resolvedBy, Instant updatedAt, long version) {
		return new DataSubjectRequest(id, folio, type, fullName, email, document, details, status, resolutionNote, receivedAt,
				resolvedAt, resolvedBy, updatedAt, version);
	}

	public void start(UUID actorId, Instant now) {
		if (this.status != Status.RECEIVED) {
			throw new ConflictException("INVALID_TRANSITION", "La solicitud ya está en trámite o resuelta.");
		}
		this.status = Status.IN_PROGRESS;
		this.updatedAt = now;
		register(new DataSubjectRequestUpdated(this.id, this.folio.value(), this.status.name(), this.fullName,
				this.email.value(), null, actorId, now));
	}

	/** Resuelve (atendida) o rechaza (con fundamento); en ambos casos se informa al titular. */
	public void conclude(boolean accepted, String note, UUID actorId, Instant now) {
		if (this.status == Status.RESOLVED || this.status == Status.REJECTED) {
			throw new ConflictException("INVALID_TRANSITION", "La solicitud ya fue resuelta.");
		}
		this.resolutionNote = Guard.multiline("resolutionNote", note, 10, 4000);
		this.status = accepted ? Status.RESOLVED : Status.REJECTED;
		this.resolvedAt = now;
		this.resolvedBy = actorId;
		this.updatedAt = now;
		register(new DataSubjectRequestUpdated(this.id, this.folio.value(), this.status.name(), this.fullName,
				this.email.value(), this.resolutionNote, actorId, now));
	}

	public UUID id() {
		return this.id;
	}

	public Folio folio() {
		return this.folio;
	}

	public Type type() {
		return this.type;
	}

	public String fullName() {
		return this.fullName;
	}

	public Email email() {
		return this.email;
	}

	public @Nullable IdentityDocument document() {
		return this.document;
	}

	public String details() {
		return this.details;
	}

	public Status status() {
		return this.status;
	}

	public @Nullable String resolutionNote() {
		return this.resolutionNote;
	}

	public Instant receivedAt() {
		return this.receivedAt;
	}

	public @Nullable Instant resolvedAt() {
		return this.resolvedAt;
	}

	public @Nullable UUID resolvedBy() {
		return this.resolvedBy;
	}

	public Instant updatedAt() {
		return this.updatedAt;
	}

	public long version() {
		return this.version;
	}

}
