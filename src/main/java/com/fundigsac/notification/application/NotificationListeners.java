package com.fundigsac.notification.application;

import java.time.Clock;
import java.time.Instant;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import com.fundigsac.complaint.api.ComplaintEvents.ComplaintAnswered;
import com.fundigsac.complaint.api.ComplaintEvents.ComplaintFiled;
import com.fundigsac.identity.api.IdentityEvents.AccountLocked;
import com.fundigsac.identity.api.IdentityEvents.PasswordChanged;
import com.fundigsac.identity.api.IdentityEvents.PasswordResetRequested;
import com.fundigsac.identity.api.IdentityEvents.RegistrationAttemptOnExistingAccount;
import com.fundigsac.identity.api.IdentityEvents.StaffInvited;
import com.fundigsac.identity.api.IdentityEvents.VerificationEmailRequested;
import com.fundigsac.inquiry.api.InquiryEvents.InquiryReceived;
import com.fundigsac.notification.domain.EmailOutboxRepository;
import com.fundigsac.notification.domain.OutgoingEmail;
import com.fundigsac.privacy.api.PrivacyEvents.DataSubjectRequestReceived;
import com.fundigsac.privacy.api.PrivacyEvents.DataSubjectRequestUpdated;
import com.fundigsac.quotation.api.QuoteEvents.QuoteSubmitted;
import com.fundigsac.shared.domain.Email;
import com.fundigsac.shared.domain.Ids;

/**
 * Reacciona a los eventos de otros módulos (tras confirmarse su transacción, de forma asíncrona y con reintento
 * garantizado por el registro de eventos) escribiendo los correos en la bandeja de salida. La clave de deduplicación
 * evita duplicados si un evento se reprocesa.
 */
@Component
class NotificationListeners {

	private static final Logger log = LoggerFactory.getLogger(NotificationListeners.class);

	private final EmailOutboxRepository outbox;

	private final EmailTemplates templates;

	private final NotificationProperties properties;

	private final Clock clock;

	NotificationListeners(EmailOutboxRepository outbox, EmailTemplates templates, NotificationProperties properties,
			Clock clock) {
		this.outbox = outbox;
		this.templates = templates;
		this.properties = properties;
		this.clock = clock;
	}

	@ApplicationModuleListener
	public void on(QuoteSubmitted e) {
		enqueue("QUOTE_RECEIPT", e.folio(), e.occurredAt(), e.email(), this.templates.quoteReceipt(e.contactName(), e.folio(), e.lineCount(), e.occurredAt()));
		enqueue("QUOTE_SALES", e.folio(), e.occurredAt(), this.properties.salesInbox(),
				this.templates.quoteForSales(e.folio(), e.companyName(), e.contactName(), e.origin(), e.lineCount()));
	}

	@ApplicationModuleListener
	public void on(InquiryReceived e) {
		enqueue("INQUIRY_RECEIPT", e.folio(), e.occurredAt(), e.email(), this.templates.inquiryReceipt(e.fullName(), e.folio(), e.occurredAt()));
		enqueue("INQUIRY_SALES", e.folio(), e.occurredAt(), this.properties.salesInbox(),
				this.templates.inquiryForSales(e.folio(), e.companyName(), e.fullName(), e.reason()));
	}

	@ApplicationModuleListener
	public void on(ComplaintFiled e) {
		enqueue("COMPLAINT_RECEIPT", e.folio(), e.occurredAt(), e.email(),
				this.templates.complaintReceipt(e.consumerName(), e.folio(), e.kind(), e.responseDueOn(), e.occurredAt()));
		enqueue("COMPLAINT_LEGAL", e.folio(), e.occurredAt(), this.properties.legalInbox(),
				this.templates.complaintForLegal(e.folio(), e.kind(), e.responseDueOn()));
	}

	@ApplicationModuleListener
	public void on(ComplaintAnswered e) {
		if ("EMAIL".equals(e.responseChannel())) {
			enqueue("COMPLAINT_ANSWER", e.folio(), e.occurredAt(), e.email(),
					this.templates.complaintAnswer(e.consumerName(), e.folio(), e.responseText()));
		}
		else {
			enqueue("COMPLAINT_ANSWER_POST", e.folio(), e.occurredAt(), this.properties.legalInbox(),
					this.templates.complaintAnswerByPost(e.folio()));
		}
	}

	@ApplicationModuleListener
	public void on(DataSubjectRequestReceived e) {
		enqueue("RIGHTS_RECEIPT", e.folio(), e.occurredAt(), e.email(), this.templates.rightsReceipt(e.fullName(), e.folio(), e.type()));
		enqueue("RIGHTS_PRIVACY", e.folio(), e.occurredAt(), this.properties.privacyInbox(),
				this.templates.rightsForPrivacy(e.folio(), e.type()));
	}

	@ApplicationModuleListener
	public void on(DataSubjectRequestUpdated e) {
		if (e.resolutionNote() != null) {
			enqueue("RIGHTS_RESOLUTION", e.folio(), e.occurredAt(), e.email(),
					this.templates.rightsResolution(e.fullName(), e.folio(), e.status(), e.resolutionNote()));
		}
	}

	@ApplicationModuleListener
	public void on(VerificationEmailRequested e) {
		enqueue("VERIFY_EMAIL", e.userId().toString(), e.occurredAt(), e.email(),
				this.templates.verifyEmail(e.fullName(), e.token(), e.expiresAt()));
	}

	@ApplicationModuleListener
	public void on(PasswordResetRequested e) {
		enqueue("PASSWORD_RESET", e.userId().toString(), e.occurredAt(), e.email(),
				this.templates.passwordReset(e.fullName(), e.token(), e.expiresAt()));
	}

	@ApplicationModuleListener
	public void on(StaffInvited e) {
		enqueue("STAFF_INVITATION", e.userId().toString(), e.occurredAt(), e.email(),
				this.templates.staffInvitation(e.fullName(), e.token(), e.expiresAt()));
	}

	@ApplicationModuleListener
	public void on(RegistrationAttemptOnExistingAccount e) {
		enqueue("REGISTRATION_ATTEMPT", e.userId().toString(), e.occurredAt(), e.email(), this.templates.registrationAttempt(e.fullName()));
	}

	@ApplicationModuleListener
	public void on(PasswordChanged e) {
		enqueue("PASSWORD_CHANGED", e.userId().toString(), e.occurredAt(), e.email(), this.templates.passwordChanged(e.fullName(), e.occurredAt()));
	}

	@ApplicationModuleListener
	public void on(AccountLocked e) {
		enqueue("ACCOUNT_LOCKED", e.userId().toString(), e.occurredAt(), e.email(), this.templates.accountLocked(e.fullName(), e.lockedUntil()));
	}

	private void enqueue(String template, String reference, Instant occurredAt, @Nullable String recipient,
			EmailTemplates.Message message) {
		if (recipient == null || recipient.isBlank()) {
			return;
		}
		Email to;
		try {
			to = new Email(recipient);
		}
		catch (RuntimeException ex) {
			log.warn("Destinatario no válido para {} ({}); se omite el correo.", template, reference);
			return;
		}
		String dedupeKey = template + ":" + reference + ":" + occurredAt.toEpochMilli() + ":" + to.value();
		String key = dedupeKey.length() > 200 ? dedupeKey.substring(0, 200) : dedupeKey;
		String ref = reference.length() > 40 ? reference.substring(0, 40) : reference;
		this.outbox.enqueue(OutgoingEmail.pending(Ids.newId(), key, template, to, message.subject(), message.body(), ref,
				this.clock.instant()));
	}

}
