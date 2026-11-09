package com.fundigsac.privacy;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fundigsac.support.IntegrationTest;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Historial público de textos legales y bandeja de solicitudes de derechos (consulta y paso a trámite). */
class StaffPrivacyDeskIT extends IntegrationTest {

	private static final String BASE = "/api/v1/staff/privacy/requests";

	private static final String UNKNOWN = BASE + "/00000000-0000-7000-8000-00000000abcd";

	private String submit(String email) throws Exception {
		Map<String, Object> request = new HashMap<>();
		request.put("type", "ACCESS");
		request.put("fullName", "Jorge Ramos");
		request.put("email", email);
		request.put("details", "Solicito conocer los datos personales que conservan de mi persona.");
		request.put("acceptPrivacyNotice", true);
		String folio = body(this.mvc.perform(postJson("/api/v1/privacy/requests", request)).andExpect(status().isCreated()).andReturn())
			.get("folio")
			.asString();
		return this.jdbc.sql("SELECT CAST(id AS text) FROM data_subject_requests WHERE folio = :f").param("f", folio).query(String.class).single();
	}

	@Test
	void legalHistoryListsAllPublishedVersions() throws Exception {
		Map<String, Object> notice = new HashMap<>();
		notice.put("version", "2031-01");
		notice.put("title", "Política de cookies futura");
		notice.put("contentUrl", "/cookies");
		notice.put("effectiveFrom", Instant.parse("2031-01-01T05:00:00Z").toString());
		this.mvc.perform(authed(postJson("/api/v1/staff/legal/cookie-policy/versions", notice), adminToken()))
			.andExpect(status().isCreated());

		this.mvc.perform(get("/api/v1/legal/cookie-policy/versions").with(randomIp()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[*].version").value(containsInAnyOrder("2026-10", "2031-01")))
			.andExpect(jsonPath("$[*].type").value(everyItem(is("cookie-policy"))));
		this.mvc.perform(get("/api/v1/legal/no-existe/versions").with(randomIp()))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("LEGAL_NOTICE_NOT_FOUND"));
		this.mvc.perform(get("/api/v1/legal/" + "x".repeat(31) + "/versions").with(randomIp()))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
	}

	@Test
	void privacyInboxFiltersByStatus() throws Exception {
		String legal = staffToken("legal.privacidad@fundigsac.test", "LEGAL_SUPPORT");
		String id = submit("titular.bandeja@correo.pe");

		this.mvc.perform(authed(get(BASE).param("status", "RECEIVED").param("size", "100").with(randomIp()), legal))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.items[*].status").value(everyItem(is("RECEIVED"))))
			.andExpect(jsonPath("$.items[*].id").value(hasItem(id)));
		this.mvc.perform(authed(get(BASE).param("status", "OTRO").with(randomIp()), legal))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[0].code").value("INVALID_VALUE"));
		this.mvc.perform(authed(get(BASE).param("size", "1000").with(randomIp()), legal)).andExpect(status().isBadRequest());
	}

	@Test
	void requestDetailIsPrivateAndCarriesTheVersion() throws Exception {
		String legal = staffToken("legal.detalle.arco@fundigsac.test", "LEGAL_SUPPORT");
		String sales = staffToken("ventas.detalle.arco@fundigsac.test", "SALES");
		String customer = customerToken("cliente.arco@andina.pe", "frase-de-paso-muy-larga");
		String id = submit("titular.detalle@correo.pe");

		this.mvc.perform(getAs(BASE + "/" + id, legal))
			.andExpect(status().isOk())
			.andExpect(header().string("ETag", "\"0\""))
			.andExpect(header().string("Cache-Control", "no-store"))
			.andExpect(jsonPath("$.type").value("ACCESS"))
			.andExpect(jsonPath("$.email").value("titular.detalle@correo.pe"));
		this.mvc.perform(getAs(UNKNOWN, legal))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("REQUEST_NOT_FOUND"));
		this.mvc.perform(getAs(BASE + "/" + id, sales)).andExpect(status().isForbidden());
		this.mvc.perform(getAs(BASE, sales)).andExpect(status().isForbidden());
		this.mvc.perform(getAs(BASE + "/" + id, customer)).andExpect(status().isForbidden());
		this.mvc.perform(get(BASE + "/" + id).with(randomIp())).andExpect(status().isUnauthorized());
		this.mvc.perform(get(BASE).with(randomIp())).andExpect(status().isUnauthorized());
	}

	@Test
	void startingARequestUsesOptimisticLocking() throws Exception {
		String legal = staffToken("legal.tramite@fundigsac.test", "LEGAL_SUPPORT");
		String sales = staffToken("ventas.tramite@fundigsac.test", "SALES");
		String id = submit("titular.tramite@correo.pe");
		String url = BASE + "/" + id + "/start";

		this.mvc.perform(authed(postJson(url, Map.of()), legal))
			.andExpect(status().isPreconditionRequired())
			.andExpect(jsonPath("$.code").value("IF_MATCH_REQUIRED"));
		this.mvc.perform(authed(postJson(url, Map.of()), legal).header("If-Match", "\"99\"")).andExpect(status().isPreconditionFailed());
		this.mvc.perform(authed(postJson(url, Map.of()), sales).header("If-Match", "\"0\"")).andExpect(status().isForbidden());
		this.mvc.perform(postJson(url, Map.of()).header("If-Match", "\"0\"")).andExpect(status().isUnauthorized());
		this.mvc.perform(authed(postJson(UNKNOWN + "/start", Map.of()), legal).header("If-Match", "\"0\"")).andExpect(status().isNotFound());

		this.mvc.perform(authed(postJson(url, Map.of()), legal).header("If-Match", "\"0\""))
			.andExpect(status().isOk())
			.andExpect(header().string("ETag", "\"1\""))
			.andExpect(jsonPath("$.status").value("IN_PROGRESS"));
		this.mvc.perform(authed(postJson(url, Map.of()), legal).header("If-Match", "\"1\""))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("INVALID_TRANSITION"));
	}

}
