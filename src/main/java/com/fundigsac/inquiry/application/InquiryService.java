package com.fundigsac.inquiry.application;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fundigsac.identity.api.StaffDirectory;
import com.fundigsac.inquiry.domain.Inquiry;
import com.fundigsac.inquiry.domain.InquiryRepository;
import com.fundigsac.shared.application.CurrentActor;
import com.fundigsac.shared.application.DomainEvents;
import com.fundigsac.shared.application.FolioGenerator;
import com.fundigsac.shared.application.Idempotency;
import com.fundigsac.shared.application.PageQuery;
import com.fundigsac.shared.application.PageResult;
import com.fundigsac.shared.domain.Email;
import com.fundigsac.shared.domain.Ids;
import com.fundigsac.shared.domain.NotFoundException;
import com.fundigsac.shared.domain.PhoneNumber;
import com.fundigsac.shared.domain.Ruc;
import com.fundigsac.shared.domain.StaleVersionException;
import com.fundigsac.shared.domain.ValidationException;
import com.fundigsac.shared.security.Authz;
import com.fundigsac.shared.security.Permission;

/** Registro público de consultas (folio CON) y su atención por el personal. */
@Service
public class InquiryService {

	public record SubmitInquiry(String reason, String fullName, String companyName, @Nullable String ruc,
			@Nullable String position, String email, String phone, String message, @Nullable String requestedDocument,
			String privacyNoticeVersion) {
	}

	public record Receipt(String folio, String status, Instant receivedAt) {
	}

	public record InquirySummary(UUID id, String folio, String reason, String status, String fullName, String companyName,
			String email, @Nullable UUID assigneeId, Instant receivedAt) {
	}

	public record HistoryEntry(@Nullable String from, String to, @Nullable String note, @Nullable UUID actorId,
			Instant occurredAt) {
	}

	public record InquiryDetail(UUID id, String folio, String reason, String status, String fullName, String companyName,
			@Nullable String ruc, @Nullable String position, String email, String phone, String message,
			@Nullable String requestedDocument, String privacyNoticeVersion, @Nullable UUID assigneeId,
			List<HistoryEntry> history, Instant receivedAt, Instant updatedAt, long version) {
	}

	private final InquiryRepository inquiries;

	private final FolioGenerator folios;

	private final Idempotency idempotency;

	private final StaffDirectory staff;

	private final CurrentActor actor;

	private final DomainEvents events;

	private final Clock clock;

	InquiryService(InquiryRepository inquiries, FolioGenerator folios, Idempotency idempotency, StaffDirectory staff,
			CurrentActor actor, DomainEvents events, Clock clock) {
		this.inquiries = inquiries;
		this.folios = folios;
		this.idempotency = idempotency;
		this.staff = staff;
		this.actor = actor;
		this.events = events;
		this.clock = clock;
	}

	@Transactional
	public Receipt submit(SubmitInquiry command, @Nullable String idempotencyKey) {
		return this.idempotency.execute("inquiry", idempotencyKey, command, () -> register(command), Receipt::folio,
				(folio) -> receipt(this.inquiries.findByFolio(folio).orElseThrow()));
	}

	private Receipt register(SubmitInquiry c) {
		Instant now = this.clock.instant();
		Inquiry.Reason reason = parse(Inquiry.Reason.class, "reason", c.reason());
		Inquiry.Sender sender = new Inquiry.Sender(c.fullName(), c.companyName(), Ruc.optional("ruc", c.ruc()), c.position(),
				Email.of("email", c.email()), PhoneNumber.of("phone", c.phone()));
		Inquiry inquiry = Inquiry.receive(Ids.newId(), this.folios.next("CON"), reason, sender, c.message(),
				c.requestedDocument(), c.privacyNoticeVersion(), now);
		this.inquiries.insert(inquiry);
		this.events.publishFrom(inquiry);
		return receipt(inquiry);
	}

	@PreAuthorize(Authz.INQUIRY_READ)
	@Transactional(readOnly = true)
	public PageResult<InquirySummary> inbox(@Nullable String status, @Nullable String query, PageQuery page) {
		String s = status == null || status.isBlank() ? null : parse(Inquiry.Status.class, "status", status).name();
		String q = query == null || query.isBlank() ? null : query.strip();
		List<InquirySummary> items = this.inquiries.search(s, q, page.size(), page.offset())
			.stream()
			.map((i) -> new InquirySummary(i.id(), i.folio().value(), i.reason().name(), i.status().name(),
					i.sender().fullName(), i.sender().companyName(), i.sender().email().value(), i.assigneeId(), i.receivedAt()))
			.toList();
		return PageResult.of(items, page, this.inquiries.count(s, q));
	}

	@PreAuthorize(Authz.INQUIRY_READ)
	@Transactional(readOnly = true)
	public InquiryDetail detail(UUID id) {
		return detailOf(load(id));
	}

	@PreAuthorize(Authz.INQUIRY_MANAGE)
	@Transactional
	public InquiryDetail assign(UUID id, UUID assigneeId, long expectedVersion) {
		if (!this.staff.isActiveStaffWith(assigneeId, Permission.INQUIRY_MANAGE)) {
			throw new ValidationException("assigneeId", "INVALID_ASSIGNEE", "La persona no puede atender consultas.");
		}
		Inquiry inquiry = loadForUpdate(id, expectedVersion);
		inquiry.assign(assigneeId, this.actor.requireUserId(), this.clock.instant());
		return save(inquiry);
	}

	@PreAuthorize(Authz.INQUIRY_MANAGE)
	@Transactional
	public InquiryDetail changeStatus(UUID id, String status, @Nullable String note, long expectedVersion) {
		Inquiry inquiry = loadForUpdate(id, expectedVersion);
		inquiry.changeStatus(parse(Inquiry.Status.class, "status", status), note, this.actor.requireUserId(),
				this.clock.instant());
		return save(inquiry);
	}

	private InquiryDetail save(Inquiry inquiry) {
		this.inquiries.update(inquiry);
		this.events.publishFrom(inquiry);
		return detailOf(load(inquiry.id()));
	}

	private InquiryDetail detailOf(Inquiry i) {
		var s = i.sender();
		List<HistoryEntry> history = this.inquiries.history(i.id())
			.stream()
			.map((e) -> new HistoryEntry(e.from() == null ? null : e.from().name(), e.to().name(), e.note(), e.actorId(),
					e.occurredAt()))
			.toList();
		return new InquiryDetail(i.id(), i.folio().value(), i.reason().name(), i.status().name(), s.fullName(),
				s.companyName(), s.ruc() == null ? null : s.ruc().value(), s.position(), s.email().value(), s.phone().value(),
				i.message(), i.requestedDocument(), i.privacyNoticeVersion(), i.assigneeId(), history, i.receivedAt(),
				i.updatedAt(), i.version());
	}

	private Inquiry loadForUpdate(UUID id, long expectedVersion) {
		Inquiry inquiry = load(id);
		if (inquiry.version() != expectedVersion) {
			throw new StaleVersionException();
		}
		return inquiry;
	}

	private Inquiry load(UUID id) {
		return this.inquiries.findById(id).orElseThrow(() -> new NotFoundException("INQUIRY_NOT_FOUND", "La consulta no existe."));
	}

	private static Receipt receipt(Inquiry i) {
		return new Receipt(i.folio().value(), i.status().name(), i.receivedAt());
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
