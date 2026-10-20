package com.fundigsac.identity;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import com.fundigsac.support.IntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Acciones del personal sobre cuentas de clientes: corregir, desactivar, reenviar correos y anonimizar. */
class StaffAccountAdministrationIT extends IntegrationTest {

	private String idOf(String email) {
		return this.jdbc.sql("SELECT id FROM user_accounts WHERE email = :e").param("e", email).query(String.class).single();
	}

	private String etag(String id, String admin) throws Exception {
		MvcResult result = this.mvc.perform(authed(get("/api/v1/staff/accounts/" + id).with(randomIp()), admin))
			.andExpect(status().isOk())
			.andReturn();
		return result.getResponse().getHeader("ETag");
	}

	@Test
	void staffDisablesEnablesAndCorrectsACustomer() throws Exception {
		String customer = customerToken("corregir@empresa.pe", "frase-de-paso-muy-larga");
		String admin = adminToken();
		String id = idOf("corregir@empresa.pe");

		this.mvc.perform(authed(postJson("/api/v1/staff/accounts/" + id + "/disable", Map.of()), admin)
			.header("If-Match", etag(id, admin)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("DISABLED"));
		this.mvc.perform(authed(get("/api/v1/account/me").with(randomIp()), customer)).andExpect(status().isUnauthorized());
		this.mvc.perform(authed(postJson("/api/v1/staff/accounts/" + id + "/enable", Map.of()), admin)
			.header("If-Match", etag(id, admin)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("ACTIVE"));

		Map<String, Object> profile = new HashMap<>();
		profile.put("fullName", "Carla Corregida");
		profile.put("companyName", "Agua Andina SAC");
		profile.put("ruc", "20131312955");
		this.mvc.perform(authed(patch("/api/v1/staff/accounts/" + id).with(randomIp())
			.contentType("application/json")
			.content(this.json.writeValueAsString(profile)), admin).header("If-Match", etag(id, admin)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.fullName").value("Carla Corregida"));
		// Versión vieja: conflicto, no se pisa el cambio de otra persona.
		this.mvc.perform(authed(postJson("/api/v1/staff/accounts/" + id + "/disable", Map.of()), admin).header("If-Match", "\"0\""))
			.andExpect(status().isPreconditionFailed());

		Awaitility.await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> this.mvc
			.perform(authed(get("/api/v1/staff/accounts/activity").with(randomIp()).param("userId", id), admin))
			.andExpect(jsonPath("$.items[*].type").value(hasItem("UPDATE")))
			.andExpect(jsonPath("$.items[*].type").value(hasItem("DISABLE")))
			.andExpect(jsonPath("$.items[*].actorName").value(hasItem("Administrador FUNDIGSAC"))));
	}

	@Test
	void staffResendsVerificationAndPasswordLinksOnlyToCustomers() throws Exception {
		Map<String, Object> register = new HashMap<>();
		register.put("email", "reenvio@empresa.pe");
		register.put("password", "frase-de-paso-muy-larga");
		register.put("fullName", "Pedro Reenvío");
		register.put("privacyNoticeVersion", "2026-10");
		register.put("acceptPrivacyNotice", true);
		this.mvc.perform(postJson("/api/v1/auth/register", register)).andExpect(status().isAccepted());
		String admin = adminToken();
		String id = idOf("reenvio@empresa.pe");

		this.mvc.perform(authed(postJson("/api/v1/staff/accounts/" + id + "/verification-email", Map.of()), admin))
			.andExpect(status().isAccepted());
		this.mvc.perform(authed(postJson("/api/v1/staff/accounts/" + id + "/password-reset", Map.of()), admin))
			.andExpect(status().isAccepted());
		Awaitility.await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
			assertThat(count("SELECT count(*) FROM email_outbox WHERE template = 'VERIFY_EMAIL' AND recipient = 'reenvio@empresa.pe'",
					Map.of())).isEqualTo(2);
			assertThat(count("SELECT count(*) FROM email_outbox WHERE template = 'PASSWORD_RESET' AND recipient = 'reenvio@empresa.pe'",
					Map.of())).isEqualTo(1);
		});

		String adminId = idOf(ADMIN_EMAIL);
		this.mvc.perform(authed(postJson("/api/v1/staff/accounts/" + adminId + "/password-reset", Map.of()), admin))
			.andExpect(status().isUnprocessableContent())
			.andExpect(jsonPath("$.code").value("STAFF_PASSWORDLESS"));
	}

	@Test
	void staffAnonymizesACustomerButNeverThemselves() throws Exception {
		String customer = customerToken("suprimir@empresa.pe", "frase-de-paso-muy-larga");
		String admin = adminToken();
		String id = idOf("suprimir@empresa.pe");

		this.mvc.perform(authed(delete("/api/v1/staff/accounts/" + id).with(randomIp()), admin).header("If-Match", etag(id, admin)))
			.andExpect(status().isNoContent());
		this.mvc.perform(authed(get("/api/v1/account/me").with(randomIp()), customer)).andExpect(status().isUnauthorized());
		this.mvc.perform(authed(get("/api/v1/staff/accounts/" + id).with(randomIp()), admin)).andExpect(status().isNotFound());
		assertThat(count("SELECT count(*) FROM user_accounts WHERE email = 'suprimir@empresa.pe'", Map.of())).isZero();

		String adminId = idOf(ADMIN_EMAIL);
		this.mvc.perform(authed(delete("/api/v1/staff/accounts/" + adminId).with(randomIp()), admin)
			.header("If-Match", etag(adminId, admin)))
			.andExpect(status().isUnprocessableContent())
			.andExpect(jsonPath("$.code").value("CANNOT_DELETE_SELF"));

		String sales = staffToken("ventas.cuentas@fundigsac.test", "SALES");
		String other = customerToken("intocable@empresa.pe", "frase-de-paso-muy-larga");
		assertThat(other).isNotBlank();
		this.mvc.perform(authed(postJson("/api/v1/staff/accounts/" + idOf("intocable@empresa.pe") + "/disable", Map.of()), sales)
			.header("If-Match", "\"1\""))
			.andExpect(status().isForbidden());
	}

}
