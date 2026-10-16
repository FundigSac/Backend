package com.fundigsac.complaint.application;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fundigsac.complaint.application.ComplaintViews.ComplaintSheet;
import com.fundigsac.complaint.application.ComplaintViews.ComplaintSummary;
import com.fundigsac.complaint.application.ComplaintViews.Receipt;
import com.fundigsac.complaint.domain.Complaint;
import com.fundigsac.complaint.domain.ComplaintRepository;
import com.fundigsac.complaint.domain.ResponseDeadlinePolicy;
import com.fundigsac.identity.api.StaffDirectory;
import com.fundigsac.shared.application.CurrentActor;
import com.fundigsac.shared.application.DomainEvents;
import com.fundigsac.shared.application.FolioGenerator;
import com.fundigsac.shared.application.Idempotency;
import com.fundigsac.shared.application.PageQuery;
import com.fundigsac.shared.application.PageResult;
import com.fundigsac.shared.application.ReceiptTokens;
import com.fundigsac.shared.domain.Email;
import com.fundigsac.shared.domain.Folio;
import com.fundigsac.shared.domain.IdentityDocument;
import com.fundigsac.shared.domain.Ids;
import com.fundigsac.shared.domain.Money;
import com.fundigsac.shared.domain.NotFoundException;
import com.fundigsac.shared.domain.PeruTime;
import com.fundigsac.shared.domain.PhoneNumber;
import com.fundigsac.shared.domain.StaleVersionException;
import com.fundigsac.shared.domain.ValidationException;
import com.fundigsac.shared.security.Authz;
import com.fundigsac.shared.security.Permission;

/**
 * Registro público de hojas (folio REC correlativo, constancia con token) y su gestión por Atención Legal. Si falla
 * el guardado no hay folio ni mensaje de "presentado" (arquitectura §11.4).
 */
@Service
public class ComplaintService {

	static final String RECEIPT_PURPOSE = "complaint";

	public record FileComplaint(String kind, String consumerName, String documentType, String documentNumber, String email,
			String phone, String address, boolean minor, @Nullable String guardianName, @Nullable String guardianDocumentType,
			@Nullable String guardianDocumentNumber, @Nullable String goodType, String goodDescription,
			@Nullable BigDecimal claimedAmount, @Nullable String currency, @Nullable String serviceLocation,
			LocalDate incidentDate, @Nullable String relatedReference, String facts, String consumerRequest,
			String responseChannel, String privacyNoticeVersion) {
	}

	private final ComplaintRepository complaints;

	private final FolioGenerator folios;

	private final Idempotency idempotency;

	private final ReceiptTokens receipts;

	private final StaffDirectory staff;

	private final CurrentActor actor;

	private final DomainEvents events;

	private final ResponseDeadlinePolicy deadline;

	private final Clock clock;

	ComplaintService(ComplaintRepository complaints, FolioGenerator folios, Idempotency idempotency,
			ReceiptTokens receipts, StaffDirectory staff, CurrentActor actor, DomainEvents events,
			ComplaintProperties properties, Clock clock) {
		this.complaints = complaints;
		this.folios = folios;
		this.idempotency = idempotency;
		this.receipts = receipts;
		this.staff = staff;
		this.actor = actor;
		this.events = events;
		this.deadline = new ResponseDeadlinePolicy(properties.responseBusinessDays(), new HashSet<>(properties.holidays()));
		this.clock = clock;
	}

	@Transactional
	public Receipt file(FileComplaint command, @Nullable String idempotencyKey) {
		return this.idempotency.execute("complaint", idempotencyKey, command, () -> register(command), Receipt::folio,
				(folio) -> receipt(this.complaints.findByFolio(folio).orElseThrow()));
	}

	private Receipt register(FileComplaint c) {
		Instant now = this.clock.instant();
		Complaint.Consumer consumer = new Complaint.Consumer(c.consumerName(),
				IdentityDocument.of(parse(IdentityDocument.Type.class, "documentType", c.documentType()), c.documentNumber()),
				Email.of("email", c.email()), PhoneNumber.of("phone", c.phone()), c.address());
		boolean guardianGiven = c.guardianName() != null && !c.guardianName().isBlank();
		Complaint.Guardian guardian = c.minor() && guardianGiven
				? new Complaint.Guardian(c.guardianName(), IdentityDocument.of(
						parse(IdentityDocument.Type.class, "guardianDocumentType", c.guardianDocumentType()),
						c.guardianDocumentNumber()))
				: null;
		Money amount = null;
		if (c.claimedAmount() != null) {
			amount = new Money(c.claimedAmount(), parse(Money.Currency.class, "currency", c.currency() == null ? "PEN" : c.currency()));
		}
		Complaint.Details details = new Complaint.Details(
				c.goodType() == null || c.goodType().isBlank() ? null : parse(Complaint.GoodType.class, "goodType", c.goodType()),
				c.goodDescription(), amount, c.serviceLocation(), c.incidentDate(), c.relatedReference(), c.facts(),
				c.consumerRequest());
		Complaint complaint = Complaint.file(Ids.newId(), this.folios.next("REC"), parse(Complaint.Kind.class, "kind", c.kind()),
				consumer, c.minor(), guardian, details,
				parse(Complaint.ResponseChannel.class, "responseChannel", c.responseChannel()), c.privacyNoticeVersion(),
				this.deadline, now);
		this.complaints.insert(complaint);
		this.events.publishFrom(complaint);
		return receipt(complaint);
	}

	@Transactional(readOnly = true)
	public ComplaintSheet receiptSheet(String folio, @Nullable String token) {
		String normalized = Folio.parse(folio).value();
		if (!this.receipts.matches(RECEIPT_PURPOSE, normalized, token)) {
			throw notFound();
		}
		Complaint complaint = this.complaints.findByFolio(normalized).orElseThrow(ComplaintService::notFound);
		return ComplaintSheet.of(complaint, List.of(), this.clock.instant(), false);
	}

	@PreAuthorize(Authz.COMPLAINT_READ)
	@Transactional(readOnly = true)
	public PageResult<ComplaintSummary> inbox(@Nullable String status, boolean overdueOnly, @Nullable String query,
			PageQuery page) {
		Instant now = this.clock.instant();
		String s = status == null || status.isBlank() ? null : parse(Complaint.Status.class, "status", status).name();
		LocalDate overdue = overdueOnly ? PeruTime.dateOf(now) : null;
		String q = query == null || query.isBlank() ? null : query.strip();
		List<ComplaintSummary> items = this.complaints.search(s, overdue, q, page.size(), page.offset())
			.stream()
			.map((c) -> new ComplaintSummary(c.id(), c.folio().value(), c.kind().name(), c.status().name(),
					c.consumer().fullName(), c.filedAt(), c.responseDueOn(), c.isOverdue(now), c.assigneeId()))
			.toList();
		return PageResult.of(items, page, this.complaints.count(s, overdue, q));
	}

	@PreAuthorize(Authz.COMPLAINT_READ)
	@Transactional(readOnly = true)
	public ComplaintSheet detail(UUID id) {
		Complaint complaint = load(id);
		return ComplaintSheet.of(complaint, this.complaints.history(id), this.clock.instant(), true);
	}

	@PreAuthorize(Authz.COMPLAINT_MANAGE)
	@Transactional
	public ComplaintSheet assign(UUID id, UUID assigneeId, long expectedVersion) {
		if (!this.staff.isActiveStaffWith(assigneeId, Permission.COMPLAINT_MANAGE)) {
			throw new ValidationException("assigneeId", "INVALID_ASSIGNEE", "La persona no puede atender reclamaciones.");
		}
		Complaint complaint = loadForUpdate(id, expectedVersion);
		complaint.assign(assigneeId, this.actor.requireUserId(), this.clock.instant());
		return save(complaint);
	}

	@PreAuthorize(Authz.COMPLAINT_MANAGE)
	@Transactional
	public ComplaintSheet addNote(UUID id, String note, long expectedVersion) {
		Complaint complaint = loadForUpdate(id, expectedVersion);
		complaint.addNote(note, this.actor.requireUserId(), this.clock.instant());
		return save(complaint);
	}

	@PreAuthorize(Authz.COMPLAINT_MANAGE)
	@Transactional
	public ComplaintSheet answer(UUID id, String response, long expectedVersion) {
		Complaint complaint = loadForUpdate(id, expectedVersion);
		complaint.answer(response, this.actor.requireUserId(), this.clock.instant());
		return save(complaint);
	}

	@PreAuthorize(Authz.COMPLAINT_MANAGE)
	@Transactional
	public ComplaintSheet close(UUID id, long expectedVersion) {
		Complaint complaint = loadForUpdate(id, expectedVersion);
		complaint.close(this.actor.requireUserId(), this.clock.instant());
		return save(complaint);
	}

	private ComplaintSheet save(Complaint complaint) {
		this.complaints.update(complaint);
		this.events.publishFrom(complaint);
		return detail(complaint.id());
	}

	private Complaint loadForUpdate(UUID id, long expectedVersion) {
		Complaint complaint = load(id);
		if (complaint.version() != expectedVersion) {
			throw new StaleVersionException();
		}
		return complaint;
	}

	private Complaint load(UUID id) {
		return this.complaints.findById(id).orElseThrow(ComplaintService::notFound);
	}

	private Receipt receipt(Complaint c) {
		String folio = c.folio().value();
		return new Receipt(folio, c.kind().name(), c.status().name(), c.filedAt(), c.responseDueOn(),
				this.receipts.issue(RECEIPT_PURPOSE, folio));
	}

	private static NotFoundException notFound() {
		return new NotFoundException("COMPLAINT_NOT_FOUND", "La hoja de reclamación no existe.");
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
