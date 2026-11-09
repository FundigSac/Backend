package com.fundigsac.complaint;

import java.time.Duration;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;

import com.fundigsac.shared.domain.PeruTime;
import com.fundigsac.support.IntegrationTest;
import tools.jackson.databind.JsonNode;

import static org.assertj.core.api.Assertions.assertThat;
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
		body.put("incidentDate", LocalDate.now(PeruTime.ZONE).minusDays(3).toString());
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
			.andExpect(jsonPath("$.phone").value("***321"))
			.andExpect(jsonPath("$.address").value("***"))
			.andExpect(jsonPath("$.claimedAmount.amount").value(1500.50))
			.andExpect(jsonPath("$.history").isEmpty());
	}

	@Test
	void theReceiptNeverShowsTheFullPersonalData() throws Exception {
		JsonNode receipt = body(this.mvc.perform(postJson("/api/v1/complaints", claim())).andExpect(status().isCreated()).andReturn());
		String sheet = this.mvc.perform(get("/api/v1/complaints/" + receipt.get("folio").asString() + "/receipt").with(randomIp())
			.header("X-Receipt-Token", receipt.get("receiptToken").asString()))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();
		assertThat(sheet).doesNotContain("45678912", "ana.perez@correo.pe", "987654321", "Próceres");
	}

	@Test
	void minorsNeedARepresentative() throws Exception {
		Map<String, Object> minor = claim();
		minor.put("minor", true);
		this.mvc.perform(postJson("/api/v1/complaints", minor))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[0].field").value("guardian"));
		Map<String, Object> future = claim();
		future.put("incidentDate", LocalDate.now(PeruTime.ZONE).plusDays(2).toString());
		this.mvc.perform(postJson("/api/v1/complaints", future)).andExpect(status().isBadRequest());
	}

	@Test
	void aMinorFilesThroughARepresentativeWhoAppearsOnTheSheet() throws Exception {
		Map<String, Object> minor = claim();
		minor.put("minor", true);
		minor.put("guardianName", "Rosa Rojas Díaz");
		minor.put("guardianDocumentType", "DNI");
		minor.put("guardianDocumentNumber", "41234567");
		JsonNode receipt = body(this.mvc.perform(postJson("/api/v1/complaints", minor)).andExpect(status().isCreated()).andReturn());
		String id = this.jdbc.sql("SELECT id FROM complaints WHERE folio = :f").param("f", receipt.get("folio").asString()).query(String.class).single();

		this.mvc.perform(authed(get("/api/v1/staff/complaints/" + id).with(randomIp()), adminToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.minor").value(true))
			.andExpect(jsonPath("$.guardianName").value("Rosa Rojas Díaz"))
			.andExpect(jsonPath("$.guardianDocument").value(org.hamcrest.Matchers.containsString("4567")));

		Map<String, Object> incomplete = claim();
		incomplete.put("minor", true);
		incomplete.put("guardianName", "Rosa Rojas Díaz");
		this.mvc.perform(postJson("/api/v1/complaints", incomplete)).andExpect(status().isBadRequest());
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
	void anAnswerByPostIsAnnouncedToLegalInsteadOfTheConsumer() throws Exception {
		Map<String, Object> byPost = claim();
		byPost.put("responseChannel", "POSTAL_ADDRESS");
		String folio = body(this.mvc.perform(postJson("/api/v1/complaints", byPost)).andExpect(status().isCreated()).andReturn()).get("folio").asString();
		String id = this.jdbc.sql("SELECT id FROM complaints WHERE folio = :f").param("f", folio).query(String.class).single();
		this.mvc.perform(authed(postJson("/api/v1/staff/complaints/" + id + "/answer",
				Map.of("response", "La respuesta se envía por carta a la dirección indicada.")), adminToken()).header("If-Match", "\"0\""))
			.andExpect(status().isOk());
		Awaitility.await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> assertThat(this.jdbc
			.sql("SELECT recipient FROM email_outbox WHERE reference = :f AND template = 'COMPLAINT_ANSWER_POST'")
			.param("f", folio)
			.query(String.class)
			.list()).containsExactly("legal@fundigsac.local"));
		assertThat(count("SELECT count(*) FROM email_outbox WHERE reference = :f AND template = 'COMPLAINT_ANSWER'", Map.of("f", folio))).isZero();
	}

	@Test
	void staffInboxHighlightsOverdueSheets() throws Exception {
		Map<String, Object> vencida = claim();
		vencida.put("consumerName", "Hoja Vencida Bandeja");
		String overdueFolio = body(this.mvc.perform(postJson("/api/v1/complaints", vencida)).andExpect(status().isCreated()).andReturn())
			.get("folio").asString();
		Map<String, Object> vigente = claim();
		vigente.put("consumerName", "Hoja Vencida Bandeja");
		String onTimeFolio = body(this.mvc.perform(postJson("/api/v1/complaints", vigente)).andExpect(status().isCreated()).andReturn())
			.get("folio").asString();
		Map<String, Object> atendida = claim();
		atendida.put("consumerName", "Hoja Vencida Bandeja");
		String answeredFolio = body(this.mvc.perform(postJson("/api/v1/complaints", atendida)).andExpect(status().isCreated()).andReturn())
			.get("folio").asString();
		String admin = adminToken();
		String answeredId = this.jdbc.sql("SELECT id FROM complaints WHERE folio = :f").param("f", answeredFolio).query(String.class).single();
		this.mvc.perform(authed(postJson("/api/v1/staff/complaints/" + answeredId + "/answer",
				Map.of("response", "Se atendió dentro del plazo con el cambio de la válvula.")), admin).header("If-Match", "\"0\""))
			.andExpect(status().isOk());
		// La aplicación calcula los vencimientos con la fecha de Lima; la prueba no puede usar CURRENT_DATE, que depende de la
		// zona de la sesión. El trigger impide reescribir el plazo: solo aquí, como superusuario de pruebas, se apaga para
		// simular el paso del tiempo. La hoja vigente vence hoy: es el último día en plazo.
		LocalDate today = LocalDate.now(PeruTime.ZONE);
		this.jdbc.sql("""
				DO $$ BEGIN
				  SET LOCAL session_replication_role = replica;
				  UPDATE complaints SET filed_at = filed_at - interval '30 days', response_due_on = DATE '%s' WHERE folio IN ('%s', '%s');
				  UPDATE complaints SET filed_at = filed_at - interval '30 days', response_due_on = DATE '%s' WHERE folio = '%s';
				END $$""".formatted(today.minusDays(1), overdueFolio, answeredFolio, today, onTimeFolio)).update();

		// Solo la hoja sin atender y con plazo vencido; una respondida o que vence hoy no se marca.
		this.mvc.perform(authed(get("/api/v1/staff/complaints").param("overdue", "true").param("q", "Hoja Vencida Bandeja").with(randomIp()), admin))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.totalItems").value(1))
			.andExpect(jsonPath("$.items[0].folio").value(overdueFolio));
		this.mvc.perform(authed(get("/api/v1/staff/complaints").param("q", "Hoja Vencida Bandeja").with(randomIp()), admin))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.totalItems").value(3))
			.andExpect(jsonPath("$.items[*].folio").value(org.hamcrest.Matchers.hasItem(onTimeFolio)));
	}

}
