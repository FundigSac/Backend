package com.fundigsac.support;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;
import org.springframework.http.MediaType;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Compara una respuesta real con el contrato OpenAPI: el código debe estar documentado para esa operación, el tipo de
 * contenido debe ser el documentado y el cuerpo debe cumplir su esquema. En un objeto cerrado se rechazan los campos no
 * documentados. Un campo opcional puede venir ausente o con null, porque Java no distingue esos casos en el contrato.
 */
public final class OpenApiContract {

	private record Route(String method, String template, Pattern pattern, JsonNode operation) {
	}

	private static final Pattern TEMPLATE_VARIABLE = Pattern.compile("\\{[^/}]+\\}");

	private final JsonNode document;

	private final JsonMapper json;

	private final List<Route> routes = new ArrayList<>();

	public OpenApiContract(JsonNode document, JsonMapper json) {
		this.document = document;
		this.json = json;
		document.get("paths").properties().forEach((path) -> path.getValue().properties().forEach((operation) -> {
			String regex = "^" + TEMPLATE_VARIABLE.matcher(Pattern.quote(path.getKey())).replaceAll("\\\\E[^/]+\\\\Q") + "$";
			this.routes.add(new Route(operation.getKey().toUpperCase(Locale.ROOT), path.getKey(), Pattern.compile(regex), operation.getValue()));
		}));
		// Una ruta literal (/accounts/stats) tiene prioridad sobre una con variable (/accounts/{id}).
		this.routes.sort(Comparator.comparingLong((Route route) -> route.template().chars().filter((c) -> c == '{').count()));
	}

	public int operationCount() {
		return this.routes.size();
	}

	/** @return lista vacía si la respuesta cumple el contrato o si la ruta no pertenece a la API documentada */
	public List<String> violations(String method, String uri, int status, @Nullable String contentType, byte[] body) {
		Route route = this.routes.stream().filter((r) -> r.method().equals(method) && r.pattern().matcher(uri).matches()).findFirst().orElse(null);
		if (route == null) {
			return List.of();
		}
		String label = method + " " + route.template() + " -> " + status;
		JsonNode response = route.operation().path("responses").get(Integer.toString(status));
		if (response == null) {
			return List.of(label + ": código no documentado. Cuerpo: " + new String(body, StandardCharsets.UTF_8));
		}
		JsonNode content = response.get("content");
		if (content == null || body.length == 0) {
			return List.of();
		}
		JsonNode media = mediaFor(content, contentType);
		if (media == null) {
			return List.of(label + ": tipo de contenido no documentado: " + contentType);
		}
		JsonNode schema = media.get("schema");
		if (schema == null) {
			return List.of();
		}
		List<String> problems = new ArrayList<>();
		check(schema, this.json.readTree(body), "$", problems, new HashSet<>());
		return problems.stream().map((problem) -> label + ": " + problem).toList();
	}

	private static @Nullable JsonNode mediaFor(JsonNode content, @Nullable String contentType) {
		MediaType actual = contentType == null ? null : MediaType.parseMediaType(contentType);
		for (var entry : content.properties()) {
			if (actual == null || MediaType.parseMediaType(entry.getKey()).isCompatibleWith(actual)) {
				return entry.getValue();
			}
		}
		return null;
	}

	private JsonNode resolve(JsonNode schema) {
		JsonNode current = schema;
		while (current.has("$ref")) {
			current = this.document.at(current.get("$ref").asString().substring(1));
		}
		return current;
	}

	private void check(JsonNode rawSchema, JsonNode value, String path, List<String> problems, Set<String> seen) {
		JsonNode schema = resolve(rawSchema);
		if (schema.has("allOf")) {
			schema.get("allOf").forEach((part) -> check(part, value, path, problems, seen));
		}
		for (String union : List.of("oneOf", "anyOf")) {
			if (schema.has(union) && !matchesAny(schema.get(union), value, path)) {
				problems.add(path + ": no cumple ninguna alternativa de " + union);
			}
		}
		if (value.isNull()) {
			return;
		}
		checkType(schema, value, path, problems);
		checkFormat(schema, value, path, problems);
		if (schema.has("enum")) {
			boolean listed = false;
			for (JsonNode allowed : schema.get("enum")) {
				listed |= allowed.equals(value);
			}
			if (!listed) {
				problems.add(path + ": valor fuera del enum: " + value);
			}
		}
		if (value.isObject()) {
			checkObject(schema, value, path, problems, seen);
		}
		if (value.isArray() && schema.has("items")) {
			int index = 0;
			for (JsonNode element : value) {
				check(schema.get("items"), element, path + "[" + index++ + "]", problems, seen);
			}
		}
	}

	private boolean matchesAny(JsonNode alternatives, JsonNode value, String path) {
		for (JsonNode alternative : alternatives) {
			List<String> attempt = new ArrayList<>();
			check(alternative, value, path, attempt, new HashSet<>());
			if (attempt.isEmpty()) {
				return true;
			}
		}
		return false;
	}

	private void checkObject(JsonNode schema, JsonNode value, String path, List<String> problems, Set<String> seen) {
		for (JsonNode required : schema.path("required")) {
			JsonNode field = value.get(required.asString());
			if (field == null || field.isNull()) {
				problems.add(path + "." + required.asString() + ": campo obligatorio ausente");
			}
		}
		JsonNode properties = schema.get("properties");
		JsonNode additional = schema.get("additionalProperties");
		value.properties().forEach((field) -> {
			String fieldPath = path + "." + field.getKey();
			JsonNode declared = properties == null ? null : properties.get(field.getKey());
			if (declared != null) {
				check(declared, field.getValue(), fieldPath, problems, seen);
			}
			else if (additional != null && additional.isObject()) {
				check(additional, field.getValue(), fieldPath, problems, seen);
			}
			else if (properties != null && (additional == null || !additional.asBoolean(false))) {
				problems.add(fieldPath + ": campo no documentado");
			}
		});
	}

	private static void checkType(JsonNode schema, JsonNode value, String path, List<String> problems) {
		JsonNode type = schema.get("type");
		if (type == null) {
			return;
		}
		boolean accepted = false;
		if (type.isArray()) {
			for (JsonNode one : type) {
				accepted |= hasType(one.asString(), value);
			}
		}
		else {
			accepted = hasType(type.asString(), value);
		}
		if (!accepted) {
			problems.add(path + ": se esperaba " + type + " y llegó " + value);
		}
	}

	private static boolean hasType(String type, JsonNode value) {
		return switch (type) {
			case "object" -> value.isObject();
			case "array" -> value.isArray();
			case "string" -> value.isString();
			case "integer" -> value.isIntegralNumber();
			case "number" -> value.isNumber();
			case "boolean" -> value.isBoolean();
			case "null" -> value.isNull();
			default -> true;
		};
	}

	private static void checkFormat(JsonNode schema, JsonNode value, String path, List<String> problems) {
		JsonNode format = schema.get("format");
		if (format == null || !value.isString()) {
			return;
		}
		try {
			switch (format.asString()) {
				case "uuid" -> UUID.fromString(value.asString());
				case "date" -> LocalDate.parse(value.asString());
				case "date-time" -> OffsetDateTime.parse(value.asString());
				default -> {
				}
			}
		}
		catch (RuntimeException ex) {
			problems.add(path + ": no cumple el formato " + format.asString() + ": " + value);
		}
	}

}
