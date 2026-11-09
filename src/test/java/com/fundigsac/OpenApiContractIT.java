package com.fundigsac;

import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ResolvableType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.fundigsac.shared.web.RateLimited;
import com.fundigsac.support.IntegrationTest;
import com.fundigsac.support.OpenApiContractConfiguration.ContractCheck;
import tools.jackson.databind.JsonNode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * El contrato OpenAPI dice la verdad: lista todas las rutas, documenta el código de éxito real y los errores RFC 9457. La
 * comprobación de cada respuesta real corre en todas las pruebas de integración (OpenApiContractConfiguration).
 */
class OpenApiContractIT extends IntegrationTest {

	/** Operaciones cuyo éxito no es 200 ni 204. Escrito a mano: es lo que responde la API. */
	private static final Map<String, String> NON_DEFAULT_SUCCESS = Map.ofEntries(Map.entry("POST /api/v1/quotes", "201"),
			Map.entry("POST /api/v1/complaints", "201"), Map.entry("POST /api/v1/inquiries", "201"),
			Map.entry("POST /api/v1/privacy/requests", "201"), Map.entry("POST /api/v1/privacy/consents", "201"),
			Map.entry("POST /api/v1/cart/submit", "201"), Map.entry("POST /api/v1/staff/users", "201"),
			Map.entry("POST /api/v1/staff/catalog/products", "201"), Map.entry("POST /api/v1/staff/catalog/documents", "201"),
			Map.entry("POST /api/v1/staff/legal/{type}/versions", "201"), Map.entry("POST /api/v1/auth/register", "202"),
			Map.entry("POST /api/v1/auth/verify-email/resend", "202"), Map.entry("POST /api/v1/auth/password/forgot", "202"),
			Map.entry("POST /api/v1/auth/staff/code", "202"), Map.entry("POST /api/v1/staff/accounts/{id}/verification-email", "202"),
			Map.entry("POST /api/v1/staff/accounts/{id}/password-reset", "202"), Map.entry("GET /api/v1/resources/{slug}/download", "302"));

	@Autowired
	@Qualifier("requestMappingHandlerMapping")
	RequestMappingHandlerMapping mappings;

	@Autowired
	ContractCheck contractCheck;

	private JsonNode document() throws Exception {
		return this.json.readTree(this.mvc.perform(get("/v3/api-docs").with(randomIp())).andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray());
	}

	private Map<String, JsonNode> operations() throws Exception {
		Map<String, JsonNode> operations = new TreeMap<>();
		document().get("paths").properties().forEach((path) -> path.getValue().properties().forEach((operation) -> operations
			.put(operation.getKey().toUpperCase(java.util.Locale.ROOT) + " " + path.getKey(), operation.getValue())));
		return operations;
	}

	private Map<String, HandlerMethod> handlers() {
		Map<String, HandlerMethod> handlers = new TreeMap<>();
		this.mappings.getHandlerMethods().forEach((RequestMappingInfo info, HandlerMethod handler) -> info.getPathPatternsCondition().getPatternValues().forEach((path) -> {
			if (path.startsWith("/api/")) {
				info.getMethodsCondition().getMethods().forEach((method) -> handlers.put(method + " " + path, handler));
			}
		}));
		return handlers;
	}

	private static Set<String> responseCodes(JsonNode operation) {
		Set<String> codes = new TreeSet<>();
		operation.get("responses").propertyNames().forEach(codes::add);
		return codes;
	}

	private static boolean readsHeader(HandlerMethod handler, String name) {
		for (var parameter : handler.getMethodParameters()) {
			RequestHeader header = parameter.getParameterAnnotation(RequestHeader.class);
			if (header != null && name.equalsIgnoreCase(header.name())) {
				return true;
			}
		}
		return false;
	}

	@Test
	void theContractListsEveryRouteOfTheApi() throws Exception {
		assertThat(operations().keySet()).isEqualTo(handlers().keySet()).hasSize(97);
		assertThat(this.contractCheck.contract().operationCount()).isEqualTo(97);
	}

	@Test
	void theSuccessCodeIsTheOneTheApiReallyAnswers() throws Exception {
		Map<String, JsonNode> operations = operations();
		NON_DEFAULT_SUCCESS.forEach((key, code) -> assertThat(responseCodes(operations.get(key))).as(key).contains(code).doesNotContain("200"));
		operations.forEach((key, operation) -> {
			if (!NON_DEFAULT_SUCCESS.containsKey(key)) {
				assertThat(responseCodes(operation)).as(key).containsAnyOf("200", "204");
			}
		});
	}

	@Test
	void everyOperationDocumentsItsProblems() throws Exception {
		Map<String, HandlerMethod> handlers = handlers();
		operations().forEach((key, operation) -> {
			Set<String> codes = responseCodes(operation);
			assertThat(codes).as(key).contains("401", "500");
			String path = key.substring(key.indexOf(' ') + 1);
			if (path.startsWith("/api/v1/staff") || path.startsWith("/api/v1/account") || path.startsWith("/api/v1/cart")) {
				assertThat(codes).as(key).contains("403");
			}
			if (path.contains("{")) {
				assertThat(codes).as(key).contains("404");
			}
			if (operation.has("requestBody")) {
				assertThat(codes).as(key).contains("400", "413", "415");
			}
			HandlerMethod handler = handlers.get(key);
			if (handler.hasMethodAnnotation(RateLimited.class)) {
				assertThat(codes).as(key).contains("429");
			}
			if (readsHeader(handler, "If-Match")) {
				assertThat(codes).as(key).contains("412", "428");
			}
			if (readsHeader(handler, "Idempotency-Key")) {
				assertThat(codes).as(key).contains("409");
			}
			codes.stream().filter((code) -> code.charAt(0) >= '4').forEach((code) -> assertThat(operation.get("responses").get(code).get("content").has("application/problem+json"))
				.as("%s %s", key, code).isTrue());
		});
	}

	/**
	 * springdoc nombra los esquemas por el nombre simple de la clase: dos clases distintas con el mismo nombre se funden en
	 * un solo esquema y el contrato deja de describir a una de ellas.
	 */
	@Test
	void differentPayloadsNeverShareASchemaName() {
		Map<String, String> shapes = new TreeMap<>();
		Set<String> collisions = new TreeSet<>();
		Set<Class<?>> visited = new HashSet<>();
		handlers().values().forEach((handler) -> {
			visit(ResolvableType.forMethodReturnType(handler.getMethod()), shapes, collisions, visited);
			for (var parameter : handler.getMethodParameters()) {
				if (parameter.hasParameterAnnotation(RequestBody.class)) {
					visit(ResolvableType.forMethodParameter(parameter), shapes, collisions, visited);
				}
			}
		});
		assertThat(collisions).isEmpty();
	}

	private static void visit(ResolvableType type, Map<String, String> shapes, Set<String> collisions, Set<Class<?>> visited) {
		for (ResolvableType generic : type.getGenerics()) {
			visit(generic, shapes, collisions, visited);
		}
		if (type.isArray()) {
			visit(type.getComponentType(), shapes, collisions, visited);
		}
		Class<?> raw = type.resolve();
		if (raw == null || raw.isArray() || !raw.getName().startsWith("com.fundigsac") || !visited.add(raw)) {
			return;
		}
		String shape = raw.isRecord()
				? Arrays.stream(raw.getRecordComponents()).map((c) -> c.getName() + ":" + c.getGenericType()).toList().toString()
				: raw.isEnum() ? Arrays.toString(raw.getEnumConstants()) : raw.getName();
		String previous = shapes.putIfAbsent(raw.getSimpleName(), shape);
		if (previous != null && !previous.equals(shape)) {
			collisions.add(raw.getSimpleName() + ": " + previous + " <> " + shape);
		}
		if (raw.isRecord()) {
			for (RecordComponent component : raw.getRecordComponents()) {
				visit(ResolvableType.forMethodReturnType(component.getAccessor()), shapes, collisions, visited);
			}
		}
	}

	@Test
	void theProblemSchemaDescribesTheErrorFormat() throws Exception {
		JsonNode problem = document().get("components").get("schemas").get("Problem");
		Set<String> properties = new TreeSet<>();
		problem.get("properties").propertyNames().forEach(properties::add);
		assertThat(properties).contains("title", "status", "detail", "code", "errors", "requestId");
	}

}
