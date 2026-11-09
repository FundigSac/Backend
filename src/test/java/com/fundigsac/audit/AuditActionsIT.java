package com.fundigsac.audit;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import com.fundigsac.support.ApiFixtures;
import com.fundigsac.support.IntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Cada acción sensible deja exactamente un registro de auditoría, con el recurso y el actor correctos. */
class AuditActionsIT extends IntegrationTest {

	private static final String STAFF = "/api/v1/staff";

	private void assertAudited(String action, String resourceType, String resourceId, String actorId) {
		Awaitility.await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> assertThat(count("""
				SELECT count(*) FROM audit_events WHERE action = :action AND resource_type = :type AND resource_id = :id
				   AND actor_id IS NOT DISTINCT FROM CAST(:actor AS uuid)""",
				parameters(action, resourceType, resourceId, actorId))).as(action).isEqualTo(1));
	}

	private static Map<String, Object> parameters(String action, String type, String id, String actor) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("action", action);
		parameters.put("type", type);
		parameters.put("id", id);
		parameters.put("actor", actor);
		return parameters;
	}

	private String idOf(String table, String folio) {
		return this.jdbc.sql("SELECT CAST(id AS text) FROM " + table + " WHERE folio = :f").param("f", folio).query(String.class).single();
	}

	private String submit(String url, Object payload) throws Exception {
		return body(this.mvc.perform(postJson(url, payload)).andExpect(status().isCreated()).andReturn()).get("folio").asString();
	}

	@Test
	void customerLifecycleIsAudited() throws Exception {
		String email = "auditoria.cliente@empresa.pe";
		String token = customerToken(email, "frase-de-paso-muy-larga");
		String id = userId(email);
		assertAudited("EMAIL_VERIFIED", "USER", id, id);

		this.mvc.perform(authed(postJson("/api/v1/account/password", Map.of("currentPassword", "frase-de-paso-muy-larga", "newPassword", "otra-frase-de-paso-larga")), token))
			.andExpect(status().isNoContent());
		assertAudited("PASSWORD_CHANGED", "USER", id, id);

		String fresh = login(email, "otra-frase-de-paso-larga");
		this.mvc.perform(authed(delete("/api/v1/account").with(randomIp()).contentType("application/json")
			.content(this.json.writeValueAsString(Map.of("password", "otra-frase-de-paso-larga"))), fresh)).andExpect(status().isNoContent());
		assertAudited("ACCOUNT_DELETED", "USER", id, id);
	}

	@Test
	void repeatedFailedPasswordsAuditTheLock() throws Exception {
		String email = "auditoria.bloqueo@empresa.pe";
		customerToken(email, "frase-de-paso-muy-larga");
		for (int attempt = 0; attempt < 5; attempt++) {
			this.mvc.perform(postJson("/api/v1/auth/login", Map.of("email", email, "password", "clave-incorrecta-x"))).andExpect(status().isUnauthorized());
		}
		assertAudited("ACCOUNT_LOCKED", "USER", userId(email), null);
	}

	@Test
	void administrationOfAccountsIsAuditedUnderTheAdministrator() throws Exception {
		String admin = adminToken();
		String adminId = userId(ADMIN_EMAIL);
		String staffEmail = "auditoria.personal@fundigsac.test";
		this.mvc.perform(authed(postJson(STAFF + "/users", ApiFixtures.invitation(staffEmail, "SALES")), admin)).andExpect(status().isCreated());
		String staffId = userId(staffEmail);
		assertAudited("STAFF_INVITED", "USER", staffId, adminId);

		String etag = etagOf(STAFF + "/users/" + staffId, admin);
		this.mvc.perform(authed(putJson(STAFF + "/users/" + staffId + "/roles", Map.of("roles", List.of("READ_ONLY"))), admin).header("If-Match", etag))
			.andExpect(status().isOk());
		assertAudited("USER_ROLES_CHANGED", "USER", staffId, adminId);

		String customerEmail = "auditoria.gestion@empresa.pe";
		customerToken(customerEmail, "frase-de-paso-muy-larga");
		String customerId = userId(customerEmail);
		String accountEtag = etagOf(STAFF + "/accounts/" + customerId, admin);
		this.mvc.perform(authed(patchJson(STAFF + "/accounts/" + customerId, Map.of("fullName", "Nombre Corregido")), admin).header("If-Match", accountEtag))
			.andExpect(status().isOk());
		assertAudited("USER_UPDATED", "USER", customerId, adminId);

		String changedEtag = etagOf(STAFF + "/accounts/" + customerId, admin);
		this.mvc.perform(authed(post(STAFF + "/accounts/" + customerId + "/disable").with(randomIp()), admin).header("If-Match", changedEtag))
			.andExpect(status().isOk());
		assertAudited("USER_STATUS_CHANGED", "USER", customerId, adminId);
	}

	@Test
	void publicFormsAreAuditedWithoutAnActor() throws Exception {
		String complaint = submit("/api/v1/complaints", ApiFixtures.claim());
		assertAudited("COMPLAINT_FILED", "COMPLAINT", idOf("complaints", complaint), null);

		String inquiry = submit("/api/v1/inquiries", ApiFixtures.inquiry("auditoria.consulta@empresa.pe"));
		assertAudited("INQUIRY_RECEIVED", "INQUIRY", idOf("inquiries", inquiry), null);

		String rights = submit("/api/v1/privacy/requests", ApiFixtures.rights("auditoria.derechos@empresa.pe"));
		assertAudited("DSR_RECEIVED", "DATA_SUBJECT_REQUEST", idOf("data_subject_requests", rights), null);
	}

	@Test
	void legalActionsAreAuditedUnderTheStaffMember() throws Exception {
		String admin = adminToken();
		String adminId = userId(ADMIN_EMAIL);
		String folio = submit("/api/v1/complaints", ApiFixtures.claim());
		String complaintId = idOf("complaints", folio);
		this.mvc.perform(authed(postJson(STAFF + "/complaints/" + complaintId + "/answer", Map.of("response", "Se atendió el reclamo con el cambio de la válvula.")), admin)
			.header("If-Match", "\"0\"")).andExpect(status().isOk());
		assertAudited("COMPLAINT_ANSWERED", "COMPLAINT", complaintId, adminId);

		String version = "a-" + ApiFixtures.unique();
		this.mvc.perform(authed(postJson(STAFF + "/legal/b2b-terms/versions", ApiFixtures.legalVersion(version)), admin)).andExpect(status().isCreated());
		Awaitility.await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> assertThat(count(
				"SELECT count(*) FROM audit_events WHERE action = 'LEGAL_NOTICE_PUBLISHED' AND actor_id = CAST(:actor AS uuid) AND details->>'version' = :v",
				Map.of("actor", adminId, "v", version))).isEqualTo(1));
	}

}
