package com.fundigsac;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.fundigsac.support.IntegrationTest;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Controles transversales: cabeceras, CORS, autenticación, errores RFC 9457, tamaño de cuerpo y límites. */
class PlatformSecurityIT extends IntegrationTest {

	private static Map<String, Object> inquiry() {
		Map<String, Object> body = new HashMap<>();
		body.put("reason", "TECHNICAL");
		body.put("fullName", "María Quispe");
		body.put("companyName", "Agua Limpia SAC");
		body.put("email", "maria@agualimpia.pe");
		body.put("phone", "987654321");
		body.put("message", "Necesito la ficha técnica de la válvula de compuerta DN200.");
		body.put("privacyNoticeVersion", "2026-10");
		body.put("acceptPrivacyNotice", true);
		return body;
	}

	@Test
	void responsesCarrySecurityHeaders() throws Exception {
		this.mvc.perform(get("/api/v1/catalog/categories").with(randomIp()))
			.andExpect(status().isOk())
			.andExpect(header().string("Content-Security-Policy", containsString("default-src 'none'")))
			.andExpect(header().string("X-Content-Type-Options", "nosniff"))
			.andExpect(header().string("X-Frame-Options", "DENY"))
			.andExpect(header().string("Referrer-Policy", "no-referrer"))
			.andExpect(header().string("Cross-Origin-Resource-Policy", "same-origin"))
			.andExpect(header().string("Permissions-Policy", containsString("camera=()")))
			.andExpect(header().exists("X-Request-Id"));
	}

	@Test
	void protectedAreasRequireAuthentication() throws Exception {
		this.mvc.perform(get("/api/v1/staff/quotes").with(randomIp()))
			.andExpect(status().isUnauthorized())
			.andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"))
			.andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
		this.mvc.perform(get("/api/v1/account/me").with(randomIp())).andExpect(status().isUnauthorized());
		this.mvc.perform(get("/api/v1/staff/quotes").with(randomIp()).header(HttpHeaders.AUTHORIZATION, "Bearer x.y.z"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("INVALID_TOKEN"));
		// Todo lo no declarado se niega.
		this.mvc.perform(get("/api/v1/inexistente").with(randomIp())).andExpect(status().isUnauthorized());
	}

	@Test
	void suspiciousUrlsAreRejectedAsProblems() throws Exception {
		// Con java.net.URI la ruta llega sin normalizar (una plantilla de texto colapsaría la doble barra).
		this.mvc.perform(get(java.net.URI.create("/api/v1/quotes//receipt")).with(randomIp()))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("REQUEST_REJECTED"));
	}

	@Test
	void corsAllowsOnlyConfiguredOrigins() throws Exception {
		this.mvc.perform(options("/api/v1/quotes").with(randomIp())
			.header(HttpHeaders.ORIGIN, "http://localhost:3000")
			.header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
			.header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "content-type,idempotency-key"))
			.andExpect(status().isOk())
			.andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:3000"));
		this.mvc.perform(options("/api/v1/quotes").with(randomIp())
			.header(HttpHeaders.ORIGIN, "https://sitio-malicioso.example")
			.header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
			.andExpect(status().isForbidden());
	}

	@Test
	void invalidInputIsRejectedWithProblemDetails() throws Exception {
		Map<String, Object> missing = inquiry();
		missing.remove("email");
		missing.put("phone", "abc");
		this.mvc.perform(postJson("/api/v1/inquiries", missing))
			.andExpect(status().isBadRequest())
			.andExpect(header().string(HttpHeaders.CONTENT_TYPE, containsString("application/problem+json")))
			.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
			.andExpect(jsonPath("$.errors[?(@.field == 'email')].code").value("REQUIRED"))
			.andExpect(jsonPath("$.requestId").exists());

		Map<String, Object> unknown = inquiry();
		unknown.put("isAdmin", true);
		this.mvc.perform(postJson("/api/v1/inquiries", unknown))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("UNKNOWN_FIELD"));

		this.mvc.perform(post("/api/v1/inquiries").with(randomIp()).contentType(MediaType.APPLICATION_JSON).content("{roto"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));

		Map<String, Object> bot = inquiry();
		bot.put("website", "http://spam.example");
		this.mvc.perform(postJson("/api/v1/inquiries", bot)).andExpect(status().isBadRequest());
	}

	@Test
	void oversizedBodiesAreRejected() throws Exception {
		Map<String, Object> huge = inquiry();
		huge.put("message", "x".repeat(70_000));
		this.mvc.perform(postJson("/api/v1/inquiries", huge))
			.andExpect(status().isContentTooLarge())
			.andExpect(jsonPath("$.code").value("PAYLOAD_TOO_LARGE"));
	}

	@Test
	void publicFormsAreRateLimitedPerIp() throws Exception {
		RequestPostProcessor sameIp = (request) -> {
			request.setRemoteAddr("192.0.2.77");
			return request;
		};
		for (int i = 0; i < 5; i++) {
			this.mvc.perform(post("/api/v1/inquiries").with(sameIp)
				.contentType(MediaType.APPLICATION_JSON)
				.content(this.json.writeValueAsString(inquiry()))).andExpect(status().isCreated());
		}
		this.mvc.perform(post("/api/v1/inquiries").with(sameIp)
			.contentType(MediaType.APPLICATION_JSON)
			.content(this.json.writeValueAsString(inquiry())))
			.andExpect(status().isTooManyRequests())
			.andExpect(header().exists(HttpHeaders.RETRY_AFTER))
			.andExpect(jsonPath("$.code").value("RATE_LIMITED"));
	}

}
