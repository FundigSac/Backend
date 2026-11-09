package com.fundigsac.inquiry;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fundigsac.support.IntegrationTest;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Bandeja de consultas: filtros, permisos y asignación de responsable con If-Match. */
class StaffInquiryDeskIT extends IntegrationTest {

	private static final String BASE = "/api/v1/staff/inquiries";

	private static final String UNKNOWN = BASE + "/00000000-0000-7000-8000-00000000abcd";

	private String submit(String email) throws Exception {
		Map<String, Object> body = new HashMap<>();
		body.put("reason", "TECHNICAL");
		body.put("fullName", "Rosa Vargas");
		body.put("companyName", "Riego Sur SAC");
		body.put("email", email);
		body.put("phone", "987654321");
		body.put("message", "Necesito confirmar la presión de trabajo de la válvula DN150.");
		body.put("privacyNoticeVersion", "2026-10");
		body.put("acceptPrivacyNotice", true);
		return body(this.mvc.perform(postJson("/api/v1/inquiries", body)).andExpect(status().isCreated()).andReturn())
			.get("folio")
			.asString();
	}

	private String idOfFolio(String folio) {
		return this.jdbc.sql("SELECT CAST(id AS text) FROM inquiries WHERE folio = :f").param("f", folio).query(String.class).single();
	}

	@Test
	void inboxFiltersByFolioAndStatus() throws Exception {
		String readOnly = staffToken("lectura.consultas@fundigsac.test", "READ_ONLY");
		String folio = submit("rosa.bandeja@riegosur.pe");

		this.mvc.perform(authed(get(BASE).param("q", folio).with(randomIp()), readOnly))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.totalItems").value(1))
			.andExpect(jsonPath("$.items[0].folio").value(folio))
			.andExpect(jsonPath("$.items[0].status").value("RECEIVED"));
		this.mvc.perform(authed(get(BASE).param("status", "RECEIVED").param("size", "100").with(randomIp()), readOnly))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.items[*].status").value(everyItem(is("RECEIVED"))));
		this.mvc.perform(authed(get(BASE).param("status", "OTRO").with(randomIp()), readOnly))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[0].code").value("INVALID_VALUE"));
		this.mvc.perform(authed(get(BASE).param("size", "1000").with(randomIp()), readOnly)).andExpect(status().isBadRequest());
	}

	@Test
	void inboxRequiresPermission() throws Exception {
		String customer = customerToken("cliente.consultas@andina.pe", "frase-de-paso-muy-larga");
		this.mvc.perform(getAs(BASE, customer)).andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("FORBIDDEN"));
		this.mvc.perform(get(BASE).with(randomIp())).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
	}

	@Test
	void assignmentMovesTheInquiryToInProgress() throws Exception {
		String salesEmail = "ventas.consultas@fundigsac.test";
		String sales = staffToken(salesEmail, "SALES");
		String legalEmail = "legal.consultas@fundigsac.test";
		String legal = staffToken(legalEmail, "LEGAL_SUPPORT");
		String id = idOfFolio(submit("rosa.asignada@riegosur.pe"));
		String url = BASE + "/" + id + "/assign";
		Map<String, Object> toSales = Map.of("assigneeId", userId(salesEmail));

		this.mvc.perform(authed(postJson(url, toSales), sales))
			.andExpect(status().isPreconditionRequired())
			.andExpect(jsonPath("$.code").value("IF_MATCH_REQUIRED"));
		this.mvc.perform(authed(postJson(url, toSales), sales).header("If-Match", "\"99\"")).andExpect(status().isPreconditionFailed());
		// Atención Legal consulta la bandeja, pero no gestiona consultas.
		this.mvc.perform(authed(postJson(url, toSales), legal).header("If-Match", "\"0\"")).andExpect(status().isForbidden());
		this.mvc.perform(postJson(url, toSales).header("If-Match", "\"0\"")).andExpect(status().isUnauthorized());
		this.mvc.perform(authed(postJson(url, Map.of("assigneeId", userId(legalEmail))), sales).header("If-Match", "\"0\""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[0].code").value("INVALID_ASSIGNEE"));
		this.mvc.perform(authed(postJson(url, Map.of()), sales).header("If-Match", "\"0\""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[0].code").value("REQUIRED"));
		this.mvc.perform(authed(postJson(UNKNOWN + "/assign", toSales), sales).header("If-Match", "\"0\""))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("INQUIRY_NOT_FOUND"));

		this.mvc.perform(authed(postJson(url, toSales), sales).header("If-Match", "\"0\""))
			.andExpect(status().isOk())
			.andExpect(header().string("ETag", "\"1\""))
			.andExpect(jsonPath("$.status").value("IN_PROGRESS"))
			.andExpect(jsonPath("$.assigneeId").value(userId(salesEmail)));
	}

	@Test
	void closedInquiriesCannotBeAssigned() throws Exception {
		String admin = adminToken();
		String id = idOfFolio(submit("rosa.cerrada@riegosur.pe"));
		String url = BASE + "/" + id;
		for (String next : new String[] { "IN_PROGRESS", "ANSWERED", "CLOSED" }) {
			this.mvc.perform(authed(postJson(url + "/status", Map.of("status", next)), admin).header("If-Match", etagOf(url, admin)))
				.andExpect(status().isOk());
		}
		this.mvc.perform(authed(postJson(url + "/assign", Map.of("assigneeId", userId(ADMIN_EMAIL))), admin)
			.header("If-Match", etagOf(url, admin)))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("INVALID_TRANSITION"));
	}

}
