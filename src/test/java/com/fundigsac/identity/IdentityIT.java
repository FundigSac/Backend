package com.fundigsac.identity;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import com.fundigsac.support.IntegrationTest;
import tools.jackson.databind.JsonNode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Registro, verificación, sesiones rotativas, bloqueo, recuperación, revocación y administración del personal. */
class IdentityIT extends IntegrationTest {

	private static Map<String, Object> registration(String email) {
		Map<String, Object> body = new HashMap<>();
		body.put("email", email);
		body.put("password", "frase-de-paso-muy-larga");
		body.put("fullName", "Carla Mendoza");
		body.put("privacyNoticeVersion", "2026-10");
		body.put("acceptPrivacyNotice", true);
		return body;
	}

	@Test
	void registrationDoesNotRevealExistingAccounts() throws Exception {
		this.mvc.perform(postJson("/api/v1/auth/register", registration("carla@empresa.pe")))
			.andExpect(status().isAccepted())
			.andExpect(jsonPath("$.message").exists());
		this.mvc.perform(postJson("/api/v1/auth/register", registration("carla@empresa.pe"))).andExpect(status().isAccepted());
		Awaitility.await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> assertThat(
				count("SELECT count(*) FROM email_outbox WHERE template = 'REGISTRATION_ATTEMPT' AND recipient = 'carla@empresa.pe'",
						Map.of())).isEqualTo(1));
		// Sin verificar no se puede iniciar sesión.
		this.mvc.perform(postJson("/api/v1/auth/login", Map.of("email", "carla@empresa.pe", "password", "frase-de-paso-muy-larga")))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.code").value("EMAIL_NOT_VERIFIED"));
	}

	@Test
	void weakPasswordsAreRejected() throws Exception {
		Map<String, Object> weak = registration("debil@empresa.pe");
		weak.put("password", "password1234");
		this.mvc.perform(postJson("/api/v1/auth/register", weak))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[0].code").value("PASSWORD_TOO_COMMON"));
	}

	@Test
	void sessionLifecycleWithRotationAndReuseDetection() throws Exception {
		this.mvc.perform(postJson("/api/v1/auth/register", registration("sesion@empresa.pe"))).andExpect(status().isAccepted());
		String verify = tokenFromOutbox("VERIFY_EMAIL", "sesion@empresa.pe");
		this.mvc.perform(postJson("/api/v1/auth/verify-email", Map.of("token", verify))).andExpect(status().isNoContent());
		this.mvc.perform(postJson("/api/v1/auth/verify-email", Map.of("token", verify)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[0].code").value("INVALID_TOKEN"));

		JsonNode tokens = body(this.mvc.perform(postJson("/api/v1/auth/login",
				Map.of("email", "SESION@empresa.pe", "password", "frase-de-paso-muy-larga")))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.tokenType").value("Bearer"))
			.andExpect(jsonPath("$.expiresIn").value(900))
			.andReturn());
		String access = tokens.get("accessToken").asString();
		String refresh = tokens.get("refreshToken").asString();

		this.mvc.perform(authed(get("/api/v1/account/me").with(randomIp()), access))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.email").value("sesion@empresa.pe"))
			.andExpect(jsonPath("$.roles[0]").value("CUSTOMER"));
		this.mvc.perform(authed(patch("/api/v1/account/me").with(randomIp())
			.contentType("application/json")
			.content("{\"fullName\":\"Carla M.\",\"ruc\":\"20131312955\",\"phone\":\"987654321\"}"), access))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.ruc").value("20131312955"));

		String rotated = body(this.mvc.perform(postJson("/api/v1/auth/refresh", Map.of("refreshToken", refresh)))
			.andExpect(status().isOk())
			.andReturn()).get("refreshToken").asString();
		// Reutilizar el token ya rotado revoca toda la familia: el nuevo tampoco sirve.
		this.mvc.perform(postJson("/api/v1/auth/refresh", Map.of("refreshToken", refresh)))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("INVALID_SESSION"));
		this.mvc.perform(postJson("/api/v1/auth/refresh", Map.of("refreshToken", rotated))).andExpect(status().isUnauthorized());
	}

	@Test
	void accountLocksAfterFiveFailuresAndPasswordResetRevokesTokens() throws Exception {
		String access = customerToken("bloqueo@empresa.pe", "clave-correcta-larga");
		for (int i = 0; i < 5; i++) {
			this.mvc.perform(postJson("/api/v1/auth/login", Map.of("email", "bloqueo@empresa.pe", "password", "clave-incorrecta-x")))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
		}
		// Bloqueada: ni la contraseña correcta entra y el mensaje no lo revela.
		this.mvc.perform(postJson("/api/v1/auth/login", Map.of("email", "bloqueo@empresa.pe", "password", "clave-correcta-larga")))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));

		this.mvc.perform(postJson("/api/v1/auth/password/forgot", Map.of("email", "bloqueo@empresa.pe"))).andExpect(status().isAccepted());
		this.mvc.perform(postJson("/api/v1/auth/password/forgot", Map.of("email", "nadie@empresa.pe"))).andExpect(status().isAccepted());
		String reset = tokenFromOutbox("PASSWORD_RESET", "bloqueo@empresa.pe");
		this.mvc.perform(postJson("/api/v1/auth/password/reset", Map.of("token", reset, "password", "nueva-clave-segura-2026")))
			.andExpect(status().isNoContent());

		// El JWT emitido antes del cambio ya no vale (sello de seguridad) y el bloqueo se levantó.
		this.mvc.perform(authed(get("/api/v1/account/me").with(randomIp()), access)).andExpect(status().isUnauthorized());
		login("bloqueo@empresa.pe", "nueva-clave-segura-2026");
	}

	@Test
	void administratorManagesStaffAndDisablingRevokesAccess() throws Exception {
		String sales = staffToken("ventas.prueba@fundigsac.test", "SALES");
		this.mvc.perform(authed(get("/api/v1/staff/profile").with(randomIp()), sales))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.permissions").value(org.hamcrest.Matchers.hasItem("quote:manage")));
		// Ventas no administra usuarios.
		this.mvc.perform(authed(get("/api/v1/staff/users").with(randomIp()), sales)).andExpect(status().isForbidden());

		String admin = adminToken();
		// El listado del personal (con y sin búsqueda) incluye a quien se acaba de invitar.
		this.mvc.perform(authed(get("/api/v1/staff/users").with(randomIp()), admin))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.items[*].email").value(org.hamcrest.Matchers.hasItem("ventas.prueba@fundigsac.test")));
		this.mvc.perform(authed(get("/api/v1/staff/users").param("q", "ventas.prueba").with(randomIp()), admin))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.totalItems").value(1));
		String id = this.jdbc.sql("SELECT id FROM user_accounts WHERE email = 'ventas.prueba@fundigsac.test'").query(String.class).single();
		long version = this.jdbc.sql("SELECT version FROM user_accounts WHERE email = 'ventas.prueba@fundigsac.test'").query(Long.class).single();
		this.mvc.perform(authed(postJson("/api/v1/staff/users/" + id + "/disable", Map.of()), admin)
			.header("If-Match", "\"" + version + "\""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("DISABLED"));
		this.mvc.perform(authed(get("/api/v1/staff/profile").with(randomIp()), sales)).andExpect(status().isUnauthorized());

		// Nadie se desactiva a sí mismo ni deja el sistema sin administradores.
		String adminId = this.jdbc.sql("SELECT id FROM user_accounts WHERE email = :e").param("e", ADMIN_EMAIL).query(String.class).single();
		long adminVersion = this.jdbc.sql("SELECT version FROM user_accounts WHERE email = :e").param("e", ADMIN_EMAIL).query(Long.class).single();
		this.mvc.perform(authed(postJson("/api/v1/staff/users/" + adminId + "/disable", Map.of()), admin)
			.header("If-Match", "\"" + adminVersion + "\""))
			.andExpect(status().isUnprocessableContent())
			.andExpect(jsonPath("$.code").value("CANNOT_DISABLE_SELF"));
	}

	@Test
	void customersCanDeleteTheirAccount() throws Exception {
		String access = customerToken("borrar@empresa.pe", "clave-para-eliminar-2026");
		this.mvc.perform(authed(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/v1/account")
			.with(randomIp())
			.contentType("application/json")
			.content("{\"password\":\"clave-para-eliminar-2026\"}"), access)).andExpect(status().isNoContent());
		this.mvc.perform(authed(get("/api/v1/account/me").with(randomIp()), access)).andExpect(status().isUnauthorized());
		assertThat(count("SELECT count(*) FROM user_accounts WHERE email = 'borrar@empresa.pe'", Map.of())).isZero();
	}

}
