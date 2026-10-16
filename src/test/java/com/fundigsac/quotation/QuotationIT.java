package com.fundigsac.quotation;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import com.fundigsac.support.IntegrationTest;
import tools.jackson.databind.JsonNode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Solicitud de cotización de punta a punta: registro, idempotencia, constancia, eventos y bandeja de Ventas. */
class QuotationIT extends IntegrationTest {

	static Map<String, Object> quote(String productSlug, String variant) {
		Map<String, Object> item = new HashMap<>();
		item.put("productSlug", productSlug);
		item.put("variantLabel", variant);
		item.put("quantity", 12);
		item.put("unit", "und");
		item.put("addons", List.of("Kit de pernos"));
		Map<String, Object> body = new HashMap<>();
		body.put("origin", "PRODUCT_FORM");
		body.put("contact", Map.of("companyName", "Constructora Andina SAC", "ruc", "20131312955", "contactName", "Luis Torres",
				"email", "compras@andina.pe", "phone", "+51 987 654 321"));
		body.put("project", Map.of("projectType", "Saneamiento", "deliveryDepartment", "Lima", "message",
				"Obra de agua potable en Ate."));
		body.put("items", List.of(item));
		body.put("privacyNoticeVersion", "2026-10");
		body.put("acceptPrivacyNotice", true);
		return body;
	}

	@Test
	void registersQuoteWithFolioReceiptAndEvents() throws Exception {
		MvcResult created = this.mvc.perform(postJson("/api/v1/quotes", quote("acople-gran-rango-agr", "DN100 · 105–125 mm")))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.folio").value(matchesPattern("COT-\\d{4}-\\d{6}")))
			.andExpect(jsonPath("$.status").value("RECEIVED"))
			.andExpect(header().string("Cache-Control", "no-store"))
			.andReturn();
		JsonNode receipt = body(created);
		String folio = receipt.get("folio").asString();
		String token = receipt.get("receiptToken").asString();

		this.mvc.perform(get("/api/v1/quotes/" + folio + "/receipt").with(randomIp()).header("X-Receipt-Token", token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.email").value("c***@andina.pe"))
			.andExpect(jsonPath("$.items[0].productName").value("Acople gran rango AGR"))
			.andExpect(jsonPath("$.items[0].unit").value("und"));
		this.mvc.perform(get("/api/v1/quotes/" + folio + "/receipt").with(randomIp()).header("X-Receipt-Token", "falso"))
			.andExpect(status().isNotFound());
		this.mvc.perform(get("/api/v1/quotes/" + folio + "/receipt").with(randomIp())).andExpect(status().isNotFound());

		// Correos en la bandeja de salida y registro de auditoría (asíncronos, tras confirmar la transacción).
		Awaitility.await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
			assertThat(count("SELECT count(*) FROM email_outbox WHERE reference = :f", Map.of("f", folio))).isEqualTo(2);
			assertThat(count("SELECT count(*) FROM audit_events WHERE action = 'QUOTE_SUBMITTED' AND details->>'folio' = :f",
					Map.of("f", folio))).isEqualTo(1);
		});
	}

	@Test
	void idempotencyKeyReturnsSameFolioAndRejectsDifferentPayload() throws Exception {
		Map<String, Object> body = quote("vcp-f4-01", null);
		String first = body(this.mvc.perform(postJson("/api/v1/quotes", body).header("Idempotency-Key", "reintento-001-abc"))
			.andExpect(status().isCreated())
			.andReturn()).get("folio").asString();
		String second = body(this.mvc.perform(postJson("/api/v1/quotes", body).header("Idempotency-Key", "reintento-001-abc"))
			.andExpect(status().isCreated())
			.andReturn()).get("folio").asString();
		assertThat(second).isEqualTo(first);

		Map<String, Object> changed = quote("vcp-f4-01", null);
		changed.put("origin", "GENERAL_FORM");
		this.mvc.perform(postJson("/api/v1/quotes", changed).header("Idempotency-Key", "reintento-001-abc"))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("IDEMPOTENCY_KEY_REUSED"));
	}

	@Test
	void validatesAgainstThePublishedCatalog() throws Exception {
		this.mvc.perform(postJson("/api/v1/quotes", quote("producto-inventado", null)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[0].code").value("UNKNOWN_PRODUCT"));
		this.mvc.perform(postJson("/api/v1/quotes", quote("acople-gran-rango-agr", "DN9999")))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[0].code").value("UNKNOWN_VARIANT"));
		Map<String, Object> badRuc = quote("vcp-f4-01", null);
		badRuc.put("contact", Map.of("companyName", "X SAC", "ruc", "20131312956", "contactName", "Ana", "email", "a@x.pe",
				"phone", "987654321"));
		this.mvc.perform(postJson("/api/v1/quotes", badRuc))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[0].field").value("contact.ruc"))
			.andExpect(jsonPath("$.errors[0].code").value("INVALID_RUC"));
	}

	@Test
	void salesHandlesTheQuoteWithOptimisticLocking() throws Exception {
		String folio = body(this.mvc.perform(postJson("/api/v1/quotes", quote("tub-ty-05", null))).andReturn()).get("folio").asString();
		String admin = adminToken();
		String id = this.jdbc.sql("SELECT id FROM quote_requests WHERE folio = :f").param("f", folio).query(String.class).single();
		String adminId = this.jdbc.sql("SELECT id FROM user_accounts WHERE email = :e").param("e", ADMIN_EMAIL).query(String.class).single();

		this.mvc.perform(authed(get("/api/v1/staff/quotes").param("q", folio).with(randomIp()), admin))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.items[0].folio").value(folio));
		this.mvc.perform(authed(get("/api/v1/staff/quotes/" + id).with(randomIp()), admin))
			.andExpect(status().isOk())
			.andExpect(header().string("ETag", "\"0\""));

		this.mvc.perform(authed(postJson("/api/v1/staff/quotes/" + id + "/assign", Map.of("assigneeId", adminId)), admin))
			.andExpect(status().isPreconditionRequired());
		this.mvc.perform(authed(postJson("/api/v1/staff/quotes/" + id + "/assign", Map.of("assigneeId", adminId)), admin)
			.header("If-Match", "\"7\"")).andExpect(status().isPreconditionFailed());
		this.mvc.perform(authed(postJson("/api/v1/staff/quotes/" + id + "/assign", Map.of("assigneeId", adminId)), admin)
			.header("If-Match", "\"0\""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("ASSIGNED"))
			.andExpect(header().string("ETag", "\"1\""));
		this.mvc.perform(authed(postJson("/api/v1/staff/quotes/" + id + "/status", Map.of("status", "CLOSED")), admin)
			.header("If-Match", "\"1\""))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("INVALID_TRANSITION"));
		this.mvc.perform(authed(postJson("/api/v1/staff/quotes/" + id + "/status", Map.of("status", "IN_REVIEW", "note", "Revisando stock")), admin)
			.header("If-Match", "\"1\""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.history.length()").value(3));
	}

	@Test
	void customersCannotReachTheSalesInbox() throws Exception {
		String customer = customerToken("cliente.cotiza@andina.pe", "una-clave-larga-segura");
		this.mvc.perform(authed(get("/api/v1/staff/quotes").with(randomIp()), customer))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.code").value("FORBIDDEN"));
		this.mvc.perform(authed(post("/api/v1/staff/quotes/00000000-0000-7000-8000-000000000000/status").with(randomIp()), customer))
			.andExpect(status().isForbidden());
	}

}
