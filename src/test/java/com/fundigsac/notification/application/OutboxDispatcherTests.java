package com.fundigsac.notification.application;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import com.fundigsac.notification.domain.EmailOutboxRepository;
import com.fundigsac.notification.domain.MailGateway;
import com.fundigsac.notification.domain.OutgoingEmail;
import com.fundigsac.shared.domain.Email;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Un fallo de SMTP nunca marca el correo como enviado: se reintenta con espera creciente y al agotarse queda FAILED. */
@ExtendWith(OutputCaptureExtension.class)
class OutboxDispatcherTests {

	private static final Instant NOW = Instant.parse("2026-11-09T15:00:00Z");

	private final EmailOutboxRepository outbox = mock(EmailOutboxRepository.class);

	private final MailGateway mail = mock(MailGateway.class);

	private final NotificationProperties properties = new NotificationProperties("smtp", "de@fundigsac.pe", "http://sitio", "http://panel",
			"ventas@fundigsac.pe", "legal@fundigsac.pe", "privacidad@fundigsac.pe", 3, Duration.ofSeconds(30), 20);

	private final OutboxDispatcher dispatcher = new OutboxDispatcher(this.outbox, this.mail, this.properties, Clock.fixed(NOW, ZoneOffset.UTC));

	private static OutgoingEmail pending() {
		return OutgoingEmail.pending(UUID.randomUUID(), "clave-" + UUID.randomUUID(), "PLANTILLA", new Email("cliente@empresa.pe"), "Asunto",
				"Cuerpo con enlace", "COT-2026-000001", NOW);
	}

	private void due(OutgoingEmail email) {
		when(this.outbox.claimDue(NOW, 20)).thenReturn(List.of(email));
	}

	private void smtpFails() {
		doThrow(new IllegalStateException("SMTP caído")).when(this.mail).send(anyString(), anyString(), anyString());
	}

	@Test
	void theLogOfAnExhaustedEmailDoesNotRevealTheRecipient(CapturedOutput output) {
		OutgoingEmail email = pending();
		due(email);
		doThrow(new IllegalStateException("Invalid Addresses: cliente@empresa.pe")).when(this.mail).send(anyString(), anyString(), anyString());

		for (int attempt = 0; attempt < 3; attempt++) {
			this.dispatcher.dispatch();
		}

		assertThat(email.status()).isEqualTo(OutgoingEmail.Status.FAILED);
		assertThat(output.getAll()).contains("PLANTILLA").doesNotContain("cliente@empresa.pe");
	}

	@Test
	void aDeliveredEmailIsMarkedSentAndLosesItsBody() {
		OutgoingEmail email = pending();
		due(email);

		this.dispatcher.dispatch();

		verify(this.mail).send("cliente@empresa.pe", "Asunto", "Cuerpo con enlace");
		assertThat(email.status()).isEqualTo(OutgoingEmail.Status.SENT);
		assertThat(email.body()).isNull();
		assertThat(email.sentAt()).isEqualTo(NOW);
		verify(this.outbox).update(email);
	}

	@Test
	void aFailedDeliveryStaysPendingKeepsItsBodyAndWaits() {
		OutgoingEmail email = pending();
		due(email);
		smtpFails();

		this.dispatcher.dispatch();

		assertThat(email.status()).isEqualTo(OutgoingEmail.Status.PENDING);
		assertThat(email.body()).isEqualTo("Cuerpo con enlace");
		assertThat(email.sentAt()).isNull();
		assertThat(email.attempts()).isEqualTo(1);
		assertThat(email.lastError()).isEqualTo("IllegalStateException: SMTP caído");
		assertThat(email.nextAttemptAt()).isEqualTo(NOW.plusSeconds(30));
		verify(this.outbox).update(email);
	}

	@Test
	void theWaitDoublesWithEachFailure() {
		OutgoingEmail email = pending();
		due(email);
		smtpFails();

		this.dispatcher.dispatch();
		this.dispatcher.dispatch();

		assertThat(email.status()).isEqualTo(OutgoingEmail.Status.PENDING);
		assertThat(email.nextAttemptAt()).isEqualTo(NOW.plusSeconds(60));
	}

	@Test
	void afterTheLastAttemptTheEmailIsFailedAndCanBeRetriedByHand() {
		OutgoingEmail email = pending();
		due(email);
		smtpFails();

		this.dispatcher.dispatch();
		this.dispatcher.dispatch();
		this.dispatcher.dispatch();

		assertThat(email.status()).isEqualTo(OutgoingEmail.Status.FAILED);
		assertThat(email.attempts()).isEqualTo(3);
		assertThat(email.retry(NOW)).isTrue();
		assertThat(email.status()).isEqualTo(OutgoingEmail.Status.PENDING);
		assertThat(email.attempts()).isZero();
	}

	@Test
	void aVeryLongErrorIsTruncatedBeforeStoringIt() {
		OutgoingEmail email = pending();
		email.markFailed("x".repeat(900), 8, Duration.ofSeconds(30), NOW);
		assertThat(email.lastError()).hasSize(500);
	}

	@Test
	void aSentEmailCannotBeRetried() {
		OutgoingEmail email = pending();
		email.markSent(NOW);
		assertThat(email.retry(NOW)).isFalse();
	}

}
