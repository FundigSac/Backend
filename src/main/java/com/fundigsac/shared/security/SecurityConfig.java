package com.fundigsac.shared.security;

import org.springframework.boot.security.autoconfigure.actuate.web.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;
import org.springframework.security.web.header.writers.StaticHeadersWriter;

/**
 * Seguridad de la API: sin sesiones ni cookies (tokens Bearer JWT), por eso CSRF no aplica. Todo lo que no está
 * listado explícitamente se niega. Los permisos finos se comprueban además en cada caso de uso (@PreAuthorize).
 */
@Configuration(proxyBeanMethods = false)
@EnableMethodSecurity
class SecurityConfig {

	private static final String API_CSP = "default-src 'none'; frame-ancestors 'none'; base-uri 'none'; form-action 'none'";

	private static final String DOCS_CSP = "default-src 'self'; img-src 'self' data:; style-src 'self' 'unsafe-inline'; "
			+ "script-src 'self'; connect-src 'self'; frame-ancestors 'none'; base-uri 'none'; form-action 'self'";

	private static final String PERMISSIONS_POLICY = "accelerometer=(), camera=(), geolocation=(), gyroscope=(), "
			+ "magnetometer=(), microphone=(), payment=(), usb=(), browsing-topics=()";

	/** Swagger UI y OpenAPI (solo si están habilitados): CSP algo más abierta porque la UI carga sus scripts. */
	@Bean
	@Order(1)
	SecurityFilterChain docsSecurity(HttpSecurity http) throws Exception {
		http.securityMatcher("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**")
			.authorizeHttpRequests((auth) -> auth.anyRequest().permitAll())
			.csrf(AbstractHttpConfigurer::disable)
			.sessionManagement((s) -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.requestCache(AbstractHttpConfigurer::disable)
			.headers((headers) -> commonHeaders(headers, DOCS_CSP));
		return http.build();
	}

	@Bean
	@Order(2)
	SecurityFilterChain apiSecurity(HttpSecurity http,
			Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter,
			ProblemAuthenticationEntryPoint entryPoint, ProblemAccessDeniedHandler accessDeniedHandler)
			throws Exception {
		http.csrf(AbstractHttpConfigurer::disable)
			.cors(Customizer.withDefaults())
			.sessionManagement((s) -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.requestCache(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.logout(AbstractHttpConfigurer::disable)
			.anonymous(Customizer.withDefaults())
			.authorizeHttpRequests((auth) -> auth
				// Puerto de gestión (8081, no publicado fuera del contenedor): salud y métricas.
				.requestMatchers(EndpointRequest.toAnyEndpoint()).permitAll()
				.requestMatchers(HttpMethod.OPTIONS, "/api/**").permitAll()
				// Lectura pública del catálogo, documentos y textos legales vigentes.
				.requestMatchers(HttpMethod.GET, "/api/v1/catalog/**", "/api/v1/resources/**", "/api/v1/legal/**").permitAll()
				// Formularios públicos (sin cuenta) y sus constancias con token.
				.requestMatchers(HttpMethod.POST, "/api/v1/inquiries", "/api/v1/quotes", "/api/v1/complaints",
						"/api/v1/privacy/consents", "/api/v1/privacy/requests").permitAll()
				.requestMatchers(HttpMethod.GET, "/api/v1/quotes/*/receipt", "/api/v1/complaints/*/receipt").permitAll()
				// Acceso: registro, verificación, ingreso, renovación, salida y recuperación de contraseña.
				.requestMatchers(HttpMethod.POST, "/api/v1/auth/**").permitAll()
				// Área del cliente y del personal: siempre autenticadas; el permiso fino lo exige cada endpoint.
				.requestMatchers("/api/v1/account/**", "/api/v1/cart/**").hasRole("CUSTOMER")
				.requestMatchers("/api/v1/staff/**").hasRole("STAFF")
				.requestMatchers("/error").permitAll()
				.anyRequest().denyAll())
			.oauth2ResourceServer((oauth2) -> oauth2
				.jwt((jwt) -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
				.authenticationEntryPoint(entryPoint)
				.accessDeniedHandler(accessDeniedHandler))
			.exceptionHandling((ex) -> ex.authenticationEntryPoint(entryPoint).accessDeniedHandler(accessDeniedHandler))
			.headers((headers) -> commonHeaders(headers, API_CSP));
		return http.build();
	}

	private static void commonHeaders(HeadersConfigurer<HttpSecurity> headers, String csp) {
		headers.contentSecurityPolicy((c) -> c.policyDirectives(csp))
			.frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
			.referrerPolicy((r) -> r.policy(ReferrerPolicy.NO_REFERRER))
			.httpStrictTransportSecurity((hsts) -> hsts.includeSubDomains(true).maxAgeInSeconds(31_536_000))
			.addHeaderWriter(new StaticHeadersWriter("Permissions-Policy", PERMISSIONS_POLICY))
			.addHeaderWriter(new StaticHeadersWriter("Cross-Origin-Resource-Policy", "same-origin"))
			.addHeaderWriter(new StaticHeadersWriter("Cross-Origin-Opener-Policy", "same-origin"));
	}

}
