package com.fundigsac.complaint;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import com.fundigsac.support.IntegrationTest;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Bandeja legal: hoja completa, asignación de responsable y notas internas con If-Match y permisos. */
class StaffComplaintDeskIT extends IntegrationTest {

	private static final String BASE = "/api/v1/staff/complaints";

	private static final String UNKNOWN = BASE + "/00000000-0000-7000-8000-00000000abcd";

	private String file() throws Exception {
		String folio = body(this.mvc.perform(postJson("/api/v1/complaints", ComplaintIT.claim())).andExpect(status().isCreated()).andReturn())
			.get("folio")
			.asString();
		return this.jdbc.sql("SELECT CAST(id AS text) FROM complaints WHERE folio = :f").param("f", folio).query(String.class).single();
	}

	private static String etag(MvcResult result) {
		return result.getResponse().getHeader("ETag");
	}

	@Test
	void legalReadsTheFullSheet() throws Exception {
		String legal = staffToken("legal.hoja@fundigsac.test", "LEGAL_SUPPORT");
		String id = file();

		this.mvc.perform(getAs(BASE + "/" + id, legal))
			.andExpect(status().isOk())
			.andExpect(header().string("ETag", "\"0\""))
			.andExpect(header().string("Cache-Control", "no-store"))
			.andExpect(jsonPath("$.document").value("DNI 45678912"))
			.andExpect(jsonPath("$.email").value("ana.perez@correo.pe"))
			.andExpect(jsonPath("$.history[0].type").value("FILED"));
		this.mvc.perform(getAs(UNKNOWN, legal))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("COMPLAINT_NOT_FOUND"));
		this.mvc.perform(getAs(BASE + "/no-es-uuid", legal)).andExpect(status().isBadRequest());
	}

	@Test
	void theSheetIsOnlyForComplaintReaders() throws Exception {
		String sales = staffToken("ventas.hoja@fundigsac.test", "SALES");
		String customer = customerToken("cliente.hoja@andina.pe", "frase-de-paso-muy-larga");
		String id = file();
		this.mvc.perform(getAs(BASE + "/" + id, sales)).andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("FORBIDDEN"));
		this.mvc.perform(getAs(BASE + "/" + id, customer)).andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("FORBIDDEN"));
		this.mvc.perform(get(BASE + "/" + id).with(randomIp())).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
	}

	@Test
	void assignmentMovesTheSheetToReview() throws Exception {
		String email = "legal.asignada@fundigsac.test";
		String legal = staffToken(email, "LEGAL_SUPPORT");
		String readOnly = staffToken("lectura.asignacion@fundigsac.test", "READ_ONLY");
		String salesEmail = "ventas.asignacion@fundigsac.test";
		staffToken(salesEmail, "SALES");
		String id = file();
		String url = BASE + "/" + id + "/assign";
		Map<String, Object> toLegal = Map.of("assigneeId", userId(email));

		this.mvc.perform(authed(postJson(url, toLegal), legal))
			.andExpect(status().isPreconditionRequired())
			.andExpect(jsonPath("$.code").value("IF_MATCH_REQUIRED"));
		this.mvc.perform(authed(postJson(url, toLegal), legal).header("If-Match", "\"99\"")).andExpect(status().isPreconditionFailed());
		// Quien solo consulta no asigna, y Ventas no puede recibir hojas del Libro de Reclamaciones.
		this.mvc.perform(authed(postJson(url, toLegal), readOnly).header("If-Match", "\"0\"")).andExpect(status().isForbidden());
		this.mvc.perform(postJson(url, toLegal).header("If-Match", "\"0\"")).andExpect(status().isUnauthorized());
		this.mvc.perform(authed(postJson(url, Map.of("assigneeId", userId(salesEmail))), legal).header("If-Match", "\"0\""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[0].code").value("INVALID_ASSIGNEE"));
		this.mvc.perform(authed(postJson(url, Map.of()), legal).header("If-Match", "\"0\""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[0].field").value("assigneeId"))
			.andExpect(jsonPath("$.errors[0].code").value("REQUIRED"));
		this.mvc.perform(authed(postJson(UNKNOWN + "/assign", toLegal), legal).header("If-Match", "\"0\"")).andExpect(status().isNotFound());

		this.mvc.perform(authed(postJson(url, toLegal), legal).header("If-Match", "\"0\""))
			.andExpect(status().isOk())
			.andExpect(header().string("ETag", "\"1\""))
			.andExpect(jsonPath("$.status").value("IN_REVIEW"))
			.andExpect(jsonPath("$.assigneeId").value(userId(email)))
			.andExpect(jsonPath("$.history[*].type").value(hasItem("ASSIGNED")));
	}

	@Test
	void closedSheetsCannotBeReassigned() throws Exception {
		String email = "legal.cerrada@fundigsac.test";
		String legal = staffToken(email, "LEGAL_SUPPORT");
		String id = file();
		String answered = etag(this.mvc.perform(authed(postJson(BASE + "/" + id + "/answer",
				Map.of("response", "Reemplazaremos la válvula sin costo en un plazo de 5 días hábiles.")), legal)
			.header("If-Match", "\"0\"")).andExpect(status().isOk()).andReturn());
		String closed = etag(this.mvc.perform(authed(postJson(BASE + "/" + id + "/close", Map.of()), legal).header("If-Match", answered))
			.andExpect(status().isOk())
			.andReturn());
		this.mvc.perform(authed(postJson(BASE + "/" + id + "/assign", Map.of("assigneeId", userId(email))), legal).header("If-Match", closed))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("ALREADY_ANSWERED"));
	}

	@Test
	void internalNotesAreRecordedInTheHistory() throws Exception {
		String legal = staffToken("legal.notas@fundigsac.test", "LEGAL_SUPPORT");
		String readOnly = staffToken("lectura.notas@fundigsac.test", "READ_ONLY");
		String id = file();
		String url = BASE + "/" + id + "/notes";
		Map<String, Object> note = Map.of("note", "Se pidió la guía de remisión al almacén.");

		this.mvc.perform(authed(postJson(url, note), legal)).andExpect(status().isPreconditionRequired());
		this.mvc.perform(authed(postJson(url, note), legal).header("If-Match", "\"99\"")).andExpect(status().isPreconditionFailed());
		this.mvc.perform(authed(postJson(url, note), readOnly).header("If-Match", "\"0\"")).andExpect(status().isForbidden());
		this.mvc.perform(postJson(url, note).header("If-Match", "\"0\"")).andExpect(status().isUnauthorized());
		this.mvc.perform(authed(postJson(url, Map.of("note", "x")), legal).header("If-Match", "\"0\""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[0].field").value("note"));
		this.mvc.perform(authed(postJson(UNKNOWN + "/notes", note), legal).header("If-Match", "\"0\"")).andExpect(status().isNotFound());

		this.mvc.perform(authed(postJson(url, note), legal).header("If-Match", "\"0\""))
			.andExpect(status().isOk())
			.andExpect(header().string("ETag", "\"1\""))
			.andExpect(jsonPath("$.history[*].type").value(hasItem("NOTE")))
			.andExpect(jsonPath("$.history[*].note").value(hasItem("Se pidió la guía de remisión al almacén.")));
		// La versión cambió: una segunda nota con la versión anterior no pisa la primera.
		this.mvc.perform(authed(postJson(url, note), legal).header("If-Match", "\"0\"")).andExpect(status().isPreconditionFailed());
	}

}
