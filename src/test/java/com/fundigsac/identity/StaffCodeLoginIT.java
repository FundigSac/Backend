package com.fundigsac.identity;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import com.fundigsac.support.IntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Acceso del personal sin contraseña: código por correo, 5 intentos, espera de reenvío y respuestas que no revelan nada. */
class StaffCodeLoginIT extends IntegrationTest {

	private void invite(String email) throws Exception {
		this.mvc.perform(authed(postJson("/api/v1/staff/users",
				Map.of("email", email, "fullName", "Lucía Paredes", "roles", List.of("LEGAL_SUPPORT"))), adminToken()))
			.andExpect(status().isCreated());
	}

	private long codesSentTo(String email) {
		return count("SELECT count(*) FROM email_outbox WHERE template = 'STAFF_LOGIN_CODE' AND recipient = :e", Map.of("e", email));
	}

	@Test
	void unknownEmailsAndCustomersGetTheSameAnswerAndNoCode() throws Exception {
		this.mvc.perform(postJson("/api/v1/auth/staff/code", Map.of("email", "nadie@fundigsac.test")))
			.andExpect(status().isAccepted())
			.andExpect(jsonPath("$.expiresInSeconds").value(600))
			.andExpect(jsonPath("$.resendAfterSeconds").value(60));
		customerToken("cliente.codigo@empresa.pe", "clave-de-cliente-2026");
		this.mvc.perform(postJson("/api/v1/auth/staff/code", Map.of("email", "cliente.codigo@empresa.pe")))
			.andExpect(status().isAccepted());
		this.mvc.perform(postJson("/api/v1/auth/staff/code/verify", Map.of("email", "nadie@fundigsac.test", "code", "123456")))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("INVALID_CODE"));
		assertThat(codesSentTo("nadie@fundigsac.test")).isZero();
		assertThat(codesSentTo("cliente.codigo@empresa.pe")).isZero();
	}

	@Test
	void invitedStaffActivatesWithTheFirstCodeAndTheCodeWorksOnce() throws Exception {
		String email = "legal.codigo@fundigsac.test";
		invite(email);
		this.mvc.perform(postJson("/api/v1/auth/staff/code", Map.of("email", email))).andExpect(status().isAccepted());
		String code = loginCodeFromOutbox(email);

		String access = body(this.mvc.perform(postJson("/api/v1/auth/staff/code/verify", Map.of("email", email.toUpperCase(), "code", code)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.tokenType").value("Bearer"))
			.andReturn()).get("accessToken").asString();
		this.mvc.perform(authed(get("/api/v1/staff/profile").with(randomIp()), access))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("ACTIVE"))
			.andExpect(jsonPath("$.emailVerified").value(true));
		// Un solo uso.
		this.mvc.perform(postJson("/api/v1/auth/staff/code/verify", Map.of("email", email, "code", code)))
			.andExpect(status().isUnauthorized());
		Awaitility.await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> assertThat(count(
				"SELECT count(*) FROM audit_events WHERE action = 'SIGNED_IN' AND details ->> 'method' = 'STAFF_CODE'", Map.of()))
			.isPositive());
	}

	@Test
	void fiveWrongAttemptsBurnTheCodeAndResendWaitsSixtySeconds() throws Exception {
		String email = "intentos.codigo@fundigsac.test";
		invite(email);
		this.mvc.perform(postJson("/api/v1/auth/staff/code", Map.of("email", email))).andExpect(status().isAccepted());
		String code = loginCodeFromOutbox(email);
		String wrong = code.equals("000000") ? "111111" : "000000";

		// Pedir otro antes de 60 s no emite un código nuevo.
		this.mvc.perform(postJson("/api/v1/auth/staff/code", Map.of("email", email))).andExpect(status().isAccepted());
		assertThat(codesSentTo(email)).isEqualTo(1);

		for (int i = 0; i < 5; i++) {
			this.mvc.perform(postJson("/api/v1/auth/staff/code/verify", Map.of("email", email, "code", wrong)))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("INVALID_CODE"));
		}
		// Agotado: ni el código correcto sirve.
		this.mvc.perform(postJson("/api/v1/auth/staff/code/verify", Map.of("email", email, "code", code)))
			.andExpect(status().isUnauthorized());

		// Un código agotado no cuenta para la espera: se puede pedir otro y ese sí entra.
		this.mvc.perform(postJson("/api/v1/auth/staff/code", Map.of("email", email))).andExpect(status().isAccepted());
		Awaitility.await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> assertThat(codesSentTo(email)).isEqualTo(2));
		this.mvc.perform(postJson("/api/v1/auth/staff/code/verify", Map.of("email", email, "code", loginCodeFromOutbox(email))))
			.andExpect(status().isOk());
	}

	@Test
	void expiredCodesAreRejected() throws Exception {
		String email = "vencido.codigo@fundigsac.test";
		invite(email);
		this.mvc.perform(postJson("/api/v1/auth/staff/code", Map.of("email", email))).andExpect(status().isAccepted());
		String code = loginCodeFromOutbox(email);
		this.jdbc.sql("""
				UPDATE staff_login_codes SET created_at = now() - interval '20 minutes', expires_at = now() - interval '10 minutes'
				 WHERE user_id = (SELECT id FROM user_accounts WHERE email = :e)""")
			.param("e", email)
			.update();
		this.mvc.perform(postJson("/api/v1/auth/staff/code/verify", Map.of("email", email, "code", code)))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("INVALID_CODE"));
	}

}
