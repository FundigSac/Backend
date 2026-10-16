package com.fundigsac.shared.web;

import java.time.Duration;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;

import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Token bucket por (política, IP) en memoria con expiración: suficiente para una instancia. Con varias instancias
 * se reemplaza el almacén por Bucket4j sobre PostgreSQL o Redis sin tocar los controladores.
 */
@Component
class RateLimitInterceptor implements HandlerInterceptor {

	private final RateLimitProperties properties;

	private final Cache<String, Bucket> buckets = Caffeine.newBuilder()
		.maximumSize(100_000)
		.expireAfterAccess(Duration.ofHours(2))
		.build();

	RateLimitInterceptor(RateLimitProperties properties) {
		this.properties = properties;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		if (!this.properties.enabled() || !(handler instanceof HandlerMethod method)) {
			return true;
		}
		RateLimited limit = method.getMethodAnnotation(RateLimited.class);
		if (limit == null) {
			return true;
		}
		RateLimitProperties.Policy policy = this.properties.policies().get(limit.value());
		if (policy == null) {
			throw new IllegalStateException("Política de límite no configurada: " + limit.value());
		}
		Bucket bucket = this.buckets.get(limit.value() + "|" + request.getRemoteAddr(), (key) -> Bucket.builder()
			.addLimit(Bandwidth.builder().capacity(policy.capacity()).refillGreedy(policy.capacity(), policy.period()).build())
			.build());
		ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
		if (!probe.isConsumed()) {
			long seconds = Math.max(1, Duration.ofNanos(probe.getNanosToWaitForRefill()).toSeconds());
			throw new TooManyRequestsException(seconds);
		}
		response.setHeader("RateLimit-Remaining", Long.toString(probe.getRemainingTokens()));
		return true;
	}

}
