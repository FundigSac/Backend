package com.fundigsac;

import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.fundigsac.support.TestcontainersConfiguration;
import tools.jackson.databind.json.JsonMapper;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * La aplicación arranca con application-prod.yml y sin claves temporales: firma con la clave PEM configurada, no carga
 * datos de demostración y mantiene apagada la documentación. Las claves se generan aquí solo para esta prueba.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("prod")
@Import(TestcontainersConfiguration.class)
class ProductionProfileIT {

	private static final String ADMIN = "admin@fundigsac.prueba";

	private static final String PASSWORD = "Clave-De-Prueba-Prod-2026!";

	@Autowired
	MockMvc mvc;

	@Autowired
	JsonMapper json;

	@DynamicPropertySource
	static void productionSecrets(DynamicPropertyRegistry registry) throws NoSuchAlgorithmException {
		KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
		generator.initialize(2048);
		String pem = "-----BEGIN PRIVATE KEY-----\n"
				+ Base64.getMimeEncoder().encodeToString(generator.generateKeyPair().getPrivate().getEncoded())
				+ "\n-----END PRIVATE KEY-----";
		registry.add("fundigsac.identity.jwt.private-key", () -> pem);
		registry.add("fundigsac.security.receipt-key", () -> Base64.getEncoder().encodeToString(new byte[32]));
		registry.add("fundigsac.identity.bootstrap-admin.email", () -> ADMIN);
		registry.add("fundigsac.identity.bootstrap-admin.password", () -> PASSWORD);
		registry.add("fundigsac.web.allowed-origins", () -> "https://sitio.fundigsac.example");
	}

	private String adminToken() throws Exception {
		String response = this.mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
			.content(this.json.writeValueAsString(Map.of("email", ADMIN, "password", PASSWORD))))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();
		return this.json.readTree(response).get("accessToken").asString();
	}

	@Test
	void signsAndValidatesTokensWithTheConfiguredKey() throws Exception {
		this.mvc.perform(get("/api/v1/staff/profile").header("Authorization", "Bearer " + adminToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.email").value(ADMIN));
	}

	@Test
	void keepsTheApiDocumentationOff() throws Exception {
		this.mvc.perform(get("/v3/api-docs")).andExpect(status().isNotFound());
		this.mvc.perform(get("/swagger-ui.html")).andExpect(status().isNotFound());
	}

	@Test
	void doesNotLoadDemonstrationData() throws Exception {
		this.mvc.perform(get("/api/v1/catalog/categories")).andExpect(status().isOk()).andExpect(jsonPath("$").isEmpty());
	}

	@Test
	void disablesGoogleWithoutAClientId() throws Exception {
		this.mvc.perform(post("/api/v1/auth/google").contentType(MediaType.APPLICATION_JSON).content("{\"idToken\":\"cualquiera\"}"))
			.andExpect(status().isUnprocessableContent())
			.andExpect(jsonPath("$.code").value("GOOGLE_SIGN_IN_DISABLED"));
	}

	@Test
	void sendsTransportSecurityHeadersOnHttps() throws Exception {
		this.mvc.perform(get("/api/v1/catalog/categories").secure(true))
			.andExpect(status().isOk())
			.andExpect(header().string("Strict-Transport-Security", containsString("max-age=31536000")));
	}

}
