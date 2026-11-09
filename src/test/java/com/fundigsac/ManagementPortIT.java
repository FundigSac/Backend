package com.fundigsac;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import com.fundigsac.support.IntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Actuator se sirve solo en el puerto interno 8081: no responde por la API pública ni se publica fuera del contenedor. */
class ManagementPortIT extends IntegrationTest {

	@Autowired
	Environment environment;

	@ParameterizedTest
	@ValueSource(strings = { "/actuator", "/actuator/health", "/actuator/health/readiness", "/actuator/prometheus", "/actuator/info" })
	void thePublicPortDoesNotServeActuator(String path) throws Exception {
		this.mvc.perform(get(path).with(randomIp())).andExpect(status().isUnauthorized());
	}

	@Test
	void actuatorHasItsOwnPort() {
		assertThat(this.environment.getProperty("management.server.port", Integer.class)).isEqualTo(8081);
		assertThat(this.environment.getProperty("server.port", Integer.class)).isEqualTo(8080);
	}

	@Test
	void composeOnlyPublishesTheApiPortOfTheBackend() throws Exception {
		String compose = Files.readString(Path.of("compose.yaml"));
		Matcher backend = Pattern.compile("(?s)\\n  backend:\\n(.*?)\\n\\S").matcher(compose);
		assertThat(backend.find()).isTrue();
		Matcher ports = Pattern.compile("(?s)\\n    ports:\\n(.*?)\\n    \\S").matcher(backend.group(1));
		assertThat(ports.find()).isTrue();
		assertThat(ports.group(1).strip()).isEqualTo("- \"127.0.0.1:8080:8080\"");
	}

}
