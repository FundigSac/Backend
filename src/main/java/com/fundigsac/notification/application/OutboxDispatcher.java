package com.fundigsac.notification.application;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fundigsac.notification.domain.EmailOutboxRepository;
import com.fundigsac.notification.domain.MailGateway;
import com.fundigsac.notification.domain.OutgoingEmail;

/**
 * Despacha la bandeja de salida cada pocos segundos. Las filas se reservan con FOR UPDATE SKIP LOCKED, así varias
 * instancias no envían el mismo correo. Los fallos se reintentan con espera exponencial; al agotarse quedan FAILED y
 * se registran como error para alerta.
 */
@Component
class OutboxDispatcher {

	private static final Logger log = LoggerFactory.getLogger(OutboxDispatcher.class);

	private final EmailOutboxRepository outbox;

	private final MailGateway mail;

	private final NotificationProperties properties;

	private final Clock clock;

	OutboxDispatcher(EmailOutboxRepository outbox, MailGateway mail, NotificationProperties properties, Clock clock) {
		this.outbox = outbox;
		this.mail = mail;
		this.properties = properties;
		this.clock = clock;
	}

	@Scheduled(initialDelayString = "${fundigsac.notifications.initial-delay:PT5S}", fixedDelayString = "${fundigsac.notifications.poll-interval:PT5S}")
	@Transactional
	public void dispatch() {
		Instant now = this.clock.instant();
		List<OutgoingEmail> due = this.outbox.claimDue(now, this.properties.batchSize());
		for (OutgoingEmail email : due) {
			try {
				this.mail.send(email.recipient().value(), email.subject(), email.body());
				email.markSent(this.clock.instant());
			}
			catch (RuntimeException ex) {
				email.markFailed(ex.getClass().getSimpleName() + ": " + ex.getMessage(), this.properties.maxAttempts(),
						this.properties.retryBase(), this.clock.instant());
				if (email.status() == OutgoingEmail.Status.FAILED) {
					log.error("Correo {} ({}) falló tras {} intentos: {}", email.id(), email.template(), email.attempts(),
							email.lastError());
				}
				else {
					log.warn("Correo {} ({}) falló (intento {}); se reintentará.", email.id(), email.template(), email.attempts());
				}
			}
			this.outbox.update(email);
		}
	}

}
