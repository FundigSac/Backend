package com.fundigsac.identity;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.web.servlet.MvcResult;

import com.fundigsac.identity.api.IdentityEvents.StaffInvited;
import com.fundigsac.support.ApiFixtures;
import com.fundigsac.support.FakeGoogleConfiguration;
import com.fundigsac.support.IntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Enumeración de cuentas, bloqueo por anónimos, sesiones vencidas y acciones sensibles en cuentas sin contraseña. */
@RecordApplicationEvents
class LoginProtectionIT extends IntegrationTest {

	private static final String LOGIN = "/api/v1/auth/login";

	@Autowired
	ApplicationEvents events;

	private void invite(String email) throws Exception {
		this.mvc.perform(authed(postJson("/api/v1/staff/users", ApiFixtures.invitation(email, "SALES")), adminToken()))
			.andExpect(status().isCreated());
	}

	private void failLogin(String email, int times) throws Exception {
		for (int i = 0; i < times; i++) {
			this.mvc.perform(postJson(LOGIN, Map.of("email", email, "password", "cualquier-clave-mala")))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
		}
	}

	private String signInWithCode(String email) throws Exception {
		this.mvc.perform(postJson("/api/v1/auth/staff/code", Map.of("email", email))).andExpect(status().isAccepted());
		String code = loginCodeFromOutbox(email);
		return body(this.mvc.perform(postJson("/api/v1/auth/staff/code/verify", Map.of("email", email, "code", code)))
			.andExpect(status().isOk())
			.andReturn()).get("accessToken").asString();
	}

	private Map<String, Object> lockState(String email) {
		return this.jdbc.sql("SELECT locked_until, failed_login_attempts FROM user_accounts WHERE email = :e").param("e", email).query().singleRow();
	}

	private String googleBody(String subject, String email) throws Exception {
		return this.json.writeValueAsString(Map.of("idToken", FakeGoogleConfiguration.idToken(subject, email, "Cuenta Google"), "privacyNoticeVersion", "2026-10"));
	}

	private String googleSignIn(String subject, String email) throws Exception {
		MvcResult result = this.mvc.perform(post("/api/v1/auth/google").with(randomIp()).contentType("application/json").content(googleBody(subject, email)))
			.andExpect(status().isOk())
			.andReturn();
		return body(result).get("accessToken").asString();
	}

	@Test
	void anUnknownEmailAnswersExactlyLikeAWrongPassword() throws Exception {
		customerToken("existe.login@empresa.pe", "frase-de-paso-muy-larga");
		String unknown = this.mvc.perform(postJson(LOGIN, Map.of("email", "no.existe.login@empresa.pe", "password", "clave-incorrecta-x")))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
			.andReturn().getResponse().getContentAsString();
		String wrong = this.mvc.perform(postJson(LOGIN, Map.of("email", "existe.login@empresa.pe", "password", "clave-incorrecta-x")))
			.andExpect(status().isUnauthorized())
			.andReturn().getResponse().getContentAsString();
		assertThat(this.json.readTree(unknown).get("detail")).isEqualTo(this.json.readTree(wrong).get("detail"));
		this.mvc.perform(postJson(LOGIN, Map.of("email", "no-es-un-correo", "password", "clave-incorrecta-x")))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
	}

	@Test
	void anonymousAttemptsDoNotLockPasswordlessStaffOutOfTheCode() throws Exception {
		String email = "sin.clave.bloqueo@fundigsac.test";
		invite(email);
		failLogin(email, 5);

		Map<String, Object> state = lockState(email);
		assertThat(state.get("locked_until")).isNull();
		assertThat(state.get("failed_login_attempts")).isEqualTo(0);
		assertThat(signInWithCode(email)).isNotBlank();
	}

	@Test
	void aPasswordLockDoesNotBlockTheCodeAndTheCodeLiftsIt() throws Exception {
		String email = "con.clave.bloqueo@fundigsac.test";
		invite(email);
		// Token real emitido al invitar, sin sembrarlo en la base de datos.
		String invitationToken = this.events.stream(StaffInvited.class)
			.filter((event) -> event.email().equals(email))
			.findFirst()
			.orElseThrow()
			.token();
		String password = "clave-del-personal-2026";
		this.mvc.perform(postJson("/api/v1/auth/invitation/accept", Map.of("token", invitationToken, "password", password)))
			.andExpect(status().isNoContent());

		failLogin(email, 5);
		assertThat(lockState(email).get("locked_until")).isNotNull();
		// La contraseña correcta no entra mientras dura el bloqueo y el mensaje no lo revela.
		this.mvc.perform(postJson(LOGIN, Map.of("email", email, "password", password)))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));

		String access = signInWithCode(email);
		this.mvc.perform(authed(get("/api/v1/staff/profile").with(randomIp()), access)).andExpect(status().isOk());
		assertThat(lockState(email).get("locked_until")).isNull();
		assertThat(login(email, password)).isNotBlank();
	}

	@Test
	void anExpiredRefreshTokenIsRejectedAndNotRotated() throws Exception {
		String email = "refresh.vencido@empresa.pe";
		customerToken(email, "frase-de-paso-muy-larga");
		String refresh = body(this.mvc.perform(postJson(LOGIN, Map.of("email", email, "password", "frase-de-paso-muy-larga")))
			.andExpect(status().isOk())
			.andReturn()).get("refreshToken").asString();
		this.jdbc.sql("UPDATE refresh_tokens SET issued_at = now() - interval '30 days', expires_at = now() - interval '1 day' WHERE user_id = CAST(:id AS uuid)")
			.param("id", userId(email))
			.update();
		long before = count("SELECT count(*) FROM refresh_tokens WHERE user_id = CAST(:id AS uuid)", Map.of("id", userId(email)));

		this.mvc.perform(postJson("/api/v1/auth/refresh", Map.of("refreshToken", refresh)))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("INVALID_SESSION"));
		assertThat(count("SELECT count(*) FROM refresh_tokens WHERE user_id = CAST(:id AS uuid)", Map.of("id", userId(email)))).isEqualTo(before);
	}

	@Test
	void aGoogleOnlyAccountDeletesItselfConfirmingWithGoogle() throws Exception {
		String email = "solo.google.borrar@gmail.com";
		String access = googleSignIn("g-borrar", email);

		this.mvc.perform(authed(delete("/api/v1/account").with(randomIp()).contentType("application/json").content("{\"password\":\"no-tiene-clave\"}"), access))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[0].code").value("GOOGLE_REAUTH_REQUIRED"));
		String otherPerson = this.json.writeValueAsString(Map.of("googleIdToken", FakeGoogleConfiguration.idToken("g-otra-persona", email, "Otra")));
		this.mvc.perform(authed(delete("/api/v1/account").with(randomIp()).contentType("application/json").content(otherPerson), access))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[0].code").value("GOOGLE_REAUTH_REQUIRED"));

		String confirmed = this.json.writeValueAsString(Map.of("googleIdToken", FakeGoogleConfiguration.idToken("g-borrar", email, "Cuenta Google")));
		this.mvc.perform(authed(delete("/api/v1/account").with(randomIp()).contentType("application/json").content(confirmed), access))
			.andExpect(status().isNoContent());
		assertThat(count("SELECT count(*) FROM user_accounts WHERE email = :e", Map.of("e", email))).isZero();
		assertThat(count("SELECT count(*) FROM user_accounts WHERE status = 'DELETED' AND full_name = 'Cuenta eliminada' AND google_subject IS NULL AND email LIKE 'eliminada-%'", Map.of()))
			.isPositive();
	}

	@Test
	void aGoogleOnlyAccountDefinesItsFirstPasswordConfirmingWithGoogle() throws Exception {
		String email = "solo.google.clave@gmail.com";
		String access = googleSignIn("g-clave", email);
		String password = "primera-clave-propia-2026";

		this.mvc.perform(authed(postJson("/api/v1/account/password", Map.of("newPassword", password)), access))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[0].code").value("GOOGLE_REAUTH_REQUIRED"));
		this.mvc.perform(authed(postJson("/api/v1/account/password",
				Map.of("newPassword", password, "googleIdToken", FakeGoogleConfiguration.idToken("g-clave", email, "Cuenta Google"))), access))
			.andExpect(status().isNoContent());

		assertThat(login(email, password)).isNotBlank();
		// La contraseña nueva pasa a ser la prueba exigida: el token de Google ya no sustituye a la contraseña.
		String renewed = login(email, password);
		this.mvc.perform(authed(postJson("/api/v1/account/password",
				Map.of("newPassword", "segunda-clave-propia-2026", "googleIdToken", FakeGoogleConfiguration.idToken("g-clave", email, "Cuenta Google"))), renewed))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[0].code").value("REQUIRED"))
			.andExpect(jsonPath("$.errors[0].field").value("currentPassword"));
	}

	@Test
	void aPasswordAccountCannotUseGoogleInsteadOfItsPassword() throws Exception {
		String email = "con.clave.borrar@empresa.pe";
		String access = customerToken(email, "frase-de-paso-muy-larga");
		String withGoogle = this.json.writeValueAsString(Map.of("googleIdToken", FakeGoogleConfiguration.idToken("g-intruso", email, "Intruso")));

		this.mvc.perform(authed(delete("/api/v1/account").with(randomIp()).contentType("application/json").content(withGoogle), access))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[0].field").value("password"))
			.andExpect(jsonPath("$.errors[0].code").value("REQUIRED"));
		this.mvc.perform(authed(delete("/api/v1/account").with(randomIp()).contentType("application/json").content("{\"password\":\"clave-incorrecta-x\"}"), access))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[0].code").value("WRONG_PASSWORD"));
		assertThat(count("SELECT count(*) FROM user_accounts WHERE email = :e AND status = 'ACTIVE'", Map.of("e", email))).isEqualTo(1);
	}

}
