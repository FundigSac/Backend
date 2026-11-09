package com.fundigsac;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.fundigsac.shared.web.RateLimited;
import com.fundigsac.support.IntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Cada operación con límite de frecuencia responde 429 justo al superar su política. El contador se consume antes de
 * validar el cuerpo, así que un cuerpo vacío basta y la prueba no deja datos.
 */
class RateLimitPoliciesIT extends IntegrationTest {

	private record Limited(String method, String path, String policy, int capacity, boolean customerSession) {

		@Override
		public String toString() {
			return this.method + " " + this.path + " (" + this.policy + ")";
		}

	}

	/** Segundos para recuperar una petición (periodo / capacidad). Escrito a mano: es lo que promete el README. */
	private static final Map<String, Long> SECONDS_PER_TOKEN = Map.of("public-form", 120L, "register", 720L, "login", 90L, "password-reset", 1200L,
			"token", 90L, "login-code", 180L, "receipt", 2L, "consent", 2L);

	private static final AtomicInteger NEXT_IP = new AtomicInteger(1);

	private static final String AUTH = "/api/v1/auth";

	@Autowired
	@Qualifier("requestMappingHandlerMapping")
	RequestMappingHandlerMapping mappings;

	static Stream<Limited> everyLimitedOperation() {
		return Stream.of(new Limited("POST", "/api/v1/quotes", "public-form", 5, false),
				new Limited("POST", "/api/v1/complaints", "public-form", 5, false),
				new Limited("POST", "/api/v1/inquiries", "public-form", 5, false),
				new Limited("POST", "/api/v1/privacy/requests", "public-form", 5, false),
				new Limited("POST", "/api/v1/cart/submit", "public-form", 5, true),
				new Limited("POST", AUTH + "/register", "register", 5, false), new Limited("POST", AUTH + "/login", "login", 10, false),
				new Limited("POST", AUTH + "/google", "login", 10, false),
				new Limited("POST", "/api/v1/account/password", "login", 10, true),
				new Limited("DELETE", "/api/v1/account", "login", 10, true),
				new Limited("POST", AUTH + "/password/forgot", "password-reset", 3, false),
				new Limited("POST", AUTH + "/verify-email/resend", "password-reset", 3, false),
				new Limited("POST", AUTH + "/verify-email", "token", 10, false), new Limited("POST", AUTH + "/refresh", "token", 10, false),
				new Limited("POST", AUTH + "/password/reset", "token", 10, false),
				new Limited("POST", AUTH + "/invitation/accept", "token", 10, false),
				new Limited("POST", AUTH + "/staff/code/verify", "token", 10, false),
				new Limited("POST", AUTH + "/staff/code", "login-code", 5, false),
				new Limited("GET", "/api/v1/quotes/COT-2026-999999/receipt", "receipt", 30, false),
				new Limited("GET", "/api/v1/complaints/REC-2026-999999/receipt", "receipt", 30, false),
				new Limited("POST", "/api/v1/privacy/consents", "consent", 30, false));
	}

	private ResultActions hit(Limited limited, String ip, String token) throws Exception {
		MockHttpServletRequestBuilder builder = request(HttpMethod.valueOf(limited.method()), limited.path()).with((r) -> {
			r.setRemoteAddr(ip);
			return r;
		});
		if (!"GET".equals(limited.method())) {
			builder.contentType(MediaType.APPLICATION_JSON).content("{}");
		}
		if (token != null) {
			authed(builder, token);
		}
		return this.mvc.perform(builder);
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("everyLimitedOperation")
	void answers429RightAfterTheCapacity(Limited limited) throws Exception {
		String ip = "198.51.100." + NEXT_IP.getAndIncrement();
		String token = limited.customerSession() ? customerToken("limite" + NEXT_IP.get() + "@empresa.pe", "frase-de-paso-muy-larga") : null;
		for (int i = 0; i < limited.capacity(); i++) {
			int status = hit(limited, ip, token).andReturn().getResponse().getStatus();
			assertThat(status).as("petición %d de %d", i + 1, limited.capacity()).isNotEqualTo(429);
		}
		String retryAfter = hit(limited, ip, token).andExpect(status().isTooManyRequests())
			.andExpect(header().exists(HttpHeaders.RETRY_AFTER))
			.andExpect(jsonPath("$.code").value("RATE_LIMITED"))
			.andReturn()
			.getResponse()
			.getHeader(HttpHeaders.RETRY_AFTER);
		long expected = SECONDS_PER_TOKEN.get(limited.policy());
		assertThat(Long.parseLong(retryAfter)).as("Retry-After de %s", limited.policy()).isBetween(Math.max(1, expected - 5), expected);
	}

	@Test
	void everyAnnotatedOperationIsCoveredAndNoOtherIsLimited() {
		Set<String> annotated = new TreeSet<>();
		this.mappings.getHandlerMethods().forEach((info, handler) -> {
			if (handler.hasMethodAnnotation(RateLimited.class)) {
				info.getPathPatternsCondition().getPatternValues().forEach((path) -> info.getMethodsCondition().getMethods().forEach((method) -> annotated
					.add(method + " " + path + " " + handler.getMethodAnnotation(RateLimited.class).value())));
			}
		});
		Set<String> tested = new TreeSet<>();
		everyLimitedOperation().forEach((l) -> tested.add(l.method() + " "
				+ l.path().replace("COT-2026-999999", "{folio}").replace("REC-2026-999999", "{folio}") + " " + l.policy()));
		assertThat(tested).isEqualTo(annotated);
	}

	@Test
	void countersArePerIpAndPerPolicy() throws Exception {
		Map<String, Object> body = Map.of("email", "limite.ip@empresa.pe", "password", "clave-incorrecta-x");
		String ip = "198.51.100." + NEXT_IP.getAndIncrement();
		String otherIp = "198.51.100." + NEXT_IP.getAndIncrement();
		for (int i = 0; i < 10; i++) {
			this.mvc.perform(request(HttpMethod.POST, AUTH + "/login").with((r) -> {
				r.setRemoteAddr(ip);
				return r;
			}).contentType(MediaType.APPLICATION_JSON).content(this.json.writeValueAsString(body))).andExpect(status().isUnauthorized());
		}
		this.mvc.perform(request(HttpMethod.POST, AUTH + "/login").with((r) -> {
			r.setRemoteAddr(otherIp);
			return r;
		}).contentType(MediaType.APPLICATION_JSON).content(this.json.writeValueAsString(body))).andExpect(status().isUnauthorized());
		this.mvc.perform(request(HttpMethod.POST, "/api/v1/privacy/consents").with((r) -> {
			r.setRemoteAddr(ip);
			return r;
		}).contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isBadRequest());
	}

}
