package com.fundigsac;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.fundigsac.support.ApiFixtures;
import com.fundigsac.support.IntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contrato de If-Match: las 26 modificaciones que lo exigen responden 428 si falta y 412 si la versión no coincide, sin
 * cambiar el registro. Las modificaciones sin If-Match son solo las que documenta el README.
 */
class OptimisticLockingContractIT extends IntegrationTest {

	private enum Kind {

		PRODUCT("/api/v1/staff/catalog/products/"), DOCUMENT("/api/v1/staff/catalog/documents/"), COMPLAINT("/api/v1/staff/complaints/"),
		PRIVACY("/api/v1/staff/privacy/requests/"), INQUIRY("/api/v1/staff/inquiries/"), QUOTE("/api/v1/staff/quotes/"),
		ACCOUNT("/api/v1/staff/accounts/"), USER("/api/v1/staff/users/");

		private final String base;

		Kind(String base) {
			this.base = base;
		}

	}

	private record Operation(String method, Kind kind, String suffix, @Nullable Object body) {

		String template() {
			return this.kind.base + "{id}" + this.suffix;
		}

		@Override
		public String toString() {
			return this.method + " " + template();
		}

	}

	private static final String STAFF = "/api/v1/staff";

	private static final Map<String, Object> PROFILE = Map.of("fullName", "Nombre Corregido");

	/** Las modificaciones del personal que no usan If-Match, documentadas en el README con su motivo. */
	private static final Set<String> WITHOUT_IF_MATCH = Set.of("POST " + STAFF + "/users", "POST " + STAFF + "/catalog/products",
			"POST " + STAFF + "/catalog/documents", "POST " + STAFF + "/legal/{type}/versions", "PUT " + STAFF + "/accounts/{id}/notes",
			"POST " + STAFF + "/notifications/{id}/retry", "POST " + STAFF + "/accounts/{id}/verification-email",
			"POST " + STAFF + "/accounts/{id}/password-reset");

	@Autowired
	@Qualifier("requestMappingHandlerMapping")
	RequestMappingHandlerMapping mappings;

	static Stream<Operation> everyOperation() {
		Object assign = Map.of("assigneeId", "00000000-0000-7000-8000-00000000abcd");
		return Stream.of(new Operation("PUT", Kind.PRODUCT, "", null), new Operation("POST", Kind.PRODUCT, "/submit", null),
				new Operation("POST", Kind.PRODUCT, "/publish", null),
				new Operation("POST", Kind.PRODUCT, "/reject", Map.of("note", "Falta la ficha")),
				new Operation("POST", Kind.PRODUCT, "/archive", null), new Operation("POST", Kind.PRODUCT, "/reopen", null),
				new Operation("PUT", Kind.DOCUMENT, "", null), new Operation("POST", Kind.DOCUMENT, "/publish", null),
				new Operation("POST", Kind.DOCUMENT, "/withdraw", null), new Operation("POST", Kind.COMPLAINT, "/assign", assign),
				new Operation("POST", Kind.COMPLAINT, "/notes", Map.of("note", "Nota de prueba")),
				new Operation("POST", Kind.COMPLAINT, "/answer", Map.of("response", "Respuesta suficientemente larga para validarse.")),
				new Operation("POST", Kind.COMPLAINT, "/close", null), new Operation("POST", Kind.PRIVACY, "/start", null),
				new Operation("POST", Kind.PRIVACY, "/conclude", Map.of("accepted", true, "resolutionNote", "Resolución de la prueba de versión.")),
				new Operation("POST", Kind.INQUIRY, "/assign", assign),
				new Operation("POST", Kind.INQUIRY, "/status", Map.of("status", "IN_PROGRESS")), new Operation("POST", Kind.QUOTE, "/assign", assign),
				new Operation("POST", Kind.QUOTE, "/status", Map.of("status", "IN_REVIEW")), new Operation("PATCH", Kind.ACCOUNT, "", PROFILE),
				new Operation("POST", Kind.ACCOUNT, "/disable", null), new Operation("POST", Kind.ACCOUNT, "/enable", null),
				new Operation("DELETE", Kind.ACCOUNT, "", null), new Operation("PUT", Kind.USER, "/roles", Map.of("roles", List.of("SALES"))),
				new Operation("POST", Kind.USER, "/disable", null), new Operation("POST", Kind.USER, "/enable", null));
	}

	private String idFromFolio(String table, String folio) {
		return this.jdbc.sql("SELECT CAST(id AS text) FROM " + table + " WHERE folio = :f").param("f", folio).query(String.class).single();
	}

	private String folioOf(String url, Object payload) throws Exception {
		return body(this.mvc.perform(postJson(url, payload)).andExpect(status().isCreated()).andReturn()).get("folio").asString();
	}

	private String create(Kind kind, String admin) throws Exception {
		long unique = ApiFixtures.unique();
		return switch (kind) {
			case PRODUCT -> body(this.mvc.perform(authed(postJson(STAFF + "/catalog/products", ApiFixtures.product("version-" + unique)), admin))
				.andExpect(status().isCreated())
				.andReturn()).get("id").asString();
			case DOCUMENT -> body(this.mvc.perform(authed(postJson(STAFF + "/catalog/documents", ApiFixtures.document("version-doc-" + unique)), admin))
				.andExpect(status().isCreated())
				.andReturn()).get("id").asString();
			case COMPLAINT -> idFromFolio("complaints", folioOf("/api/v1/complaints", ApiFixtures.claim()));
			case PRIVACY -> idFromFolio("data_subject_requests", folioOf("/api/v1/privacy/requests", ApiFixtures.rights("version" + unique + "@correo.pe")));
			case INQUIRY -> idFromFolio("inquiries", folioOf("/api/v1/inquiries", ApiFixtures.inquiry("version" + unique + "@correo.pe")));
			case QUOTE -> idFromFolio("quote_requests", folioOf("/api/v1/quotes", ApiFixtures.quote()));
			case ACCOUNT -> {
				String email = "version" + unique + "@empresa.pe";
				customerToken(email, "frase-de-paso-muy-larga");
				yield userId(email);
			}
			case USER -> body(this.mvc.perform(authed(postJson(STAFF + "/users", ApiFixtures.invitation("version" + unique + "@fundigsac.test", "SALES")), admin))
				.andExpect(status().isCreated())
				.andReturn()).get("id").asString();
		};
	}

	private @Nullable Object bodyFor(Operation operation, String admin) throws Exception {
		if (operation.body() != null || !"PUT".equals(operation.method())) {
			return operation.body() == null ? null : adminAsAssignee(operation.body(), admin);
		}
		long unique = ApiFixtures.unique();
		return operation.kind() == Kind.PRODUCT ? ApiFixtures.product("version-put-" + unique)
				: ApiFixtures.document("version-put-" + unique);
	}

	/** Asignar exige una persona con permiso: la prueba usa al administrador para llegar a la comprobación de versión. */
	private Object adminAsAssignee(Object body, String admin) {
		if (body instanceof Map<?, ?> map && map.containsKey("assigneeId")) {
			return Map.of("assigneeId", userId(ADMIN_EMAIL));
		}
		return body;
	}

	private MockHttpServletRequestBuilder call(Operation operation, String id, String admin, @Nullable Object body) {
		MockHttpServletRequestBuilder builder = request(HttpMethod.valueOf(operation.method()), operation.template().replace("{id}", id))
			.with(randomIp());
		if (body != null) {
			builder.contentType(MediaType.APPLICATION_JSON).content(this.json.writeValueAsString(body));
		}
		return authed(builder, admin);
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("everyOperation")
	void requiresAMatchingVersionAndLeavesTheRecordUntouched(Operation operation) throws Exception {
		String admin = adminToken();
		String id = create(operation.kind(), admin);
		Object payload = bodyFor(operation, admin);
		String detail = operation.kind().base + id;
		String before = etagOf(detail, admin);

		this.mvc.perform(call(operation, id, admin, payload))
			.andExpect(status().isPreconditionRequired())
			.andExpect(jsonPath("$.code").value("IF_MATCH_REQUIRED"));
		this.mvc.perform(call(operation, id, admin, payload).header("If-Match", "\"999\""))
			.andExpect(status().isPreconditionFailed())
			.andExpect(jsonPath("$.code").value("STALE_VERSION"));

		assertThat(etagOf(detail, admin)).isEqualTo(before);
	}

	@Test
	void everyStaffModificationEitherRequiresIfMatchOrIsDocumentedAsAnException() {
		Set<String> withIfMatch = new TreeSet<>();
		Set<String> without = new TreeSet<>();
		this.mappings.getHandlerMethods().forEach((RequestMappingInfo info, HandlerMethod handler) -> {
			for (String path : info.getPathPatternsCondition().getPatternValues()) {
				for (RequestMethod method : info.getMethodsCondition().getMethods()) {
					if (path.startsWith(STAFF) && method != RequestMethod.GET) {
						(requiresIfMatch(handler) ? withIfMatch : without).add(method + " " + path);
					}
				}
			}
		});
		Set<String> listed = new TreeSet<>();
		everyOperation().forEach((operation) -> listed.add(operation.method() + " " + operation.template()));

		assertThat(withIfMatch).isEqualTo(listed).hasSize(26);
		assertThat(without).isEqualTo(new TreeSet<>(WITHOUT_IF_MATCH));
	}

	private static boolean requiresIfMatch(HandlerMethod handler) {
		for (MethodParameter parameter : handler.getMethodParameters()) {
			RequestHeader header = parameter.getParameterAnnotation(RequestHeader.class);
			if (header != null && "If-Match".equals(header.name())) {
				return true;
			}
		}
		return false;
	}

}
