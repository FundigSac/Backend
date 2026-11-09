package com.fundigsac;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import com.fundigsac.shared.domain.PeruTime;
import com.fundigsac.support.IntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Idempotency-Key en los formularios públicos: el reintento devuelve el mismo folio y no crea otro registro. */
class PublicFormsIdempotencyIT extends IntegrationTest {

	private static Map<String, Object> complaint() {
		Map<String, Object> body = new HashMap<>();
		body.put("kind", "GRIEVANCE");
		body.put("consumerName", "Ana Pérez Rojas");
		body.put("documentType", "DNI");
		body.put("documentNumber", "45678912");
		body.put("email", "ana.idempotente@correo.pe");
		body.put("phone", "987654321");
		body.put("address", "Av. Los Próceres 456, Lima");
		body.put("minor", false);
		body.put("goodDescription", "Atención en la oficina de ventas");
		body.put("incidentDate", LocalDate.now(PeruTime.ZONE).minusDays(1).toString());
		body.put("facts", "No recibí respuesta a mi solicitud en el plazo que me indicaron.");
		body.put("consumerRequest", "Pido una respuesta por escrito.");
		body.put("responseChannel", "EMAIL");
		body.put("privacyNoticeVersion", "2026-10");
		body.put("acceptPrivacyNotice", true);
		return body;
	}

	private static Map<String, Object> inquiry() {
		Map<String, Object> body = new HashMap<>();
		body.put("reason", "GENERAL");
		body.put("fullName", "María Quispe");
		body.put("companyName", "Agua Limpia SAC");
		body.put("email", "maria.idempotente@agualimpia.pe");
		body.put("phone", "987654321");
		body.put("message", "Consulta enviada dos veces por un corte de conexión.");
		body.put("privacyNoticeVersion", "2026-10");
		body.put("acceptPrivacyNotice", true);
		return body;
	}

	private static Map<String, Object> rightsRequest() {
		Map<String, Object> body = new HashMap<>();
		body.put("type", "OPPOSITION");
		body.put("fullName", "Jorge Ramos");
		body.put("email", "jorge.idempotente@correo.pe");
		body.put("details", "Me opongo al envío de comunicaciones comerciales a mi correo.");
		body.put("acceptPrivacyNotice", true);
		return body;
	}

	private void assertIdempotent(String url, String key, Map<String, Object> body, Consumer<Map<String, Object>> change, String table)
			throws Exception {
		String first = body(this.mvc.perform(postJson(url, body).header("Idempotency-Key", key))
			.andExpect(status().isCreated())
			.andReturn()).get("folio").asString();
		String second = body(this.mvc.perform(postJson(url, body).header("Idempotency-Key", key))
			.andExpect(status().isCreated())
			.andReturn()).get("folio").asString();
		assertThat(second).isEqualTo(first);
		assertThat(count("SELECT count(*) FROM " + table + " WHERE folio = :f", Map.of("f", first))).isEqualTo(1);

		Map<String, Object> other = new HashMap<>(body);
		change.accept(other);
		this.mvc.perform(postJson(url, other).header("Idempotency-Key", key))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("IDEMPOTENCY_KEY_REUSED"));
		this.mvc.perform(postJson(url, body).header("Idempotency-Key", "corta"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[0].code").value("INVALID_IDEMPOTENCY_KEY"));
	}

	@Test
	void complaintsAreIdempotent() throws Exception {
		assertIdempotent("/api/v1/complaints", "reclamo-idempotente-1", complaint(),
				(b) -> b.put("consumerRequest", "Pido otra cosa distinta."), "complaints");
	}

	@Test
	void inquiriesAreIdempotent() throws Exception {
		assertIdempotent("/api/v1/inquiries", "consulta-idempotente-1", inquiry(),
				(b) -> b.put("message", "Otra consulta con un texto diferente."), "inquiries");
	}

	@Test
	void rightsRequestsAreIdempotent() throws Exception {
		assertIdempotent("/api/v1/privacy/requests", "derecho-idempotente-1", rightsRequest(),
				(b) -> b.put("details", "Otro detalle completamente diferente."), "data_subject_requests");
	}

	@Test
	void keysAreScopedPerForm() throws Exception {
		String key = "clave-compartida-entre-formularios";
		String inquiryFolio = body(this.mvc.perform(postJson("/api/v1/inquiries", inquiry()).header("Idempotency-Key", key))
			.andExpect(status().isCreated())
			.andReturn()).get("folio").asString();
		String requestFolio = body(this.mvc.perform(postJson("/api/v1/privacy/requests", rightsRequest()).header("Idempotency-Key", key))
			.andExpect(status().isCreated())
			.andReturn()).get("folio").asString();
		assertThat(inquiryFolio).startsWith("CON-");
		assertThat(requestFolio).startsWith("ARC-");
	}

}
