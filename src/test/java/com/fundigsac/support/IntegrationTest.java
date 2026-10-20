package com.fundigsac.support;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.awaitility.Awaitility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Base de las pruebas de integración: aplicación completa + PostgreSQL 18 en Testcontainers + MockMvc. Cada petición
 * usa una IP aleatoria para que los límites de frecuencia no interfieran entre pruebas (salvo la prueba de límites).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({ TestcontainersConfiguration.class, FakeGoogleConfiguration.class })
public abstract class IntegrationTest {

	public static final String ADMIN_EMAIL = "admin@fundigsac.test";

	public static final String ADMIN_PASSWORD = "Prueba-Segura-2026!";

	private static final Pattern TOKEN = Pattern.compile("#token=([A-Za-z0-9_-]{20,})");

	private static final Pattern LOGIN_CODE = Pattern.compile("(?m)^\\s+(\\d{6})\\s*$");

	@Autowired
	protected MockMvc mvc;

	@Autowired
	protected JsonMapper json;

	@Autowired
	protected JdbcClient jdbc;

	protected static RequestPostProcessor randomIp() {
		String ip = "10." + ThreadLocalRandom.current().nextInt(1, 255) + "." + ThreadLocalRandom.current().nextInt(1, 255)
				+ "." + ThreadLocalRandom.current().nextInt(1, 255);
		return (request) -> {
			request.setRemoteAddr(ip);
			return request;
		};
	}

	protected MockHttpServletRequestBuilder postJson(String url, Object body) {
		return post(url).with(randomIp()).contentType(MediaType.APPLICATION_JSON).content(this.json.writeValueAsString(body));
	}

	protected MockHttpServletRequestBuilder authed(MockHttpServletRequestBuilder builder, String token) {
		return builder.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
	}

	protected JsonNode body(MvcResult result) throws Exception {
		return this.json.readTree(result.getResponse().getContentAsString());
	}

	protected String login(String email, String password) throws Exception {
		MvcResult result = this.mvc.perform(postJson("/api/v1/auth/login", Map.of("email", email, "password", password)))
			.andReturn();
		if (result.getResponse().getStatus() != 200) {
			throw new IllegalStateException("Login falló: " + result.getResponse().getContentAsString());
		}
		return body(result).get("accessToken").asString();
	}

	protected String adminToken() throws Exception {
		return login(ADMIN_EMAIL, ADMIN_PASSWORD);
	}

	protected MvcResult getWithToken(String url, String token) throws Exception {
		return this.mvc.perform(authed(get(url).with(randomIp()), token)).andReturn();
	}

	/** Espera a que el correo de una plantilla llegue a la bandeja de salida y devuelve el token del enlace. */
	protected String tokenFromOutbox(String template, String recipient) {
		return tokenFromOutboxBody(template, recipient, TOKEN);
	}

	/** Espera el correo de una plantilla y devuelve el primer grupo del patrón dentro de su cuerpo. */
	protected String tokenFromOutboxBody(String template, String recipient, Pattern pattern) {
		String[] body = new String[1];
		Awaitility.await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
			body[0] = this.jdbc.sql("""
					SELECT body FROM email_outbox WHERE template = :template AND recipient = :recipient AND body IS NOT NULL
					 ORDER BY created_at DESC LIMIT 1""")
				.param("template", template)
				.param("recipient", recipient)
				.query(String.class)
				.optional()
				.orElseThrow();
		});
		Matcher m = pattern.matcher(body[0]);
		if (!m.find()) {
			throw new IllegalStateException("Sin token en el correo " + template);
		}
		return m.group(1);
	}

	protected long count(String sql, Map<String, Object> params) {
		return this.jdbc.sql(sql).params(params).query(Long.class).single();
	}

	/** Cliente registrado, verificado y con sesión iniciada; devuelve su token de acceso. */
	protected String customerToken(String email, String password) throws Exception {
		Map<String, Object> register = new java.util.HashMap<>();
		register.put("email", email);
		register.put("password", password);
		register.put("fullName", "Cliente de Prueba");
		register.put("companyName", "Constructora Andina SAC");
		register.put("ruc", "20131312955");
		register.put("phone", "+51 987 654 321");
		register.put("privacyNoticeVersion", "2026-10");
		register.put("acceptPrivacyNotice", true);
		this.mvc.perform(postJson("/api/v1/auth/register", register)).andReturn();
		String token = tokenFromOutbox("VERIFY_EMAIL", email);
		this.mvc.perform(postJson("/api/v1/auth/verify-email", Map.of("token", token))).andReturn();
		return login(email, password);
	}

	/** Personal invitado por el administrador con los roles dados; entra con el código del correo y devuelve su token. */
	protected String staffToken(String email, String... roles) throws Exception {
		this.mvc.perform(authed(postJson("/api/v1/staff/users",
				Map.of("email", email, "fullName", "Personal de Prueba", "roles", java.util.List.of(roles))), adminToken()))
			.andReturn();
		tokenFromOutboxBody("STAFF_INVITATION", email, Pattern.compile("(/login)"));
		this.mvc.perform(postJson("/api/v1/auth/staff/code", Map.of("email", email))).andReturn();
		String code = loginCodeFromOutbox(email);
		MvcResult result = this.mvc.perform(postJson("/api/v1/auth/staff/code/verify", Map.of("email", email, "code", code)))
			.andReturn();
		if (result.getResponse().getStatus() != 200) {
			throw new IllegalStateException("Ingreso con código falló: " + result.getResponse().getContentAsString());
		}
		return body(result).get("accessToken").asString();
	}

	/** Código de 6 dígitos del último correo de acceso enviado al personal. */
	protected String loginCodeFromOutbox(String recipient) {
		return tokenFromOutboxBody("STAFF_LOGIN_CODE", recipient, LOGIN_CODE);
	}

}
