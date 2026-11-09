package com.fundigsac;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpMethod;

import com.fundigsac.support.ApiFixtures;
import com.fundigsac.support.IntegrationTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Entradas inválidas en las operaciones que solo se probaban con datos correctos. */
class InvalidInputCoverageIT extends IntegrationTest {

	private static final String STAFF = "/api/v1/staff";

	private static final String UNKNOWN = "00000000-0000-7000-8000-00000000abcd";

	private record InvalidBody(String method, String path, Object body, String field) {

		@Override
		public String toString() {
			return this.method + " " + this.path + " (" + this.field + ")";
		}

	}

	static Stream<InvalidBody> staffOperationsWithInvalidBodies() {
		Map<String, Object> product = new HashMap<>(ApiFixtures.product("cuerpo-invalido"));
		product.put("name", "x");
		return Stream.of(new InvalidBody("POST", STAFF + "/users", Map.of("email", "no-correo", "fullName", "Nombre Válido", "roles", List.of("SALES")), "email"),
				new InvalidBody("PUT", STAFF + "/catalog/products/" + UNKNOWN, product, "name"),
				new InvalidBody("POST", STAFF + "/catalog/products/" + UNKNOWN + "/reject", Map.of("note", "x"), "note"),
				new InvalidBody("POST", STAFF + "/quotes/" + UNKNOWN + "/status", Map.of("note", "Sin estado"), "status"),
				new InvalidBody("POST", STAFF + "/inquiries/" + UNKNOWN + "/status", Map.of("note", "Sin estado"), "status"),
				new InvalidBody("POST", STAFF + "/complaints/" + UNKNOWN + "/answer", Map.of("response", "corta"), "response"),
				new InvalidBody("POST", STAFF + "/privacy/requests/" + UNKNOWN + "/conclude", Map.of("accepted", true, "resolutionNote", "x"), "resolutionNote"),
				new InvalidBody("POST", STAFF + "/legal/b2b-terms/versions", Map.of("version", "x"), "title"),
				new InvalidBody("PATCH", STAFF + "/accounts/" + UNKNOWN, Map.of("fullName", "x"), "fullName"));
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("staffOperationsWithInvalidBodies")
	void anInvalidStaffBodyIsRejectedBeforeTouchingTheRecord(InvalidBody invalid) throws Exception {
		this.mvc.perform(authed(request(HttpMethod.valueOf(invalid.method()), invalid.path()).with(randomIp())
			.contentType("application/json")
			.content(this.json.writeValueAsString(invalid.body()))
			.header("If-Match", "\"0\""), adminToken()))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
			.andExpect(jsonPath("$.errors[?(@.field == '%s')]".formatted(invalid.field())).exists());
	}

	@Test
	void assigningAnUnknownQuoteIsNotFound() throws Exception {
		this.mvc.perform(authed(postJson(STAFF + "/quotes/" + UNKNOWN + "/assign", Map.of("assigneeId", userId(ADMIN_EMAIL))), adminToken())
			.header("If-Match", "\"0\""))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("QUOTE_NOT_FOUND"));
	}

	@Test
	void aDuplicateProductSlugIsAConflictAndAnInvalidOneIsRejected() throws Exception {
		String admin = adminToken();
		String slug = "duplicado-" + ApiFixtures.unique();
		this.mvc.perform(authed(postJson("/api/v1/staff/catalog/products", ApiFixtures.product(slug)), admin)).andExpect(status().isCreated());
		this.mvc.perform(authed(postJson("/api/v1/staff/catalog/products", ApiFixtures.product(slug)), admin))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("SLUG_TAKEN"));
		this.mvc.perform(authed(postJson("/api/v1/staff/catalog/products", ApiFixtures.product("Slug Inválido")), admin))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[0].field").value("slug"));
	}

	@Test
	void theOwnProfileRejectsInvalidData() throws Exception {
		String customer = customerToken("perfil.invalido@empresa.pe", "frase-de-paso-muy-larga");
		this.mvc.perform(authed(patch("/api/v1/account/me").with(randomIp()).contentType("application/json")
			.content(this.json.writeValueAsString(Map.of("fullName", "Nombre Válido", "ruc", "20131312956"))), customer))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[0].code").value("INVALID_RUC"));
		this.mvc.perform(authed(patch("/api/v1/account/me").with(randomIp()).contentType("application/json").content("{\"fullName\":\"X\"}"), customer))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[0].field").value("fullName"));
		this.mvc.perform(authed(get("/api/v1/account/quotes").param("size", "1000").with(randomIp()), customer)).andExpect(status().isBadRequest());
	}

	@Test
	void staffInboxesRejectUnknownFilters() throws Exception {
		String admin = adminToken();
		this.mvc.perform(authed(get("/api/v1/staff/accounts/stats").param("range", "1y").with(randomIp()), admin))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[0].code").value("INVALID_FILTER"));
		this.mvc.perform(authed(get("/api/v1/staff/complaints").param("status", "NOPE").with(randomIp()), admin)).andExpect(status().isBadRequest());
		this.mvc.perform(authed(get("/api/v1/staff/notifications").param("size", "1000").with(randomIp()), admin)).andExpect(status().isBadRequest());
	}

	@Test
	void publicListingsRejectInvalidFilters() throws Exception {
		this.mvc.perform(get("/api/v1/catalog/filters").param("category", "Categoría Inválida").with(randomIp()))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[0].code").value("INVALID_SLUG"));
		this.mvc.perform(get("/api/v1/resources").param("type", "NOPE").with(randomIp()))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
	}

}
