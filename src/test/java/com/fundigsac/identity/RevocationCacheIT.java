package com.fundigsac.identity;

import java.time.Duration;
import java.util.Map;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import com.fundigsac.support.IntegrationTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * El perfil de pruebas apaga la caché de revocación para que las demás pruebas no esperen; aquí se usa el valor real
 * de 10 segundos. Con la caché apagada no se habría detectado que un JWT recién emitido se rechazaba tras cambiar la
 * contraseña.
 */
@TestPropertySource(properties = "fundigsac.identity.jwt.revocation-cache-ttl=PT10S")
class RevocationCacheIT extends IntegrationTest {

	@Test
	void aFreshTokenWorksAtOnceAndTheOldOneExpiresWithinTheCacheWindow() throws Exception {
		String email = "cache.revocacion@empresa.pe";
		String oldToken = customerToken(email, "clave-original-larga-2026");
		this.mvc.perform(authed(get("/api/v1/account/me").with(randomIp()), oldToken)).andExpect(status().isOk());

		this.mvc.perform(postJson("/api/v1/auth/password/forgot", Map.of("email", email))).andExpect(status().isAccepted());
		String reset = tokenFromOutbox("PASSWORD_RESET", email);
		this.mvc.perform(postJson("/api/v1/auth/password/reset", Map.of("token", reset, "password", "clave-nueva-larga-2026")))
			.andExpect(status().isNoContent());

		String freshToken = login(email, "clave-nueva-larga-2026");
		this.mvc.perform(authed(get("/api/v1/account/me").with(randomIp()), freshToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.email").value(email));

		Awaitility.await().atMost(Duration.ofSeconds(15)).pollInterval(Duration.ofMillis(500)).untilAsserted(() -> this.mvc
			.perform(authed(get("/api/v1/account/me").with(randomIp()), oldToken))
			.andExpect(status().isUnauthorized()));
	}

}
