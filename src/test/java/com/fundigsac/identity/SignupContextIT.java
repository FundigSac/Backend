package com.fundigsac.identity;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fundigsac.support.FakeGoogleConfiguration;
import com.fundigsac.support.IntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Página de origen del alta y consentimiento del boletín con su fecha, por correo y con Google. */
class SignupContextIT extends IntegrationTest {

	private Map<String, Object> row(String email) {
		return this.jdbc.sql("""
				SELECT signup_source, newsletter_opt_in_at IS NOT NULL AS newsletter FROM user_accounts WHERE email = :e""")
			.param("e", email)
			.query()
			.singleRow();
	}

	@Test
	void emailRegistrationRecordsSourceAndNewsletterConsent() throws Exception {
		Map<String, Object> body = new HashMap<>();
		body.put("email", "boletin@empresa.pe");
		body.put("password", "frase-de-paso-muy-larga");
		body.put("fullName", "Rosa Quispe");
		body.put("privacyNoticeVersion", "2026-10");
		body.put("acceptPrivacyNotice", true);
		body.put("source", "cotizar");
		body.put("newsletter", true);
		this.mvc.perform(postJson("/api/v1/auth/register", body)).andExpect(status().isAccepted());
		assertThat(row("boletin@empresa.pe")).containsEntry("signup_source", "cotizar").containsEntry("newsletter", true);

		// Solo se aceptan páginas conocidas, y la validación no depende de si el correo ya existía.
		body.put("source", "<script>");
		this.mvc.perform(postJson("/api/v1/auth/register", body))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[0].code").value("INVALID_SOURCE"));
	}

	@Test
	void googleSignupRecordsSourceAndNoNewsletterByDefault() throws Exception {
		Map<String, Object> body = new HashMap<>();
		body.put("idToken", FakeGoogleConfiguration.idToken("g-5005", "carrito@gmail.com", "Luis Rojas"));
		body.put("privacyNoticeVersion", "2026-10");
		body.put("source", "carrito");
		this.mvc.perform(postJson("/api/v1/auth/google", body)).andExpect(status().isOk());
		assertThat(row("carrito@gmail.com")).containsEntry("signup_source", "carrito").containsEntry("newsletter", false);
	}

}
