package com.fundigsac.complaint;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;

import com.fundigsac.support.IntegrationTest;
import tools.jackson.databind.JsonNode;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Libro de Reclamaciones: hoja con correlativo, constancia, respuesta y protección de la hoja en la base de datos. */
class ComplaintIT extends IntegrationTest {

	static Map<String, Object> claim() {
		Map<String, Object> body = new HashMap<>();
		body.put("kind", "CLAIM");
		body.put("consumerName", "Ana Pérez Rojas");
		body.put("documentType", "DNI");
		body.put("documentNumber", "45678912");
		body.put("email", "ana.perez@correo.pe");
		body.put("phone", "987654321");
		body.put("address", "Av. Los Próceres 456, Lima");
		body.put("minor", false);
		body.put("goodType", "PRODUCT");
		body.put("goodDescription", "Válvula de compuerta DN150");
		body.put("claimedAmount", 1500.50);
		body.put("currency", "PEN");
		body.put("incidentDate", LocalDate.now().minusDays(3).toString());
		body.put("facts", "La válvula llegó con la brida dañada y no cierra herméticamente.");
		body.put("consumerRequest", "Cambio de la válvula por una nueva.");
		body.put("responseChannel", "EMAIL");
		body.put("privacyNoticeVersion", "2026-10");
		body.put("acceptPrivacyNotice", true);
		return body;
	}

	@Test
	void filesClaimWithCorrelativeNumberAndDeadline() throws Exception {
		JsonNode receipt = body(this.mvc.perform(postJson("/api/v1/complaints", claim()))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.folio").value(matchesPattern("REC-\\d{4}-\\d{6}")))
			.andExpect(jsonPath("$.responseDueOn").exists())
			.andReturn());
		String folio = receipt.get("folio").asString();
		this.mvc.perform(get("/api/v1/complaints/" + folio + "/receipt").with(randomIp())
			.header("X-Receipt-Token", receipt.get("receiptToken").asString()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.document").value("DNI ****8912"))
			.andExpect(jsonPath("$.email").value("a***@correo.pe"))
			.andExpect(jsonPath("$.claimedAmount.amount").value(1500.50))
			.andExpect(jsonPath("$.history").isEmpty());
	}

	@Test
	void minorsNeedARepresentative() throws Exception {
		Map<String, Object> minor = claim();
		minor.put("minor", true);
		this.mvc.perform(postJson("/api/v1/complaints", minor))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[0].field").value("guardian"));
		Map<String, Object> future = claim();
		future.put("incidentDate", LocalDate.now().plusDays(2).toString());
		this.mvc.perform(postJson("/api/v1/complaints", future)).andExpect(status().isBadRequest());
	}

	@Test
	void legalAnswersOnceAndTheSheetCannotBeRewritten() throws Exception {
		String folio = body(this.mvc.perform(postJson("/api/v1/complaints", claim())).andReturn()).get("folio").asString();
		String id = this.jdbc.sql("SELECT id FROM complaints WHERE folio = :f").param("f", folio).query(String.class).single();
		String admin = adminToken();
		this.mvc.perform(authed(postJson("/api/v1/staff/complaints/" + id + "/answer",
				Map.of("response", "Reemplazaremos la válvula sin costo en un plazo de 5 días hábiles.")), admin)
			.header("If-Match", "\"0\""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("ANSWERED"))
			.andExpect(jsonPath("$.history[*].type").value(org.hamcrest.Matchers.contains("FILED", "ANSWERED")));
		this.mvc.perform(authed(postJson("/api/v1/staff/complaints/" + id + "/answer",
				Map.of("response", "Segunda respuesta que no debería registrarse nunca.")), admin)
			.header("If-Match", "\"1\""))
			.andExpect(status().isConflict());
		this.mvc.perform(authed(postJson("/api/v1/staff/complaints/" + id + "/close", Map.of()), admin).header("If-Match", "\"1\""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("CLOSED"));

		// La base de datos rechaza reescribir lo presentado por el consumidor, aunque la aplicación lo intentara.
		assertThatThrownBy(() -> this.jdbc.sql("UPDATE complaints SET facts = 'Texto alterado por un tercero' WHERE id = CAST(:id AS uuid)")
			.param("id", id)
			.update()).isInstanceOf(DataAccessException.class).hasMessageContaining("no se puede modificar");
	}

	@Test
	void staffInboxHighlightsOverdueSheets() throws Exception {
		this.mvc.perform(authed(get("/api/v1/staff/complaints").param("overdue", "true").with(randomIp()), adminToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.totalItems").value(0));
	}

}
