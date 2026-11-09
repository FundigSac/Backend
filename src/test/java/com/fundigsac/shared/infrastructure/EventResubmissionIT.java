package com.fundigsac.shared.infrastructure;

import java.time.Duration;
import java.util.Map;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import com.fundigsac.notification.domain.EmailOutboxRepository;
import com.fundigsac.notification.domain.OutgoingEmail;
import com.fundigsac.support.ApiFixtures;
import com.fundigsac.support.IntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Outbox de eventos: si un oyente falla (o la aplicación cae) antes de terminar, la publicación queda incompleta y la
 * tarea de reintento vuelve a entregarla sin perder el correo.
 */
class EventResubmissionIT extends IntegrationTest {

	private static final String INCOMPLETE = "SELECT count(*) FROM event_publication WHERE completion_date IS NULL AND listener_id LIKE '%NotificationListeners%'";

	@MockitoSpyBean
	EmailOutboxRepository outbox;

	@Autowired
	MaintenanceJobs jobs;

	@Test
	void anIncompleteEventIsDeliveredAgainAndTheEmailIsNotLost() throws Exception {
		doThrow(new IllegalStateException("Caída simulada del oyente")).doCallRealMethod().when(this.outbox).enqueue(any(OutgoingEmail.class));

		String folio = body(this.mvc.perform(postJson("/api/v1/inquiries", ApiFixtures.inquiry("reintento.evento@empresa.pe")))
			.andExpect(status().isCreated())
			.andReturn()).get("folio").asString();

		Awaitility.await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> assertThat(count(INCOMPLETE, Map.of())).isEqualTo(1));
		assertThat(count("SELECT count(*) FROM email_outbox WHERE reference = :f", Map.of("f", folio))).as("el fallo revirtió los correos").isZero();

		// Solo se reintentan publicaciones con más de 2 minutos: se simula ese paso del tiempo.
		this.jdbc.sql("UPDATE event_publication SET publication_date = publication_date - interval '5 minutes' WHERE completion_date IS NULL").update();
		this.jobs.resubmitIncompleteEvents();

		Awaitility.await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
			assertThat(count("SELECT count(*) FROM email_outbox WHERE reference = :f", Map.of("f", folio))).isEqualTo(2);
			assertThat(count(INCOMPLETE, Map.of())).isZero();
		});
	}

	@Test
	void aRecentlyFailedEventIsNotRetriedBeforeItsGracePeriod() throws Exception {
		doThrow(new IllegalStateException("Caída simulada del oyente")).when(this.outbox).enqueue(any(OutgoingEmail.class));

		String folio = body(this.mvc.perform(postJson("/api/v1/inquiries", ApiFixtures.inquiry("reintento.reciente@empresa.pe")))
			.andExpect(status().isCreated())
			.andReturn()).get("folio").asString();
		Awaitility.await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> assertThat(count(INCOMPLETE, Map.of())).isPositive());
		doCallRealMethod().when(this.outbox).enqueue(any(OutgoingEmail.class));

		this.jobs.resubmitIncompleteEvents();

		// La entrega es asíncrona: se exige que durante 3 segundos no llegue ningún correo. Con el oyente ya reparado, un
		// reintento antes de tiempo los crearía en milisegundos.
		Awaitility.await().during(Duration.ofSeconds(3)).atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
			assertThat(count("SELECT count(*) FROM email_outbox WHERE reference = :f", Map.of("f", folio))).isZero();
			assertThat(count(INCOMPLETE, Map.of())).isPositive();
		});
		// Deja el registro limpio para no afectar a otras pruebas de este contexto.
		this.jdbc.sql("DELETE FROM event_publication WHERE completion_date IS NULL").update();
	}

}
