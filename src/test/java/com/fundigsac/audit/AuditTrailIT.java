package com.fundigsac.audit;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import com.fundigsac.support.IntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Consulta de la auditoría: filtros, validación de parámetros y permiso audit:read. */
class AuditTrailIT extends IntegrationTest {

	private static final String BASE = "/api/v1/staff/audit";

	@Test
	void filtersByResourceActionActorAndDate() throws Exception {
		String admin = adminToken();
		String adminId = userId(ADMIN_EMAIL);
		String folio = body(this.mvc.perform(postJson("/api/v1/quotes", com.fundigsac.quotation.QuotationFixtures.quote()))
			.andExpect(status().isCreated())
			.andReturn()).get("folio").asString();
		String quoteId = this.jdbc.sql("SELECT CAST(id AS text) FROM quote_requests WHERE folio = :f").param("f", folio).query(String.class).single();
		Awaitility.await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> assertThat(
				count("SELECT count(*) FROM audit_events WHERE action = 'QUOTE_SUBMITTED' AND details->>'folio' = :f", Map.of("f", folio))).isEqualTo(1));
		String resourceId = this.jdbc.sql("SELECT resource_id FROM audit_events WHERE action = 'QUOTE_SUBMITTED' AND details->>'folio' = :f")
			.param("f", folio)
			.query(String.class)
			.single();

		// resourceType y action se normalizan a mayúsculas.
		this.mvc.perform(authed(get(BASE).param("resourceType", "quote").param("action", "quote_submitted")
			.param("resourceId", resourceId).with(randomIp()), admin))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.totalItems").value(1))
			.andExpect(jsonPath("$.items[0].resourceType").value("QUOTE"))
			.andExpect(jsonPath("$.items[0].action").value("QUOTE_SUBMITTED"))
			.andExpect(jsonPath("$.items[0].details.folio").value(folio));
		assertThat(resourceId).isNotBlank();
		assertThat(quoteId).isNotBlank();

		this.mvc.perform(authed(get(BASE).param("resourceType", "QUOTE").param("from", "2030-01-01T00:00:00Z").with(randomIp()), admin))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.totalItems").value(0));
		this.mvc.perform(authed(get(BASE).param("to", Instant.now().plus(Duration.ofDays(1)).toString()).param("size", "100").with(randomIp()), admin))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.items").isNotEmpty());

		// Las acciones del administrador quedan a su nombre.
		this.mvc.perform(authed(postJson("/api/v1/staff/users",
				Map.of("email", "auditado@fundigsac.test", "fullName", "Persona Auditada", "roles", java.util.List.of("SALES"))), admin))
			.andExpect(status().isCreated());
		Awaitility.await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> this.mvc
			.perform(authed(get(BASE).param("actorId", adminId).param("resourceType", "USER").param("size", "100").with(randomIp()), admin))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.items").isNotEmpty())
			.andExpect(jsonPath("$.items[*].actorId").value(everyItem(is(adminId)))));
	}

	@Test
	void invalidFiltersAreRejected() throws Exception {
		String admin = adminToken();
		this.mvc.perform(authed(get(BASE).param("actorId", "no-es-uuid").with(randomIp()), admin)).andExpect(status().isBadRequest());
		this.mvc.perform(authed(get(BASE).param("from", "ayer").with(randomIp()), admin)).andExpect(status().isBadRequest());
		this.mvc.perform(authed(get(BASE).param("size", "101").with(randomIp()), admin))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
		this.mvc.perform(authed(get(BASE).param("page", "-1").with(randomIp()), admin)).andExpect(status().isBadRequest());
		this.mvc.perform(authed(get(BASE).param("action", "x".repeat(61)).with(randomIp()), admin)).andExpect(status().isBadRequest());
	}

	@Test
	void auditIsOnlyForAuditReaders() throws Exception {
		String sales = staffToken("ventas.auditoria@fundigsac.test", "SALES");
		String customer = customerToken("cliente.auditoria@andina.pe", "frase-de-paso-muy-larga");
		this.mvc.perform(getAs(BASE, sales)).andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("FORBIDDEN"));
		this.mvc.perform(getAs(BASE, customer)).andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("FORBIDDEN"));
		this.mvc.perform(get(BASE).with(randomIp())).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
	}

}
