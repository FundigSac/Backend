package com.fundigsac.shared.security;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.security.web.firewall.RequestRejectedHandler;
import org.springframework.stereotype.Component;

import com.fundigsac.shared.web.ProblemWriter;

/**
 * URL rechazada por el firewall de Spring Security (dobles barras, recorridos de ruta, caracteres no permitidos):
 * responde 400 en el mismo formato RFC 9457 que el resto de la API.
 */
@Component
class ProblemRequestRejectedHandler implements RequestRejectedHandler {

	private final ProblemWriter problems;

	ProblemRequestRejectedHandler(ProblemWriter problems) {
		this.problems = problems;
	}

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response, RequestRejectedException ex)
			throws IOException {
		this.problems.write(response, HttpStatus.BAD_REQUEST, "REQUEST_REJECTED", "La dirección de la solicitud no es válida.");
	}

}
