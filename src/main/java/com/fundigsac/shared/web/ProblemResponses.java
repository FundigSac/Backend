package com.fundigsac.shared.web;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.SpecVersion;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;

import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.method.HandlerMethod;

/**
 * Documenta en OpenAPI los errores RFC 9457 de cada operación. Se deducen de su firma, para que una operación nueva quede
 * documentada sin esfuerzo: parámetros (400), cuerpo (413 y 415), ruta con identificador (404), If-Match (412 y 428),
 * límite de frecuencia (429) y área protegida (403). Las pruebas de integración comparan cada respuesta real con este
 * contrato, así que un código que no esté aquí hace fallar la suite.
 */
final class ProblemResponses {

	static final String SCHEMA_NAME = "Problem";

	private static final String MEDIA_TYPE = "application/problem+json";

	private static final String SCHEMA_REF = "#/components/schemas/" + SCHEMA_NAME;

	private static final List<String> PROTECTED_AREAS = List.of("/api/v1/staff", "/api/v1/account", "/api/v1/cart");

	private static final Map<Integer, String> DESCRIPTIONS = Map.ofEntries(Map.entry(400, "Datos o parámetros no válidos"),
			Map.entry(401, "Falta la sesión o el token Bearer no es válido"), Map.entry(403, "Sin permiso para esta operación"),
			Map.entry(404, "El recurso no existe o no pertenece a quien lo solicita"),
			Map.entry(409, "Conflicto con el estado actual del registro o clave de idempotencia reutilizada"),
			Map.entry(412, "El registro cambió: el ETag de If-Match ya no es vigente"),
			Map.entry(413, "El cuerpo supera el tamaño permitido"), Map.entry(415, "Tipo de contenido no admitido"),
			Map.entry(422, "No se cumple una regla de negocio"), Map.entry(428, "Falta la cabecera If-Match"),
			Map.entry(429, "Demasiadas solicitudes: vea la cabecera Retry-After"), Map.entry(500, "Error inesperado"));

	private ProblemResponses() {
	}

	static Operation document(Operation operation, HandlerMethod handler) {
		for (int code : problemsOf(handler)) {
			operation.getResponses().addApiResponse(Integer.toString(code), new ApiResponse().description(DESCRIPTIONS.get(code))
				.content(new Content().addMediaType(MEDIA_TYPE, new MediaType().schema(new Schema<>().$ref(SCHEMA_REF)))));
		}
		return operation;
	}

	/** Esquema de los errores: ProblemDetail con los campos propios de la API. */
	static Schema<?> problemSchema() {
		Schema<?> fieldProblem = object().required(List.of("field", "code", "message"))
			.addProperty("field", string())
			.addProperty("code", string())
			.addProperty("message", string());
		return object().required(List.of("title", "status", "detail", "code"))
			.addProperty("type", string().format("uri"))
			.addProperty("title", string())
			.addProperty("status", integer())
			.addProperty("detail", string())
			.addProperty("instance", string())
			.addProperty("code", string())
			.addProperty("requestId", string())
			.addProperty("errors", typed("array").items(fieldProblem));
	}

	private static Set<Integer> problemsOf(HandlerMethod handler) {
		Set<Integer> codes = new TreeSet<>(Set.of(401, 500));
		String path = pathOf(handler);
		boolean reads = isReadOnly(handler);
		if (PROTECTED_AREAS.stream().anyMatch(path::startsWith)) {
			codes.add(403);
		}
		if (path.startsWith("/api/v1/staff") && !reads) {
			codes.addAll(Set.of(409, 422));
		}
		for (MethodParameter parameter : handler.getMethodParameters()) {
			addFor(parameter, codes);
		}
		if (handler.hasMethodAnnotation(RateLimited.class)) {
			codes.add(429);
		}
		ApiProblems extra = handler.getMethodAnnotation(ApiProblems.class);
		if (extra != null) {
			Arrays.stream(extra.value()).forEach(codes::add);
		}
		return codes;
	}

	private static void addFor(MethodParameter parameter, Set<Integer> codes) {
		if (parameter.hasParameterAnnotation(RequestBody.class)) {
			codes.addAll(Set.of(400, 413, 415));
		}
		if (parameter.hasParameterAnnotation(PathVariable.class)) {
			codes.addAll(Set.of(400, 404));
		}
		if (parameter.hasParameterAnnotation(RequestParam.class)) {
			codes.add(400);
		}
		RequestHeader header = parameter.getParameterAnnotation(RequestHeader.class);
		if (header != null) {
			codes.add(400);
			if ("If-Match".equalsIgnoreCase(header.name())) {
				codes.addAll(Set.of(412, 428));
			}
			if ("Idempotency-Key".equalsIgnoreCase(header.name())) {
				codes.add(409);
			}
		}
	}

	private static boolean isReadOnly(HandlerMethod handler) {
		RequestMapping mapping = AnnotatedElementUtils.findMergedAnnotation(handler.getMethod(), RequestMapping.class);
		return mapping != null && Arrays.asList(mapping.method()).contains(RequestMethod.GET);
	}

	private static String pathOf(HandlerMethod handler) {
		return first(AnnotatedElementUtils.findMergedAnnotation(handler.getBeanType(), RequestMapping.class))
				+ first(AnnotatedElementUtils.findMergedAnnotation(handler.getMethod(), RequestMapping.class));
	}

	private static String first(RequestMapping mapping) {
		return mapping == null || mapping.path().length == 0 ? "" : mapping.path()[0];
	}

	private static Schema<?> object() {
		return typed("object");
	}

	private static Schema<?> string() {
		return typed("string");
	}

	private static Schema<?> integer() {
		return typed("integer");
	}

	private static Schema<?> typed(String type) {
		Schema<?> schema = new Schema<>(SpecVersion.V31);
		schema.setTypes(Set.of(type));
		return schema;
	}

}
