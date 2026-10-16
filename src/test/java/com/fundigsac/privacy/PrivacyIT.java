package com.fundigsac.privacy;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fundigsac.support.IntegrationTest;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Textos legales versionados, evidencia de consentimiento y derechos del titular. */
class PrivacyIT extends IntegrationTest {

	@Test
	void servesCurrentLegalNoticesAndPublishesNewVersions() throws Exception {
		this.mvc.perform(get("/api/v1/legal/privacy-policy").with(randomIp()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.version").value("2026-10"))
			.andExpect(jsonPath("$.contentUrl").value("/privacidad"));
		this.mvc.perform(get("/api/v1/legal/no-existe").with(randomIp())).andExpect(status().isNotFound());

		Map<String, Object> notice = new HashMap<>();
		notice.put("version", "2026-11");
		notice.put("title", "Términos B2B");
		notice.put("contentUrl", "/terminos-b2b");
		notice.put("body", "Texto completo de los términos comerciales.");
		notice.put("effectiveFrom", Instant.parse("2030-01-01T05:00:00Z").toString());
		this.mvc.perform(authed(postJson("/api/v1/staff/legal/b2b-terms/versions", notice), adminToken()))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.contentSha256").value(matchesPattern("[0-9a-f]{64}")));
		// Aún no está vigente: la versión actual sigue siendo la anterior.
		this.mvc.perform(get("/api/v1/legal/b2b-terms").with(randomIp())).andExpect(jsonPath("$.version").value("2026-10"));
		this.mvc.perform(authed(postJson("/api/v1/staff/legal/b2b-terms/versions", notice), adminToken()))
			.andExpect(status().isConflict());
	}

	@Test
	void recordsConsentEvidenceWithoutTrackingData() throws Exception {
		this.mvc.perform(postJson("/api/v1/privacy/consents", Map.of("choices", Map.of("necessary", true, "thirdParty", false),
				"policyVersion", "2026-10", "source", "BANNER")))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.consentId").exists());
		this.mvc.perform(postJson("/api/v1/privacy/consents", Map.of("choices", Map.of("necessary", false, "thirdParty", true),
				"policyVersion", "2026-10", "source", "BANNER")))
			.andExpect(status().isBadRequest());
		this.mvc.perform(postJson("/api/v1/privacy/consents", Map.of("choices", Map.of("necessary", true, "thirdParty", true),
				"policyVersion", "1999-01", "source", "BANNER")))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[0].code").value("UNKNOWN_POLICY_VERSION"));
	}

	@Test
	void handlesDataSubjectRequests() throws Exception {
		Map<String, Object> request = new HashMap<>();
		request.put("type", "ACCOUNT_DELETION");
		request.put("fullName", "Jorge Ramos");
		request.put("email", "jorge@correo.pe");
		request.put("details", "Solicito eliminar los datos asociados a mi correo.");
		request.put("acceptPrivacyNotice", true);
		String folio = body(this.mvc.perform(postJson("/api/v1/privacy/requests", request))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.folio").value(matchesPattern("ARC-\\d{4}-\\d{6}")))
			.andReturn()).get("folio").asString();
		String id = this.jdbc.sql("SELECT id FROM data_subject_requests WHERE folio = :f").param("f", folio).query(String.class).single();
		this.mvc.perform(authed(postJson("/api/v1/staff/privacy/requests/" + id + "/conclude",
				Map.of("accepted", true, "resolutionNote", "Datos eliminados de nuestros registros comerciales.")), adminToken())
			.header("If-Match", "\"0\""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("RESOLVED"));
	}

}
