package com.fundigsac.shared.web;

import java.time.Duration;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** CORS cerrado a los orígenes configurados (sin credenciales de navegador: la API usa tokens Bearer). */
@Configuration(proxyBeanMethods = false)
class WebConfig implements WebMvcConfigurer {

	private final RateLimitInterceptor rateLimitInterceptor;

	WebConfig(RateLimitInterceptor rateLimitInterceptor) {
		this.rateLimitInterceptor = rateLimitInterceptor;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(this.rateLimitInterceptor).addPathPatterns("/api/**");
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource(WebProperties properties) {
		CorsConfiguration cors = new CorsConfiguration();
		cors.setAllowedOrigins(properties.allowedOrigins());
		cors.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		cors.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "Accept-Language", "Idempotency-Key",
				"If-Match", "X-Request-Id", "X-Receipt-Token"));
		cors.setExposedHeaders(List.of("ETag", "Location", "Retry-After", "RateLimit-Remaining", "X-Request-Id"));
		cors.setAllowCredentials(false);
		cors.setMaxAge(Duration.ofHours(1));
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/api/**", cors);
		return source;
	}

}
