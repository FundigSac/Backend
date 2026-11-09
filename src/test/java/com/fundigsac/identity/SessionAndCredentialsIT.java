package com.fundigsac.identity;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import com.fundigsac.shared.domain.SecureTokens;
import com.fundigsac.support.IntegrationTest;
import tools.jackson.databind.JsonNode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Cierre de sesión, cambio de contraseña, reenvío de verificación y aceptación de invitaciones. */
class SessionAndCredentialsIT extends IntegrationTest {

	private static final String PASSWORD = "frase-de-paso-muy-larga";

	private static final String CHANGE_PASSWORD = "/api/v1/account/password";

	private static final String RESEND = "/api/v1/auth/verify-email/resend";

	private static final String ACCEPT = "/api/v1/auth/invitation/accept";

	private void register(String email) throws Exception {
		Map<String, Object> body = new HashMap<>();
		body.put("email", email);
		body.put("password", PASSWORD);
		body.put("fullName", "Cliente de Sesión");
		body.put("privacyNoticeVersion", "2026-10");
		body.put("acceptPrivacyNotice", true);
		this.mvc.perform(postJson("/api/v1/auth/register", body)).andExpect(status().isAccepted());
	}

	private long verificationEmailsTo(String email) {
		return count("SELECT count(*) FROM email_outbox WHERE template = 'VERIFY_EMAIL' AND recipient = :e", Map.of("e", email));
	}

	@Test
	void logoutEndsTheSessionFamily() throws Exception {
		String email = "cierre.sesion@empresa.pe";
		customerToken(email, PASSWORD);
		JsonNode tokens = body(this.mvc.perform(postJson("/api/v1/auth/login", Map.of("email", email, "password", PASSWORD)))
			.andExpect(status().isOk())
			.andReturn());
		String refresh = tokens.get("refreshToken").asString();

		this.mvc.perform(postJson("/api/v1/auth/logout", Map.of("refreshToken", refresh))).andExpect(status().isNoContent());
		this.mvc.perform(postJson("/api/v1/auth/refresh", Map.of("refreshToken", refresh)))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("INVALID_SESSION"));
		// Cerrar sesión dos veces, o con un token desconocido, no revela nada.
		this.mvc.perform(postJson("/api/v1/auth/logout", Map.of("refreshToken", refresh))).andExpect(status().isNoContent());
		this.mvc.perform(postJson("/api/v1/auth/logout", Map.of("refreshToken", "token-que-nunca-existio"))).andExpect(status().isNoContent());
		this.mvc.perform(postJson("/api/v1/auth/logout", Map.of()))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[0].field").value("refreshToken"));
	}

	@Test
	void changingThePasswordClosesEverySession() throws Exception {
		String email = "cambio.clave@empresa.pe";
		String access = customerToken(email, PASSWORD);
		String refresh = body(this.mvc.perform(postJson("/api/v1/auth/login", Map.of("email", email, "password", PASSWORD)))
			.andExpect(status().isOk())
			.andReturn()).get("refreshToken").asString();

		this.mvc.perform(authed(postJson(CHANGE_PASSWORD, Map.of("currentPassword", PASSWORD, "newPassword", "nueva-frase-de-paso-2026")), access))
			.andExpect(status().isNoContent());

		this.mvc.perform(getAs("/api/v1/account/me", access)).andExpect(status().isUnauthorized());
		this.mvc.perform(postJson("/api/v1/auth/refresh", Map.of("refreshToken", refresh))).andExpect(status().isUnauthorized());
		this.mvc.perform(postJson("/api/v1/auth/login", Map.of("email", email, "password", PASSWORD))).andExpect(status().isUnauthorized());
		assertThat(login(email, "nueva-frase-de-paso-2026")).isNotBlank();
	}

	@Test
	void passwordChangeValidatesTheCurrentAndNewPassword() throws Exception {
		String email = "validar.clave@empresa.pe";
		String access = customerToken(email, PASSWORD);

		this.mvc.perform(authed(postJson(CHANGE_PASSWORD, Map.of("currentPassword", "otra-clave-incorrecta", "newPassword", "nueva-frase-de-paso-2026")), access))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[0].field").value("currentPassword"))
			.andExpect(jsonPath("$.errors[0].code").value("WRONG_PASSWORD"));
		this.mvc.perform(authed(postJson(CHANGE_PASSWORD, Map.of("currentPassword", PASSWORD, "newPassword", "corta")), access))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[0].code").value("INVALID_LENGTH"));
		this.mvc.perform(authed(postJson(CHANGE_PASSWORD, Map.of("currentPassword", PASSWORD, "newPassword", "password1234")), access))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[0].field").value("newPassword"))
			.andExpect(jsonPath("$.errors[0].code").value("PASSWORD_TOO_COMMON"));
		this.mvc.perform(authed(postJson(CHANGE_PASSWORD, Map.of("currentPassword", PASSWORD)), access))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[0].code").value("REQUIRED"));
		// Tras los intentos fallidos la contraseña sigue siendo la misma.
		assertThat(login(email, PASSWORD)).isNotBlank();
	}

	@Test
	void passwordChangeIsOnlyForCustomers() throws Exception {
		String body = "{\"currentPassword\":\"" + PASSWORD + "\",\"newPassword\":\"nueva-frase-de-paso-2026\"}";
		this.mvc.perform(post(CHANGE_PASSWORD).with(randomIp()).contentType("application/json").content(body))
			.andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
		this.mvc.perform(authed(post(CHANGE_PASSWORD).with(randomIp()).contentType("application/json").content(body), adminToken()))
			.andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("FORBIDDEN"));
	}

	@Test
	void resendingVerificationIssuesANewTokenAndInvalidatesTheOldOne() throws Exception {
		String email = "reenvio.verificacion@empresa.pe";
		register(email);
		String first = tokenFromOutbox("VERIFY_EMAIL", email);

		this.mvc.perform(postJson(RESEND, Map.of("email", email)))
			.andExpect(status().isAccepted())
			.andExpect(jsonPath("$.message").exists());
		Awaitility.await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> assertThat(verificationEmailsTo(email)).isEqualTo(2));
		String second = tokenFromOutbox("VERIFY_EMAIL", email);
		assertThat(second).isNotEqualTo(first);

		this.mvc.perform(postJson("/api/v1/auth/verify-email", Map.of("token", first)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[0].code").value("INVALID_TOKEN"));
		this.mvc.perform(postJson("/api/v1/auth/verify-email", Map.of("token", second))).andExpect(status().isNoContent());
		assertThat(login(email, PASSWORD)).isNotBlank();

		// La cuenta ya verificada no recibe otro enlace, pero la respuesta es la misma.
		this.mvc.perform(postJson(RESEND, Map.of("email", email))).andExpect(status().isAccepted());
		awaitEventsProcessed();
		assertThat(verificationEmailsTo(email)).isEqualTo(2);
	}

	@Test
	void resendingVerificationDoesNotRevealAccounts() throws Exception {
		this.mvc.perform(postJson(RESEND, Map.of("email", "desconocido.reenvio@empresa.pe"))).andExpect(status().isAccepted());
		awaitEventsProcessed();
		assertThat(verificationEmailsTo("desconocido.reenvio@empresa.pe")).isZero();
		this.mvc.perform(postJson(RESEND, Map.of("email", "no-es-un-correo")))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[0].code").value("INVALID_EMAIL"));
		this.mvc.perform(postJson(RESEND, Map.of())).andExpect(status().isBadRequest());
	}

	/**
	 * El correo de invitación solo enlaza al acceso con código (no lleva el token), así que el token se siembra con su
	 * hash, igual que lo guarda la aplicación.
	 */
	private String invitationTokenFor(String email) throws Exception {
		this.mvc.perform(authed(postJson("/api/v1/staff/users",
				Map.of("email", email, "fullName", "Invitada de Prueba", "roles", List.of("SALES"))), adminToken()))
			.andExpect(status().isCreated());
		String raw = SecureTokens.random();
		this.jdbc.sql("""
				INSERT INTO one_time_tokens (id, user_id, purpose, token_hash, created_at, expires_at)
				VALUES (gen_random_uuid(), CAST(:user AS uuid), 'STAFF_INVITATION', :hash, now(), now() + interval '1 hour')""")
			.param("user", userId(email))
			.param("hash", SecureTokens.sha256(raw))
			.update();
		return raw;
	}

	@Test
	void invitationIsAcceptedOnceAndActivatesTheAccount() throws Exception {
		String email = "acepta.invitacion@fundigsac.test";
		String raw = invitationTokenFor(email);

		this.mvc.perform(postJson(ACCEPT, Map.of("token", raw, "password", "contrasena-de-la-invitada-2026"))).andExpect(status().isNoContent());
		assertThat(login(email, "contrasena-de-la-invitada-2026")).isNotBlank();
		this.mvc.perform(postJson(ACCEPT, Map.of("token", raw, "password", "contrasena-de-la-invitada-2026")))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[0].code").value("INVALID_TOKEN"));
	}

	@Test
	void invitationValidatesTokenAndPassword() throws Exception {
		String email = "valida.invitacion@fundigsac.test";
		String raw = invitationTokenFor(email);

		this.mvc.perform(postJson(ACCEPT, Map.of("token", "token-falso-de-invitacion", "password", "contrasena-de-la-invitada-2026")))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[0].code").value("INVALID_TOKEN"));
		this.mvc.perform(postJson(ACCEPT, Map.of("token", raw, "password", "corta")))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[0].code").value("INVALID_LENGTH"));
		// Una contraseña rechazada no consume el token (transacción completa).
		this.mvc.perform(postJson(ACCEPT, Map.of("token", raw, "password", "password1234")))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[0].code").value("PASSWORD_TOO_COMMON"));
		this.mvc.perform(postJson(ACCEPT, Map.of("token", raw, "password", "contrasena-de-la-invitada-2026"))).andExpect(status().isNoContent());
	}

}
