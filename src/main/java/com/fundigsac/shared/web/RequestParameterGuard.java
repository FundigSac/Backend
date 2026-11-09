package com.fundigsac.shared.web;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.fundigsac.shared.domain.Guard;

/**
 * Rechaza con 400 los parámetros de consulta con U+0000 antes de que lleguen a un filtro SQL: PostgreSQL no admite ese
 * carácter y el error de la base de datos se confundiría con un conflicto.
 */
@Component
class RequestParameterGuard implements HandlerInterceptor {

	private static final String FIELD_OF_INVALID_NAME = "query";

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		for (Map.Entry<String, String[]> parameter : request.getParameterMap().entrySet()) {
			String name = parameter.getKey();
			Guard.noNullCharacter(FIELD_OF_INVALID_NAME, name);
			for (String value : parameter.getValue()) {
				Guard.noNullCharacter(name, value);
			}
		}
		return true;
	}

}
