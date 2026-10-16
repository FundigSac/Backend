package com.fundigsac.shared.web;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Documentación OpenAPI 3.1 agrupada por audiencia: pública, cuenta del cliente y personal interno. */
@Configuration(proxyBeanMethods = false)
class OpenApiConfig {

	static final String BEARER = ApiDocs.BEARER;

	/** Grupo que Swagger UI abre por defecto (springdoc.swagger-ui.urls-primary-name). */
	static final String PUBLIC_GROUP = "1. Pública (sin cuenta)";

	@Bean
	OpenAPI fundigsacOpenApi() {
		return new OpenAPI()
			.info(new Info().title("FUNDIGSAC API")
				.version("v1")
				.description("""
						API del sitio de FUNDIGSAC: catálogo técnico, cotizaciones, consultas, libro de reclamaciones, \
						privacidad e identidad. Errores en formato RFC 9457 (application/problem+json) con `code`, \
						`errors` y `requestId`. Los formularios públicos aceptan `Idempotency-Key`; las modificaciones del \
						personal exigen `If-Match` con el ETag del registro.""")
				.contact(new Contact().name("FUNDIGSAC").email("ventas@fundigsac.com")))
			.components(new Components().addSecuritySchemes(BEARER,
					new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
						.description("Token de acceso de POST /api/v1/auth/login (15 minutos).")));
	}

	@Bean
	GroupedOpenApi publicApi() {
		return GroupedOpenApi.builder()
			.group("1-publica")
			.displayName(PUBLIC_GROUP)
			.pathsToMatch("/api/v1/**")
			.pathsToExclude("/api/v1/staff/**", "/api/v1/account/**", "/api/v1/cart/**", "/api/v1/auth/**")
			.build();
	}

	@Bean
	GroupedOpenApi accountApi() {
		return GroupedOpenApi.builder()
			.group("2-cuenta")
			.displayName("2. Acceso y cuenta del cliente")
			.pathsToMatch("/api/v1/auth/**", "/api/v1/account/**", "/api/v1/cart/**")
			.build();
	}

	@Bean
	GroupedOpenApi staffApi() {
		return GroupedOpenApi.builder()
			.group("3-personal")
			.displayName("3. Personal interno (bandejas y administración)")
			.pathsToMatch("/api/v1/staff/**")
			.build();
	}

}
