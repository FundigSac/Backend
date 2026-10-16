package com.fundigsac.notification.application;

import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fundigsac.notification.domain.EmailOutboxRepository;
import com.fundigsac.notification.domain.OutgoingEmail;
import com.fundigsac.shared.application.PageQuery;
import com.fundigsac.shared.application.PageResult;
import com.fundigsac.shared.domain.ConflictException;
import com.fundigsac.shared.domain.NotFoundException;
import com.fundigsac.shared.domain.ValidationException;
import com.fundigsac.shared.security.Authz;

/** Monitoreo de la bandeja de salida (sin cuerpos de correo) y reintento manual de fallidos. */
@Service
public class NotificationAdminService {

	public record EmailView(UUID id, String template, String recipient, String subject, String status, int attempts,
			Instant nextAttemptAt, @Nullable String lastError, @Nullable String reference, Instant createdAt,
			@Nullable Instant sentAt) {

		static EmailView of(OutgoingEmail e) {
			return new EmailView(e.id(), e.template(), e.recipient().masked(), e.subject(), e.status().name(), e.attempts(),
					e.nextAttemptAt(), e.lastError(), e.reference(), e.createdAt(), e.sentAt());
		}

	}

	private final EmailOutboxRepository outbox;

	private final Clock clock;

	NotificationAdminService(EmailOutboxRepository outbox, Clock clock) {
		this.outbox = outbox;
		this.clock = clock;
	}

	@PreAuthorize(Authz.NOTIFICATIONS_MANAGE)
	@Transactional(readOnly = true)
	public PageResult<EmailView> list(@Nullable String status, PageQuery page) {
		String s = null;
		if (status != null && !status.isBlank()) {
			try {
				s = OutgoingEmail.Status.valueOf(status.strip().toUpperCase(Locale.ROOT)).name();
			}
			catch (IllegalArgumentException ex) {
				throw new ValidationException("status", "INVALID_STATUS", "Estado no válido (PENDING, SENT o FAILED).");
			}
		}
		var items = this.outbox.search(s, page.size(), page.offset()).stream().map(EmailView::of).toList();
		return PageResult.of(items, page, this.outbox.count(s));
	}

	@PreAuthorize(Authz.NOTIFICATIONS_MANAGE)
	@Transactional
	public EmailView retry(UUID id) {
		OutgoingEmail email = this.outbox.findById(id)
			.orElseThrow(() -> new NotFoundException("EMAIL_NOT_FOUND", "El correo no existe."));
		if (!email.retry(this.clock.instant())) {
			throw new ConflictException("NOT_RETRYABLE", "Solo se reintenta un correo fallido.");
		}
		this.outbox.update(email);
		return EmailView.of(email);
	}

}
