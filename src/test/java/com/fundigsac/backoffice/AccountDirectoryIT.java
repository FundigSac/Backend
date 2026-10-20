package com.fundigsac.backoffice;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fundigsac.support.IntegrationTest;
import tools.jackson.databind.JsonNode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Directorio de cuentas del panel: filtros, búsqueda sin tildes, conteos, indicadores, actividad y permisos. */
class AccountDirectoryIT extends IntegrationTest {

	private void register(String email, String name, String source, boolean newsletter) throws Exception {
		Map<String, Object> body = new HashMap<>();
		body.put("email", email);
		body.put("password", "frase-de-paso-muy-larga");
		body.put("fullName", name);
		body.put("companyName", "Agua Andina SAC");
		body.put("privacyNoticeVersion", "2026-10");
		body.put("acceptPrivacyNotice", true);
		body.put("source", source);
		body.put("newsletter", newsletter);
		this.mvc.perform(postJson("/api/v1/auth/register", body)).andExpect(status().isAccepted());
	}

	@Test
	void listsAccountsWithFiltersAccentInsensitiveSearchAndStatusCounts() throws Exception {
		register("pena.directorio@empresa.pe", "Martín Peña Directorio", "carrito", true);
		customerToken("activa.directorio@empresa.pe", "frase-de-paso-muy-larga");
		String admin = adminToken();

		JsonNode page = body(this.mvc.perform(authed(get("/api/v1/staff/accounts").with(randomIp())
			.param("q", "martin pena")
			.param("kind", "customer"), admin))
			.andExpect(status().isOk())
			.andReturn());
		assertThat(page.get("totalItems").asLong()).isEqualTo(1);
		JsonNode row = page.get("items").get(0);
		assertThat(row.get("email").asString()).isEqualTo("pena.directorio@empresa.pe");
		assertThat(row.get("status").asString()).isEqualTo("PENDING_VERIFICATION");
		assertThat(row.get("signupSource").asString()).isEqualTo("carrito");
		assertThat(row.get("newsletter").asBoolean()).isTrue();
		assertThat(row.get("signupMethod").asString()).isEqualTo("EMAIL");

		// Los conteos ignoran el filtro de estado (para las pestañas) pero respetan los demás.
		JsonNode filtered = body(this.mvc.perform(authed(get("/api/v1/staff/accounts").with(randomIp())
			.param("q", "directorio@empresa.pe")
			.param("status", "ACTIVE"), admin)).andExpect(status().isOk()).andReturn());
		assertThat(filtered.get("totalItems").asLong()).isEqualTo(1);
		assertThat(filtered.get("statusCounts").get("all").asLong()).isEqualTo(2);
		assertThat(filtered.get("statusCounts").get("PENDING_VERIFICATION").asLong()).isEqualTo(1);

		String id = row.get("id").asString();
		this.mvc.perform(authed(get("/api/v1/staff/accounts/" + id).with(randomIp()), admin))
			.andExpect(status().isOk())
			.andExpect(header().exists("ETag"))
			.andExpect(jsonPath("$.fullName").value("Martín Peña Directorio"));

		this.mvc.perform(authed(get("/api/v1/staff/accounts").with(randomIp()).param("status", "BORRADA"), admin))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[0].code").value("INVALID_FILTER"));
	}

	@Test
	void statsAndActivityReflectRegistrationsAndSignIns() throws Exception {
		customerToken("indicadores@empresa.pe", "frase-de-paso-muy-larga");
		String admin = adminToken();

		JsonNode stats = body(this.mvc.perform(authed(get("/api/v1/staff/accounts/stats").with(randomIp()).param("range", "30d"), admin))
			.andExpect(status().isOk())
			.andReturn());
		assertThat(stats.get("series").size()).isEqualTo(30);
		assertThat(stats.get("series").get(29).get("count").asLong()).isPositive();
		assertThat(stats.get("newInRange").asLong()).isPositive();
		assertThat(stats.get("funnel").get("registered").asLong()).isGreaterThanOrEqualTo(stats.get("funnel").get("verified").asLong());

		String id = this.jdbc.sql("SELECT id FROM user_accounts WHERE email = 'indicadores@empresa.pe'").query(String.class).single();
		org.awaitility.Awaitility.await().atMost(java.time.Duration.ofSeconds(15)).untilAsserted(() -> this.mvc
			.perform(authed(get("/api/v1/staff/accounts/activity").with(randomIp()).param("userId", id), admin))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.items[*].type").value(hasItem("SIGNUP")))
			.andExpect(jsonPath("$.items[*].type").value(hasItem("VERIFY")))
			.andExpect(jsonPath("$.items[*].type").value(hasItem("LOGIN"))));
	}

	@Test
	void onlyUserManagersCanReadTheDirectory() throws Exception {
		String sales = staffToken("ventas.directorio@fundigsac.test", "SALES");
		this.mvc.perform(authed(get("/api/v1/staff/accounts").with(randomIp()), sales)).andExpect(status().isForbidden());
		this.mvc.perform(authed(get("/api/v1/staff/accounts/stats").with(randomIp()), sales)).andExpect(status().isForbidden());
		String customer = customerToken("curioso@empresa.pe", "frase-de-paso-muy-larga");
		this.mvc.perform(authed(get("/api/v1/staff/accounts").with(randomIp()), customer)).andExpect(status().isForbidden());
	}

}
