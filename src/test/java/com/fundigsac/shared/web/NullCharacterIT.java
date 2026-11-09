package com.fundigsac.shared.web;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.fundigsac.support.IntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** U+0000 no es un valor válido en PostgreSQL: se rechaza con 400 en filtros y notas, nunca como conflicto 409. */
class NullCharacterIT extends IntegrationTest {

	private static final String NUL = "con" + (char) 0 + "nulo";

	private record Filter(String path, String parameter) {

		@Override
		public String toString() {
			return this.path + "?" + this.parameter;
		}

	}

	static Stream<Filter> staffFilters() {
		return Stream.of(new Filter("/api/v1/staff/accounts", "q"), new Filter("/api/v1/staff/audit", "action"),
				new Filter("/api/v1/staff/complaints", "q"), new Filter("/api/v1/staff/quotes", "q"),
				new Filter("/api/v1/staff/inquiries", "q"), new Filter("/api/v1/staff/users", "q"),
				new Filter("/api/v1/staff/privacy/requests", "q"), new Filter("/api/v1/staff/catalog/products", "q"));
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("staffFilters")
	void aStaffFilterWithNulIsABadRequest(Filter filter) throws Exception {
		this.mvc.perform(authed(get(filter.path()).param(filter.parameter(), NUL).with(randomIp()), adminToken()))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
			.andExpect(jsonPath("$.errors[0].field").value(filter.parameter()))
			.andExpect(jsonPath("$.errors[0].code").value("INVALID_CHARACTER"));
	}

	@Test
	void aPublicFilterWithNulIsABadRequest() throws Exception {
		this.mvc.perform(get("/api/v1/catalog/products").param("q", NUL).with(randomIp()))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[0].code").value("INVALID_CHARACTER"));
	}

	@Test
	void anAnonymousVisitorIsStillAskedToSignIn() throws Exception {
		this.mvc.perform(get("/api/v1/staff/accounts").param("q", NUL).with(randomIp())).andExpect(status().isUnauthorized());
	}

	@Test
	void aNoteWithNulIsRejectedAndNotStored() throws Exception {
		String admin = adminToken();
		customerToken("nota.nul@empresa.pe", "frase-de-paso-muy-larga");
		String account = userId("nota.nul@empresa.pe");
		this.mvc.perform(authed(putJson("/api/v1/staff/accounts/" + account + "/notes", Map.of("notes", NUL)), admin))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[0].field").value("notes"))
			.andExpect(jsonPath("$.errors[0].code").value("INVALID_CHARACTER"));
		assertThat(count("SELECT count(*) FROM account_notes WHERE user_id = CAST(:id AS uuid)", Map.of("id", account))).isZero();
		this.mvc.perform(authed(putJson("/api/v1/staff/accounts/" + UUID.randomUUID() + "/notes", Map.of("notes", "Nota correcta")), admin))
			.andExpect(status().isNotFound());
	}

}
