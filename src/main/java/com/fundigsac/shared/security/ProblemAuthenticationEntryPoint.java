package com.fundigsac.shared.security;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fundigsac.shared.web.ProblemWriter;

/** 401 en formato RFC 9457 (application/problem+json) con la cabecera WWW-Authenticate de Bearer. */
@Component
class ProblemAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private final ProblemWriter problems;

	ProblemAuthenticationEntryPoint(ProblemWriter problems) {
		this.problems = problems;
	}

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException ex)
			throws IOException {
		boolean invalidToken = ex instanceof InvalidBearerTokenException;
		response.setHeader("WWW-Authenticate",
				invalidToken ? "Bearer error=\"invalid_token\", error_description=\"El token no es válido o expiró\"" : "Bearer");
		this.problems.write(response, HttpStatus.UNAUTHORIZED, invalidToken ? "INVALID_TOKEN" : "AUTHENTICATION_REQUIRED",
				invalidToken ? "La sesión no es válida o expiró. Inicie sesión nuevamente." : "Inicie sesión para continuar.");
	}

}
