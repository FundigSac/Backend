package com.fundigsac.shared.security;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.fundigsac.shared.web.ProblemWriter;

/** 403 en formato RFC 9457: hay sesión, pero el rol o permiso no alcanza. */
@Component
class ProblemAccessDeniedHandler implements AccessDeniedHandler {

	private final ProblemWriter problems;

	ProblemAccessDeniedHandler(ProblemWriter problems) {
		this.problems = problems;
	}

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex)
			throws IOException {
		this.problems.write(response, HttpStatus.FORBIDDEN, "FORBIDDEN", "No tiene permiso para realizar esta acción.");
	}

}
