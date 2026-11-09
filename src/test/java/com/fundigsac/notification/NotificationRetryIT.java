package com.fundigsac.notification;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import com.fundigsac.support.IntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Reintento manual de correos fallidos: solo correos con cuerpo disponible y solo con el permiso adecuado. */
class NotificationRetryIT extends IntegrationTest {

	private static final String BASE = "/api/v1/staff/notifications";

	private static final String UNKNOWN = BASE + "/00000000-0000-7000-8000-00000000abcd/retry";

	private List<String> outboxIdsFor(String email) throws Exception {
		Map<String, Object> inquiry = new HashMap<>();
		inquiry.put("reason", "GENERAL");
		inquiry.put("fullName", "Elena Ríos");
		inquiry.put("companyName", "Riego Centro SAC");
		inquiry.put("email", email);
		inquiry.put("phone", "987654321");
		inquiry.put("message", "Consulta usada para comprobar el reintento de correos.");
		inquiry.put("privacyNoticeVersion", "2026-10");
		inquiry.put("acceptPrivacyNotice", true);
		String folio = body(this.mvc.perform(postJson("/api/v1/inquiries", inquiry)).andExpect(status().isCreated()).andReturn())
			.get("folio")
			.asString();
		Awaitility.await().atMost(Duration.ofSeconds(15))
			.untilAsserted(() -> assertThat(count("SELECT count(*) FROM email_outbox WHERE reference = :f", Map.of("f", folio))).isEqualTo(2));
		return this.jdbc.sql("SELECT CAST(id AS text) FROM email_outbox WHERE reference = :f ORDER BY template").param("f", folio).query(String.class).list();
	}

	private void markFailed(String id) {
		this.jdbc.sql("UPDATE email_outbox SET status = 'FAILED', attempts = 5, last_error = 'SMTP no disponible' WHERE id = CAST(:id AS uuid)")
			.param("id", id)
			.update();
	}

	@Test
	void retriesAFailedEmailOnlyOnce() throws Exception {
		String admin = adminToken();
		String id = outboxIdsFor("elena.reintento@riegocentro.pe").get(0);
		markFailed(id);

		this.mvc.perform(authed(post(BASE + "/" + id + "/retry").with(randomIp()), admin))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("PENDING"))
			.andExpect(jsonPath("$.attempts").value(0));
		// Ya está en cola: otro reintento no lo duplica.
		this.mvc.perform(authed(post(BASE + "/" + id + "/retry").with(randomIp()), admin))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("NOT_RETRYABLE"));
	}

	@Test
	void anEmailWithoutBodyCannotBeRetried() throws Exception {
		String admin = adminToken();
		String id = outboxIdsFor("elena.sin.cuerpo@riegocentro.pe").get(1);
		markFailed(id);
		this.jdbc.sql("UPDATE email_outbox SET body = NULL WHERE id = CAST(:id AS uuid)").param("id", id).update();

		this.mvc.perform(authed(post(BASE + "/" + id + "/retry").with(randomIp()), admin))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("NOT_RETRYABLE"));
	}

	@Test
	void unknownEmailIsNotFound() throws Exception {
		this.mvc.perform(authed(post(UNKNOWN).with(randomIp()), adminToken()))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("EMAIL_NOT_FOUND"));
		this.mvc.perform(authed(post(BASE + "/no-es-uuid/retry").with(randomIp()), adminToken())).andExpect(status().isBadRequest());
	}

	@Test
	void retryRequiresTheNotificationsPermission() throws Exception {
		String sales = staffToken("ventas.correos@fundigsac.test", "SALES");
		String customer = customerToken("cliente.correos@andina.pe", "frase-de-paso-muy-larga");
		this.mvc.perform(authed(post(UNKNOWN).with(randomIp()), sales)).andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("FORBIDDEN"));
		this.mvc.perform(authed(post(UNKNOWN).with(randomIp()), customer)).andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("FORBIDDEN"));
		this.mvc.perform(post(UNKNOWN).with(randomIp())).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
	}

}
