package com.fundigsac.inquiry;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import com.fundigsac.support.IntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** La respuesta del personal a una consulta llega por correo a quien la presentó; un cambio sin texto no envía nada. */
class InquiryAnswerIT extends IntegrationTest {

	private String submit(String email) throws Exception {
		Map<String, Object> body = new HashMap<>();
		body.put("reason", "DOCUMENTATION");
		body.put("fullName", "María Quispe");
		body.put("companyName", "Agua Limpia SAC");
		body.put("email", email);
		body.put("phone", "987654321");
		body.put("message", "Solicito la ficha técnica de la válvula DN200.");
		body.put("privacyNoticeVersion", "2026-10");
		body.put("acceptPrivacyNotice", true);
		String folio = body(this.mvc.perform(postJson("/api/v1/inquiries", body)).andExpect(status().isCreated()).andReturn())
			.get("folio")
			.asString();
		return this.jdbc.sql("SELECT CAST(id AS text) FROM inquiries WHERE folio = :f").param("f", folio).query(String.class).single();
	}

	private String etag(String id, String token) throws Exception {
		MvcResult result = this.mvc.perform(authed(get("/api/v1/staff/inquiries/" + id).with(randomIp()), token)).andReturn();
		return result.getResponse().getHeader("ETag");
	}

	private long answers(String email) {
		return count("SELECT count(*) FROM email_outbox WHERE template = 'INQUIRY_ANSWER' AND recipient = :e", Map.of("e", email));
	}

	@Test
	void answeringWithTextEmailsTheRequester() throws Exception {
		String id = submit("respuesta@agualimpia.pe");
		String admin = adminToken();
		this.mvc.perform(authed(postJson("/api/v1/staff/inquiries/" + id + "/status",
				Map.of("status", "ANSWERED", "note", "Adjuntamos la ficha técnica de la válvula DN200 en nuestro sitio.")), admin)
			.header("If-Match", etag(id, admin)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("ANSWERED"));
		String body = tokenFromOutboxBody("INQUIRY_ANSWER", "respuesta@agualimpia.pe",
				java.util.regex.Pattern.compile("(Adjuntamos la ficha técnica[^\\n]*)"));
		assertThat(body).contains("DN200");
	}

	@Test
	void statusChangesWithoutTextSendNothing() throws Exception {
		String id = submit("sin.respuesta@agualimpia.pe");
		String admin = adminToken();
		this.mvc.perform(authed(postJson("/api/v1/staff/inquiries/" + id + "/status", Map.of("status", "IN_PROGRESS")), admin)
			.header("If-Match", etag(id, admin))).andExpect(status().isOk());
		this.mvc.perform(authed(postJson("/api/v1/staff/inquiries/" + id + "/status", Map.of("status", "ANSWERED")), admin)
			.header("If-Match", etag(id, admin))).andExpect(status().isOk());
		Awaitility.await().pollDelay(Duration.ofSeconds(2)).atMost(Duration.ofSeconds(5))
			.untilAsserted(() -> assertThat(answers("sin.respuesta@agualimpia.pe")).isZero());
	}

}
