package com.fundigsac.notification.application;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import com.fundigsac.support.ApiFixtures;
import com.fundigsac.support.IntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Los logs no guardan cuerpos ni datos personales. Cada flujo usa valores únicos y al final se busca cada uno en lo que la
 * aplicación escribió, incluidos el envío de correos, los errores de validación y los accesos fallidos.
 */
@ExtendWith(OutputCaptureExtension.class)
class LogPrivacyIT extends IntegrationTest {

	private static final String EMAIL = "persona.logs@correo-unico.pe";

	private static final String NAME = "Nombre Exclusivo Logs";

	private static final String PHONE = "987123654";

	private static final String DOCUMENT = "40781236";

	private static final String ADDRESS = "Calle Exclusiva 777 Logs";

	private static final String COMPANY = "Empresa Exclusiva Logs";

	private static final String MESSAGE = "Mensaje exclusivo para detectar cuerpos en los logs";

	private static final String PASSWORD = "clave-exclusiva-logs-2026";

	private static final String CONTROL = "control-de-captura-de-logs";

	@Autowired
	OutboxDispatcher dispatcher;

	@Test
	void personalDataNeverReachesTheLogs(CapturedOutput output) throws Exception {
		LoggerFactory.getLogger(LogPrivacyIT.class).info(CONTROL);

		Map<String, Object> inquiry = new HashMap<>(ApiFixtures.inquiry(EMAIL));
		inquiry.put("fullName", NAME);
		inquiry.put("companyName", COMPANY);
		inquiry.put("phone", PHONE);
		inquiry.put("message", MESSAGE);
		this.mvc.perform(postJson("/api/v1/inquiries", inquiry)).andExpect(status().isCreated());

		Map<String, Object> claim = new HashMap<>(ApiFixtures.claim());
		claim.put("consumerName", NAME);
		claim.put("documentNumber", DOCUMENT);
		claim.put("email", EMAIL);
		claim.put("phone", PHONE);
		claim.put("address", ADDRESS);
		claim.put("facts", MESSAGE);
		this.mvc.perform(postJson("/api/v1/complaints", claim)).andExpect(status().isCreated());

		Map<String, Object> rights = new HashMap<>(ApiFixtures.rights(EMAIL));
		rights.put("fullName", NAME);
		this.mvc.perform(postJson("/api/v1/privacy/requests", rights)).andExpect(status().isCreated());

		Map<String, Object> invalid = new HashMap<>(inquiry);
		invalid.remove("acceptPrivacyNotice");
		this.mvc.perform(postJson("/api/v1/inquiries", invalid)).andExpect(status().isBadRequest());

		this.mvc.perform(postJson("/api/v1/auth/login", Map.of("email", EMAIL, "password", PASSWORD))).andExpect(status().isUnauthorized());
		this.mvc.perform(postJson("/api/v1/auth/password/forgot", Map.of("email", EMAIL))).andExpect(status().isAccepted());

		awaitEventsProcessed();
		this.dispatcher.dispatch();

		assertThat(output.getAll()).as("la captura de logs funciona").contains(CONTROL);
		assertThat(output.getAll()).doesNotContain(EMAIL, NAME, PHONE, DOCUMENT, ADDRESS, COMPANY, MESSAGE, PASSWORD);
	}

}
