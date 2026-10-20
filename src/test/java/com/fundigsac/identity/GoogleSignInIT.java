package com.fundigsac.identity;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fundigsac.support.FakeGoogleConfiguration;
import com.fundigsac.support.IntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Ingreso de clientes con Google: alta sin contraseña, vinculación de cuentas existentes y rechazos. */
class GoogleSignInIT extends IntegrationTest {

	private static Map<String, Object> googleBody(String token) {
		Map<String, Object> body = new HashMap<>();
		body.put("idToken", token);
		body.put("privacyNoticeVersion", "2026-10");
		return body;
	}

	private Map<String, Object> row(String email) {
		return this.jdbc.sql("""
				SELECT signup_method, google_subject, status, password_hash IS NULL AS no_password,
				       email_verified_at IS NOT NULL AS verified
				  FROM user_accounts WHERE email = :e""")
			.param("e", email)
			.query()
			.singleRow();
	}

	@Test
	void firstGoogleSignInCreatesAnActiveCustomerWithoutPassword() throws Exception {
		String token = FakeGoogleConfiguration.idToken("g-1001", "maria@gmail.com", "María Torres");
		String access = body(this.mvc.perform(postJson("/api/v1/auth/google", googleBody(token)))
			.andExpect(status().isOk())
			.andReturn()).get("accessToken").asString();
		this.mvc.perform(authed(get("/api/v1/account/me").with(randomIp()), access))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.email").value("maria@gmail.com"))
			.andExpect(jsonPath("$.fullName").value("María Torres"))
			.andExpect(jsonPath("$.emailVerified").value(true));
		assertThat(row("maria@gmail.com")).containsEntry("signup_method", "GOOGLE")
			.containsEntry("google_subject", "g-1001")
			.containsEntry("status", "ACTIVE")
			.containsEntry("no_password", true);

		// Segundo ingreso: misma cuenta, sin duplicar.
		this.mvc.perform(postJson("/api/v1/auth/google", googleBody(token))).andExpect(status().isOk());
		assertThat(count("SELECT count(*) FROM user_accounts WHERE email = 'maria@gmail.com'", Map.of())).isEqualTo(1);
	}

	@Test
	void googleVerifiesAPendingEmailAccountAndLinksIt() throws Exception {
		Map<String, Object> register = new HashMap<>();
		register.put("email", "pendiente@gmail.com");
		register.put("password", "frase-de-paso-muy-larga");
		register.put("fullName", "Jorge Pérez");
		register.put("privacyNoticeVersion", "2026-10");
		register.put("acceptPrivacyNotice", true);
		this.mvc.perform(postJson("/api/v1/auth/register", register)).andExpect(status().isAccepted());
		assertThat(row("pendiente@gmail.com")).containsEntry("status", "PENDING_VERIFICATION");

		this.mvc.perform(postJson("/api/v1/auth/google",
				googleBody(FakeGoogleConfiguration.idToken("g-2002", "pendiente@gmail.com", "Jorge Pérez"))))
			.andExpect(status().isOk());
		assertThat(row("pendiente@gmail.com")).containsEntry("status", "ACTIVE")
			.containsEntry("verified", true)
			.containsEntry("google_subject", "g-2002")
			.containsEntry("signup_method", "EMAIL");
		// La contraseña sigue sirviendo.
		login("pendiente@gmail.com", "frase-de-paso-muy-larga");
	}

	@Test
	void googleRejectsInvalidTokensStaffAndMissingPrivacyVersion() throws Exception {
		this.mvc.perform(postJson("/api/v1/auth/google", googleBody("token-falso")))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("INVALID_GOOGLE_TOKEN"));
		this.mvc.perform(postJson("/api/v1/auth/google", googleBody(FakeGoogleConfiguration.idToken("g-3003", ADMIN_EMAIL, "Admin"))))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.code").value("STAFF_USE_PANEL"));
		Map<String, Object> noVersion = googleBody(FakeGoogleConfiguration.idToken("g-4004", "sinaviso@gmail.com", "Ana Ruiz"));
		noVersion.remove("privacyNoticeVersion");
		this.mvc.perform(postJson("/api/v1/auth/google", noVersion))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[0].code").value("REQUIRED"));
		assertThat(count("SELECT count(*) FROM user_accounts WHERE email = 'sinaviso@gmail.com'", Map.of())).isZero();
	}

}
