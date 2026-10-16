package com.fundigsac.privacy.application;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fundigsac.privacy.api.PrivacyEvents.LegalNoticePublished;
import com.fundigsac.privacy.domain.ConsentRecord;
import com.fundigsac.privacy.domain.DataSubjectRequest;
import com.fundigsac.privacy.domain.LegalNotice;
import com.fundigsac.privacy.domain.PrivacyRepository;
import com.fundigsac.shared.application.CurrentActor;
import com.fundigsac.shared.application.DomainEvents;
import com.fundigsac.shared.application.FolioGenerator;
import com.fundigsac.shared.application.Idempotency;
import com.fundigsac.shared.application.PageQuery;
import com.fundigsac.shared.application.PageResult;
import com.fundigsac.shared.domain.ConflictException;
import com.fundigsac.shared.domain.Email;
import com.fundigsac.shared.domain.IdentityDocument;
import com.fundigsac.shared.domain.Ids;
import com.fundigsac.shared.domain.NotFoundException;
import com.fundigsac.shared.domain.SecureTokens;
import com.fundigsac.shared.domain.StaleVersionException;
import com.fundigsac.shared.domain.ValidationException;
import com.fundigsac.shared.security.Authz;

/** Textos legales vigentes, evidencia de consentimiento y solicitudes de derechos del titular. */
@Service
public class PrivacyService {

	public record NoticeView(String type, String version, String title, @Nullable String summary, String contentUrl,
			@Nullable String body, @Nullable String contentSha256, Instant effectiveFrom, Instant publishedAt) {

		static NoticeView of(LegalNotice n) {
			return new NoticeView(n.type().slug(), n.version(), n.title(), n.summary(), n.contentUrl(), n.body(),
					n.contentSha256(), n.effectiveFrom(), n.publishedAt());
		}

	}

	public record PublishNotice(String version, String title, @Nullable String summary, String contentUrl,
			@Nullable String body, Instant effectiveFrom) {
	}

	public record ConsentReceipt(UUID consentId, String policyVersion, Instant recordedAt) {
	}

	public record SubmitRequest(String type, String fullName, String email, @Nullable String documentType,
			@Nullable String documentNumber, String details) {
	}

	public record RequestReceipt(String folio, String status, Instant receivedAt) {
	}

	public record RequestView(UUID id, String folio, String type, String status, String fullName, String email,
			@Nullable String document, String details, @Nullable String resolutionNote, Instant receivedAt,
			@Nullable Instant resolvedAt, @Nullable UUID resolvedBy, long version) {

		static RequestView of(DataSubjectRequest r) {
			return new RequestView(r.id(), r.folio().value(), r.type().name(), r.status().name(), r.fullName(),
					r.email().value(), r.document() == null ? null : r.document().type() + " " + r.document().number(),
					r.details(), r.resolutionNote(), r.receivedAt(), r.resolvedAt(), r.resolvedBy(), r.version());
		}

	}

	private final PrivacyRepository repository;

	private final FolioGenerator folios;

	private final Idempotency idempotency;

	private final CurrentActor actor;

	private final DomainEvents events;

	private final Clock clock;

	PrivacyService(PrivacyRepository repository, FolioGenerator folios, Idempotency idempotency, CurrentActor actor,
			DomainEvents events, Clock clock) {
		this.repository = repository;
		this.folios = folios;
		this.idempotency = idempotency;
		this.actor = actor;
		this.events = events;
		this.clock = clock;
	}

	// ----- textos legales -----

	@Transactional(readOnly = true)
	public NoticeView currentNotice(String typeSlug) {
		LegalNotice.Type type = LegalNotice.Type.fromSlug(typeSlug);
		return this.repository.currentNotice(type, this.clock.instant())
			.map(NoticeView::of)
			.orElseThrow(() -> new NotFoundException("LEGAL_NOTICE_NOT_FOUND", "No hay una versión vigente publicada."));
	}

	@Transactional(readOnly = true)
	public List<NoticeView> noticeVersions(String typeSlug) {
		return this.repository.noticeVersions(LegalNotice.Type.fromSlug(typeSlug)).stream().map(NoticeView::of).toList();
	}

	@PreAuthorize(Authz.LEGAL_PUBLISH)
	@Transactional
	public NoticeView publishNotice(String typeSlug, PublishNotice command) {
		LegalNotice.Type type = LegalNotice.Type.fromSlug(typeSlug);
		if (this.repository.noticeVersionExists(type, command.version())) {
			throw new ConflictException("VERSION_EXISTS", "Esa versión ya está publicada; use un número nuevo.");
		}
		UUID actorId = this.actor.requireUserId();
		Instant now = this.clock.instant();
		String body = command.body() == null || command.body().isBlank() ? null : command.body();
		LegalNotice notice = new LegalNotice(Ids.newId(), type, command.version(), command.title(), command.summary(),
				command.contentUrl(), body, body == null ? null : SecureTokens.sha256(body.strip()), command.effectiveFrom(),
				actorId, now);
		this.repository.insertNotice(notice);
		this.events.publish(new LegalNoticePublished(notice.id(), type.name(), notice.version(), actorId, now));
		return NoticeView.of(notice);
	}

	// ----- consentimiento -----

	/** Registra la decisión. La versión debe ser una política de cookies publicada (si ya hay alguna registrada). */
	@Transactional
	public ConsentReceipt recordConsent(@Nullable UUID consentId, Map<String, Boolean> choices, String policyVersion,
			String source) {
		boolean anyPolicy = !this.repository.noticeVersions(LegalNotice.Type.COOKIE_POLICY).isEmpty();
		if (anyPolicy && !this.repository.noticeVersionExists(LegalNotice.Type.COOKIE_POLICY, policyVersion)) {
			throw new ValidationException("policyVersion", "UNKNOWN_POLICY_VERSION", "La versión de la política de cookies no existe.");
		}
		ConsentRecord record = new ConsentRecord(Ids.newId(), consentId == null ? Ids.newId() : consentId, choices,
				policyVersion, parse(ConsentRecord.Source.class, "source", source), this.clock.instant());
		this.repository.insertConsent(record);
		return new ConsentReceipt(record.consentId(), record.policyVersion(), record.recordedAt());
	}

	// ----- derechos del titular -----

	@Transactional
	public RequestReceipt submitRequest(SubmitRequest command, @Nullable String idempotencyKey) {
		return this.idempotency.execute("privacy-request", idempotencyKey, command, () -> {
			IdentityDocument document = command.documentType() == null || command.documentType().isBlank() ? null
					: IdentityDocument.of(parse(IdentityDocument.Type.class, "documentType", command.documentType()),
							command.documentNumber());
			DataSubjectRequest request = DataSubjectRequest.receive(Ids.newId(), this.folios.next("ARC"),
					parse(DataSubjectRequest.Type.class, "type", command.type()), command.fullName(),
					Email.of("email", command.email()), document, command.details(), this.clock.instant());
			this.repository.insertRequest(request);
			this.events.publishFrom(request);
			return receipt(request);
		}, RequestReceipt::folio, (folio) -> receipt(this.repository.findRequestByFolio(folio).orElseThrow()));
	}

	@PreAuthorize(Authz.PRIVACY_READ)
	@Transactional(readOnly = true)
	public PageResult<RequestView> requests(@Nullable String status, PageQuery page) {
		String s = status == null || status.isBlank() ? null : parse(DataSubjectRequest.Status.class, "status", status).name();
		var items = this.repository.searchRequests(s, page.size(), page.offset()).stream().map(RequestView::of).toList();
		return PageResult.of(items, page, this.repository.countRequests(s));
	}

	@PreAuthorize(Authz.PRIVACY_READ)
	@Transactional(readOnly = true)
	public RequestView request(UUID id) {
		return RequestView.of(load(id));
	}

	@PreAuthorize(Authz.PRIVACY_MANAGE)
	@Transactional
	public RequestView startRequest(UUID id, long expectedVersion) {
		DataSubjectRequest request = loadForUpdate(id, expectedVersion);
		request.start(this.actor.requireUserId(), this.clock.instant());
		return save(request);
	}

	@PreAuthorize(Authz.PRIVACY_MANAGE)
	@Transactional
	public RequestView concludeRequest(UUID id, boolean accepted, String note, long expectedVersion) {
		DataSubjectRequest request = loadForUpdate(id, expectedVersion);
		request.conclude(accepted, note, this.actor.requireUserId(), this.clock.instant());
		return save(request);
	}

	private RequestView save(DataSubjectRequest request) {
		this.repository.updateRequest(request);
		this.events.publishFrom(request);
		return RequestView.of(load(request.id()));
	}

	private DataSubjectRequest loadForUpdate(UUID id, long expectedVersion) {
		DataSubjectRequest request = load(id);
		if (request.version() != expectedVersion) {
			throw new StaleVersionException();
		}
		return request;
	}

	private DataSubjectRequest load(UUID id) {
		return this.repository.findRequest(id)
			.orElseThrow(() -> new NotFoundException("REQUEST_NOT_FOUND", "La solicitud no existe."));
	}

	private static RequestReceipt receipt(DataSubjectRequest r) {
		return new RequestReceipt(r.folio().value(), r.status().name(), r.receivedAt());
	}

	private static <E extends Enum<E>> E parse(Class<E> type, String field, @Nullable String value) {
		try {
			return Enum.valueOf(type, value == null ? "" : value.strip().toUpperCase(Locale.ROOT));
		}
		catch (IllegalArgumentException ex) {
			throw new ValidationException(field, "INVALID_VALUE", "Valor no válido.");
		}
	}

}
