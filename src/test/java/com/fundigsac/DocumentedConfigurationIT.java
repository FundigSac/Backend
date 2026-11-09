package com.fundigsac;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;

import com.fundigsac.complaint.application.ComplaintProperties;
import com.fundigsac.identity.application.IdentityProperties;
import com.fundigsac.shared.domain.PeruTime;
import com.fundigsac.shared.web.RateLimitProperties;
import com.fundigsac.shared.web.RateLimitProperties.Policy;
import com.fundigsac.shared.web.WebProperties;
import com.fundigsac.support.ApiFixtures;
import com.fundigsac.support.IntegrationTest;
import tools.jackson.databind.JsonNode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Los valores que el README promete, leídos de la configuración real (application.yml) y observados en la conducta.
 * Los valores esperados se escriben a mano a propósito: cambiarlos sin actualizar el README hace fallar la suite.
 */
class DocumentedConfigurationIT extends IntegrationTest {

	private static final int MAX_BODY_BYTES = 65_536;

	@Autowired
	RateLimitProperties rateLimits;

	@Autowired
	IdentityProperties identity;

	@Autowired
	WebProperties web;

	@Autowired
	ComplaintProperties complaints;

	@Autowired
	Environment environment;

	@Test
	void theRateLimitsAreTheDocumentedOnes() {
		assertThat(this.rateLimits.enabled()).isTrue();
		assertThat(this.rateLimits.policies()).containsExactlyInAnyOrderEntriesOf(Map.of("public-form", new Policy(5, Duration.ofMinutes(10)),
				"register", new Policy(5, Duration.ofHours(1)), "login", new Policy(10, Duration.ofMinutes(15)), "password-reset",
				new Policy(3, Duration.ofHours(1)), "token", new Policy(10, Duration.ofMinutes(15)), "login-code",
				new Policy(5, Duration.ofMinutes(15)), "receipt", new Policy(30, Duration.ofMinutes(1)), "consent",
				new Policy(30, Duration.ofMinutes(1))));
	}

	@Test
	void identitySettingsAreTheDocumentedOnes() {
		assertThat(this.identity.jwt().accessTokenTtl()).isEqualTo(Duration.ofMinutes(15));
		assertThat(this.identity.jwt().audience()).isEqualTo("fundigsac-api");
		assertThat(this.identity.sessions().customerRefreshTtl()).isEqualTo(Duration.ofDays(14));
		assertThat(this.identity.sessions().staffRefreshTtl()).isEqualTo(Duration.ofHours(12));
		assertThat(this.identity.login().maxFailedAttempts()).isEqualTo(5);
		assertThat(this.identity.login().lockDuration()).isEqualTo(Duration.ofMinutes(15));
		assertThat(this.identity.tokens().staffLoginCodeTtl()).isEqualTo(Duration.ofMinutes(10));
		assertThat(this.identity.tokens().staffLoginCodeResend()).isEqualTo(Duration.ofSeconds(60));
		assertThat(this.identity.tokens().staffLoginCodeAttempts()).isEqualTo(5);
	}

	@Test
	void platformSettingsAreTheDocumentedOnes() {
		assertThat(this.web.maxBodyBytes()).isEqualTo(MAX_BODY_BYTES);
		assertThat(this.complaints.responseBusinessDays()).isEqualTo(15);
		assertThat(this.environment.getProperty("spring.modulith.events.republish-outstanding-events-on-restart", Boolean.class)).isTrue();
		assertThat(this.environment.getProperty("spring.modulith.events.completion-mode")).isEqualTo("delete");
	}

	@Test
	void fiveFailedPasswordsLockTheAccountForFifteenMinutes() throws Exception {
		String email = "bloqueo.documentado@empresa.pe";
		customerToken(email, "frase-de-paso-muy-larga");
		for (int attempt = 0; attempt < 5; attempt++) {
			this.mvc.perform(postJson("/api/v1/auth/login", Map.of("email", email, "password", "clave-incorrecta-x"))).andExpect(status().isUnauthorized());
		}
		Double remaining = this.jdbc.sql("SELECT EXTRACT(EPOCH FROM (locked_until - now()))::float8 FROM user_accounts WHERE email = :e")
			.param("e", email)
			.query(Double.class)
			.single();
		assertThat(remaining).isCloseTo(Duration.ofMinutes(15).toSeconds(), within(30.0));
	}

	@Test
	void customerSessionsLastFourteenDaysAndStaffSessionsTwelveHours() throws Exception {
		String customer = "sesion.cliente.documentada@empresa.pe";
		customerToken(customer, "frase-de-paso-muy-larga");
		assertThat(sessionSeconds(customer)).isEqualTo(Duration.ofDays(14).toSeconds());
		String staff = "sesion.personal.documentada@fundigsac.test";
		staffToken(staff, "SALES");
		assertThat(sessionSeconds(staff)).isEqualTo(Duration.ofHours(12).toSeconds());
	}

	private long sessionSeconds(String email) {
		return this.jdbc.sql("""
				SELECT EXTRACT(EPOCH FROM (expires_at - issued_at))::bigint FROM refresh_tokens
				 WHERE user_id = (SELECT id FROM user_accounts WHERE email = :e) ORDER BY issued_at DESC LIMIT 1""")
			.param("e", email)
			.query(Long.class)
			.single();
	}

	private byte[] inquiryOfExactly(int bytes) throws Exception {
		Map<String, Object> inquiry = new HashMap<>(ApiFixtures.inquiry("limite.exacto@empresa.pe"));
		inquiry.put("message", "");
		int empty = this.json.writeValueAsBytes(inquiry).length;
		inquiry.put("message", "x".repeat(bytes - empty));
		byte[] content = this.json.writeValueAsBytes(inquiry);
		assertThat(content).hasSize(bytes);
		return content;
	}

	@Test
	void theBodyLimitIsExactlySixtyFourKilobytes() throws Exception {
		int atTheLimit = this.mvc.perform(post("/api/v1/inquiries").with(randomIp()).contentType(MediaType.APPLICATION_JSON).content(inquiryOfExactly(MAX_BODY_BYTES)))
			.andReturn().getResponse().getStatus();
		assertThat(atTheLimit).as("un cuerpo de exactamente 64 KB no supera el límite").isNotEqualTo(413);
		this.mvc.perform(post("/api/v1/inquiries").with(randomIp()).contentType(MediaType.APPLICATION_JSON).content(inquiryOfExactly(MAX_BODY_BYTES + 1)))
			.andExpect(status().isContentTooLarge())
			.andExpect(jsonPath("$.code").value("PAYLOAD_TOO_LARGE"));
	}

	@Test
	void aComplaintIsDueFifteenBusinessDaysAfterFilingInLima() throws Exception {
		JsonNode receipt = body(this.mvc.perform(postJson("/api/v1/complaints", ApiFixtures.claim())).andExpect(status().isCreated()).andReturn());
		LocalDate due = PeruTime.dateOf(Instant.parse(receipt.get("filedAt").asString()));
		int counted = 0;
		while (counted < 15) {
			due = due.plusDays(1);
			if (due.getDayOfWeek() != DayOfWeek.SATURDAY && due.getDayOfWeek() != DayOfWeek.SUNDAY) {
				counted++;
			}
		}
		assertThat(LocalDate.parse(receipt.get("responseDueOn").asString())).isEqualTo(due);
	}

}
