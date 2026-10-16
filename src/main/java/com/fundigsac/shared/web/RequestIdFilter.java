package com.fundigsac.shared.web;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Correlación: toma X-Request-Id si tiene un formato seguro (o genera uno), lo pone en los logs (MDC) y en la
 * respuesta, y registra una línea de acceso por petición sin cuerpo, sin query string y sin datos personales.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestIdFilter extends OncePerRequestFilter {

	public static final String HEADER = "X-Request-Id";

	public static final String MDC_KEY = "requestId";

	private static final Pattern SAFE_ID = Pattern.compile("^[A-Za-z0-9._-]{8,64}$");

	private static final Logger log = LoggerFactory.getLogger("fundigsac.access");

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		String incoming = request.getHeader(HEADER);
		String requestId = incoming != null && SAFE_ID.matcher(incoming).matches() ? incoming : UUID.randomUUID().toString();
		MDC.put(MDC_KEY, requestId);
		response.setHeader(HEADER, requestId);
		long start = System.nanoTime();
		try {
			chain.doFilter(request, response);
		}
		finally {
			if (!request.getRequestURI().startsWith("/actuator")) {
				log.info("{} {} -> {} ({} ms)", request.getMethod(), request.getRequestURI(), response.getStatus(),
						(System.nanoTime() - start) / 1_000_000);
			}
			MDC.remove(MDC_KEY);
		}
	}

}
