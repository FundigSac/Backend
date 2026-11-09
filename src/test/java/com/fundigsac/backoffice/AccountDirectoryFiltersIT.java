package com.fundigsac.backoffice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.fundigsac.support.FakeGoogleConfiguration;
import com.fundigsac.support.IntegrationTest;
import tools.jackson.databind.JsonNode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Filtros, periodos y órdenes del directorio del panel, cada uno con datos que lo distinguen. */
class AccountDirectoryFiltersIT extends IntegrationTest {

	private static final String BASE = "/api/v1/staff/accounts";

	private void seed() throws Exception {
		if (count("SELECT count(*) FROM user_accounts WHERE email = 'ana.filtros@empresa.pe'", Map.of()) > 0) {
			return;
		}
		customerToken("ana.filtros@empresa.pe", "frase-de-paso-muy-larga");
		customerToken("zoe.filtros@empresa.pe", "frase-de-paso-muy-larga");
		this.jdbc.sql("UPDATE user_accounts SET full_name = 'Ana Filtros' WHERE email = 'ana.filtros@empresa.pe'").update();
		this.jdbc.sql("UPDATE user_accounts SET full_name = 'Zoe Filtros' WHERE email = 'zoe.filtros@empresa.pe'").update();
		this.mvc.perform(postJson("/api/v1/auth/google",
				Map.of("idToken", FakeGoogleConfiguration.idToken("g-filtros", "google.filtros@gmail.com", "Cuenta Filtros"), "privacyNoticeVersion", "2026-10")))
			.andExpect(status().isOk());
	}

	private List<String> names(String sort, boolean descending) throws Exception {
		JsonNode page = body(this.mvc.perform(authed(get(BASE).with(randomIp()).param("q", "filtros").param("kind", "customer").param("method", "email")
			.param("sort", sort).param("desc", Boolean.toString(descending)), adminToken()))
			.andExpect(status().isOk())
			.andReturn());
		List<String> names = new ArrayList<>();
		page.get("items").forEach((item) -> names.add(item.get("fullName").asString()));
		return names;
	}

	@Test
	void sortsByNameInBothDirections() throws Exception {
		seed();
		assertThat(names("name", false)).containsExactly("Ana Filtros", "Zoe Filtros");
		assertThat(names("name", true)).containsExactly("Zoe Filtros", "Ana Filtros");
	}

	@ParameterizedTest
	@ValueSource(strings = { "registeredAt", "lastActiveAt", "quotes", "status" })
	void everyDocumentedSortWorks(String sort) throws Exception {
		seed();
		assertThat(names(sort, true)).containsExactlyInAnyOrder("Ana Filtros", "Zoe Filtros");
	}

	@Test
	void anUnknownSortIsRejected() throws Exception {
		this.mvc.perform(authed(get(BASE).with(randomIp()).param("sort", "precio"), adminToken()))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[0].code").value("INVALID_FILTER"));
	}

	@Test
	void filtersBySignupMethod() throws Exception {
		seed();
		this.mvc.perform(authed(get(BASE).with(randomIp()).param("q", "filtros").param("method", "GOOGLE"), adminToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.totalItems").value(1))
			.andExpect(jsonPath("$.items[0].email").value("google.filtros@gmail.com"));
	}

	@Test
	void filtersByRole() throws Exception {
		this.mvc.perform(authed(get(BASE).with(randomIp()).param("role", "ADMINISTRATOR").param("kind", "STAFF").param("size", "100"), adminToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.items[*].email").value(org.hamcrest.Matchers.hasItem(ADMIN_EMAIL)));
		this.mvc.perform(authed(get(BASE).with(randomIp()).param("role", "inventado"), adminToken())).andExpect(status().isBadRequest());
	}

	@ParameterizedTest
	@ValueSource(strings = { "7d", "30d", "90d", "all" })
	void everyDocumentedRangeWorks(String range) throws Exception {
		seed();
		this.mvc.perform(authed(get(BASE).with(randomIp()).param("q", "filtros").param("range", range), adminToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.totalItems").value(3));
		this.mvc.perform(authed(get(BASE + "/stats").with(randomIp()).param("range", range), adminToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.newInRange").isNumber());
	}

	@Test
	void anOldAccountFallsOutOfAShortRange() throws Exception {
		seed();
		this.jdbc.sql("UPDATE user_accounts SET created_at = now() - interval '20 days' WHERE email = 'zoe.filtros@empresa.pe'").update();
		try {
			this.mvc.perform(authed(get(BASE).with(randomIp()).param("q", "zoe.filtros").param("range", "7d"), adminToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalItems").value(0));
			this.mvc.perform(authed(get(BASE).with(randomIp()).param("q", "zoe.filtros").param("range", "30d"), adminToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalItems").value(1));
		}
		finally {
			this.jdbc.sql("UPDATE user_accounts SET created_at = now() WHERE email = 'zoe.filtros@empresa.pe'").update();
		}
	}

	@Test
	void anUnknownRangeIsRejected() throws Exception {
		this.mvc.perform(authed(get(BASE).with(randomIp()).param("range", "1y"), adminToken()))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[0].code").value("INVALID_FILTER"));
	}

	@Test
	void activityFiltersByTypeAndText() throws Exception {
		seed();
		String id = userId("ana.filtros@empresa.pe");
		awaitEventsProcessed();
		this.mvc.perform(authed(get(BASE + "/activity").with(randomIp()).param("type", "LOGIN").param("userId", id), adminToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.items[*].type").value(org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.is("LOGIN"))));
		this.mvc.perform(authed(get(BASE + "/activity").with(randomIp()).param("q", "ana.filtros"), adminToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.totalItems").value(org.hamcrest.Matchers.greaterThan(0)));
		this.mvc.perform(authed(get(BASE + "/activity").with(randomIp()).param("type", "OTRO"), adminToken())).andExpect(status().isBadRequest());
	}

}
