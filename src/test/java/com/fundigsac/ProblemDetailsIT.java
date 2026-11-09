package com.fundigsac;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import com.fundigsac.support.IntegrationTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Todo error del marco web sale como RFC 9457 con código estable, sin depender del controlador que lo provoca. */
class ProblemDetailsIT extends IntegrationTest {

	@Test
	void anUnsupportedMethodHasACode() throws Exception {
		String customer = customerToken("metodo.no.permitido@empresa.pe", "frase-de-paso-muy-larga");
		this.mvc.perform(authed(put("/api/v1/account/me").with(randomIp()).contentType(MediaType.APPLICATION_JSON).content("{}"), customer))
			.andExpect(status().isMethodNotAllowed())
			.andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"))
			.andExpect(jsonPath("$.requestId").exists());
	}

	@Test
	void anUnsupportedMediaTypeHasACode() throws Exception {
		this.mvc.perform(post("/api/v1/inquiries").with(randomIp()).contentType(MediaType.TEXT_PLAIN).content("hola"))
			.andExpect(status().isUnsupportedMediaType())
			.andExpect(jsonPath("$.code").value("UNSUPPORTED_MEDIA_TYPE"));
	}

	@Test
	void anUnknownPublicRouteHasACode() throws Exception {
		this.mvc.perform(get("/api/v1/catalog/ruta/que/no/existe").with(randomIp()))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("NOT_FOUND"));
	}

	@Test
	void aWrongParameterTypeIsABadRequest() throws Exception {
		this.mvc.perform(get("/api/v1/catalog/products").param("dn", "abc").with(randomIp()))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("BAD_REQUEST"));
	}

	@Test
	void aWrongJsonTypeNamesTheField() throws Exception {
		this.mvc.perform(post("/api/v1/privacy/consents").with(randomIp()).contentType(MediaType.APPLICATION_JSON)
			.content("{\"consentId\":\"no-es-uuid\",\"choices\":{\"necessary\":true},\"policyVersion\":\"2026-10\",\"source\":\"BANNER\"}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("INVALID_VALUE"))
			.andExpect(jsonPath("$.errors[0].field").value("consentId"));
	}

	@Test
	void parameterConstraintsReportTheParameter() throws Exception {
		this.mvc.perform(authed(get("/api/v1/staff/quotes").param("size", "1000").with(randomIp()), adminToken()))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
			.andExpect(jsonPath("$.errors[0].field").value("size"))
			.andExpect(jsonPath("$.errors[0].code").value("OUT_OF_RANGE"));
	}

	@Test
	void aSafeRequestIdIsEchoedInHeaderAndBody() throws Exception {
		this.mvc.perform(post("/api/v1/inquiries").with(randomIp()).header("X-Request-Id", "traza-externa-123").contentType(MediaType.APPLICATION_JSON).content("{}"))
			.andExpect(status().isBadRequest())
			.andExpect(header().string("X-Request-Id", "traza-externa-123"))
			.andExpect(jsonPath("$.requestId").value("traza-externa-123"));
	}

	@Test
	void aDuplicateInvitationIsAConflictWithoutLeakingDetails() throws Exception {
		String admin = adminToken();
		Map<String, Object> invitation = Map.of("email", "duplicada.invitacion@fundigsac.test", "fullName", "Invitada Duplicada", "roles", java.util.List.of("SALES"));
		this.mvc.perform(authed(postJson("/api/v1/staff/users", invitation), admin)).andExpect(status().isCreated());
		this.mvc.perform(authed(postJson("/api/v1/staff/users", invitation), admin))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("EMAIL_IN_USE"));
	}

}
