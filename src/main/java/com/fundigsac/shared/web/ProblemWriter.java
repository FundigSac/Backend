package com.fundigsac.shared.web;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import tools.jackson.databind.json.JsonMapper;

/** Escribe respuestas RFC 9457 desde filtros (fuera de Spring MVC), con el mismo formato que ApiExceptionHandler. */
@Component
public class ProblemWriter {

	private final JsonMapper json;

	public ProblemWriter(JsonMapper json) {
		this.json = json;
	}

	public void write(HttpServletResponse response, HttpStatus status, String code, String detail) throws IOException {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("type", "about:blank");
		body.put("title", status.getReasonPhrase());
		body.put("status", status.value());
		body.put("detail", detail);
		body.put("code", code);
		String requestId = MDC.get(RequestIdFilter.MDC_KEY);
		if (requestId != null) {
			body.put("requestId", requestId);
		}
		response.setStatus(status.value());
		response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		this.json.writeValue(response.getOutputStream(), body);
	}

}
