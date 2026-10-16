package com.fundigsac.notification.application;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.fundigsac.support.IntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** El despachador envía los correos pendientes, borra el cuerpo y el personal los ve enmascarados. */
class OutboxDispatcherIT extends IntegrationTest {

	@Autowired
	OutboxDispatcher dispatcher;

	@Test
	void sendsPendingEmailsAndPurgesBodies() throws Exception {
		Map<String, Object> inquiry = new HashMap<>();
		inquiry.put("reason", "DOCUMENTATION");
		inquiry.put("fullName", "Pedro Salas");
		inquiry.put("companyName", "Riego Norte SAC");
		inquiry.put("email", "pedro@riegonorte.pe");
		inquiry.put("phone", "987123456");
		inquiry.put("message", "Solicito el catálogo general técnico 2026.");
		inquiry.put("requestedDocument", "Catálogo General Técnico FUNDIGSAC 2026");
		inquiry.put("privacyNoticeVersion", "2026-10");
		inquiry.put("acceptPrivacyNotice", true);
		String folio = body(this.mvc.perform(postJson("/api/v1/inquiries", inquiry)).andExpect(status().isCreated()).andReturn())
			.get("folio").asString();
		Awaitility.await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> assertThat(
				count("SELECT count(*) FROM email_outbox WHERE reference = :f AND status = 'PENDING'", Map.of("f", folio))).isEqualTo(2));

		// Puede haber correos pendientes de otras pruebas: se despacha por lotes hasta vaciar la bandeja.
		for (int i = 0; i < 20 && count("SELECT count(*) FROM email_outbox WHERE status = 'PENDING'", Map.of()) > 0; i++) {
			this.dispatcher.dispatch();
		}

		assertThat(count("SELECT count(*) FROM email_outbox WHERE reference = :f AND status = 'SENT' AND body IS NULL",
				Map.of("f", folio))).isEqualTo(2);
		this.mvc.perform(authed(get("/api/v1/staff/notifications").param("status", "SENT").with(randomIp()), adminToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.items[0].recipient").value(org.hamcrest.Matchers.containsString("***@")));
	}

}
