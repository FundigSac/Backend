package com.fundigsac;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.fundigsac.identity.domain.Role;
import com.fundigsac.shared.security.Permission;
import com.fundigsac.support.ApiFixtures;
import com.fundigsac.support.IntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;

/**
 * Autorización operación por operación. Cada operación protegida se prueba sin sesión (401) y con los siete roles: quien
 * tiene el permiso no recibe 401, 403 ni un error 5xx, y quien no lo tiene recibe 403. Si aparece una operación que no
 * está en la tabla, la prueba de inventario falla. Lo esperado se escribe aquí a mano (arquitectura §12.2) y no se deriva
 * de {@link Role}: una escalada de privilegios en ese enum tiene que hacer fallar esta prueba.
 */
class AuthorizationMatrixIT extends IntegrationTest {

	/** Quién puede usar una operación. */
	private enum Audience {

		CUSTOMER, ANY_STAFF, PERMISSION

	}

	private record Operation(String method, String path, Audience audience, @Nullable Permission permission,
			Supplier<@Nullable Object> body) {

		boolean allows(Role role) {
			return switch (this.audience) {
				case CUSTOMER -> role == Role.CUSTOMER;
				case ANY_STAFF -> role.isStaff();
				case PERMISSION -> EXPECTED_PERMISSIONS.get(role).contains(this.permission);
			};
		}

		String url() {
			return this.path.replace("{id}", UUID.randomUUID().toString())
				.replace("{itemId}", UUID.randomUUID().toString())
				.replace("{type}", "b2b-terms")
				.replace("{folio}", "COT-2026-999999");
		}

		String key() {
			return this.method + " " + this.path;
		}

		@Override
		public String toString() {
			return key();
		}

	}

	/** Oráculo independiente del código de producción: qué permisos debe tener cada rol. */
	private static final Map<Role, Set<Permission>> EXPECTED_PERMISSIONS = expectedPermissions();

	private static final String ACCOUNT = "/api/v1/account";

	private static final String CART = "/api/v1/cart";

	private static final String STAFF = "/api/v1/staff";

	private static final UUID SOMEONE = UUID.fromString("00000000-0000-7000-8000-00000000abcd");

	private static final Map<String, Object> ASSIGN = Map.of("assigneeId", SOMEONE.toString());

	private static final Map<String, Object> PROFILE = Map.of("fullName", "Nombre Corregido");

	private static final List<Operation> OPERATIONS = operations();

	private static final Map<Role, String> TOKENS = new EnumMap<>(Role.class);

	@Autowired
	@Qualifier("requestMappingHandlerMapping")
	RequestMappingHandlerMapping mappings;

	private static Map<Role, Set<Permission>> expectedPermissions() {
		Map<Role, Set<Permission>> expected = new EnumMap<>(Role.class);
		expected.put(Role.CUSTOMER, EnumSet.noneOf(Permission.class));
		expected.put(Role.ADMINISTRATOR, EnumSet.of(Permission.AUDIT_READ, Permission.NOTIFICATIONS_MANAGE, Permission.USERS_MANAGE,
				Permission.QUOTE_READ, Permission.QUOTE_MANAGE, Permission.CATALOG_READ, Permission.CATALOG_WRITE,
				Permission.CATALOG_PUBLISH, Permission.COMPLAINT_READ, Permission.COMPLAINT_MANAGE, Permission.INQUIRY_READ,
				Permission.INQUIRY_MANAGE, Permission.PRIVACY_READ, Permission.PRIVACY_MANAGE, Permission.LEGAL_PUBLISH));
		expected.put(Role.SALES, EnumSet.of(Permission.QUOTE_READ, Permission.QUOTE_MANAGE, Permission.INQUIRY_READ,
				Permission.INQUIRY_MANAGE, Permission.CATALOG_READ));
		expected.put(Role.CATALOG_EDITOR, EnumSet.of(Permission.CATALOG_READ, Permission.CATALOG_WRITE));
		expected.put(Role.CATALOG_REVIEWER, EnumSet.of(Permission.CATALOG_READ, Permission.CATALOG_PUBLISH));
		expected.put(Role.LEGAL_SUPPORT, EnumSet.of(Permission.COMPLAINT_READ, Permission.COMPLAINT_MANAGE, Permission.PRIVACY_READ,
				Permission.PRIVACY_MANAGE, Permission.LEGAL_PUBLISH, Permission.INQUIRY_READ));
		expected.put(Role.READ_ONLY, EnumSet.of(Permission.CATALOG_READ, Permission.QUOTE_READ, Permission.INQUIRY_READ,
				Permission.COMPLAINT_READ));
		return expected;
	}

	private static Operation customer(String method, String path, Supplier<@Nullable Object> body) {
		return new Operation(method, path, Audience.CUSTOMER, null, body);
	}

	private static Operation permission(String method, String path, Permission permission, Supplier<@Nullable Object> body) {
		return new Operation(method, path, Audience.PERMISSION, permission, body);
	}

	private static Operation permission(String method, String path, Permission permission) {
		return permission(method, path, permission, () -> null);
	}

	private static Operation permission(String method, String path, Permission permission, Object body) {
		return permission(method, path, permission, () -> body);
	}

	private static List<Operation> operations() {
		List<Operation> ops = new ArrayList<>();
		ops.add(customer("GET", ACCOUNT + "/me", () -> null));
		ops.add(customer("PATCH", ACCOUNT + "/me", () -> PROFILE));
		ops.add(customer("POST", ACCOUNT + "/password", () -> Map.of("currentPassword", "clave-erronea-de-la-matriz", "newPassword", "otra-clave-larga-2026")));
		ops.add(customer("DELETE", ACCOUNT, () -> Map.of("password", "clave-erronea-de-la-matriz")));
		ops.add(customer("GET", ACCOUNT + "/quotes", () -> null));
		ops.add(customer("GET", ACCOUNT + "/quotes/{folio}", () -> null));
		ops.add(customer("GET", CART, () -> null));
		ops.add(customer("POST", CART + "/items", () -> Map.of("productSlug", "vcp-f4-01", "quantity", 1)));
		ops.add(customer("PATCH", CART + "/items/{itemId}", () -> Map.of("quantity", 2)));
		ops.add(customer("DELETE", CART + "/items/{itemId}", () -> null));
		ops.add(customer("DELETE", CART, () -> null));
		ops.add(customer("POST", CART + "/submit", () -> Map.of("privacyNoticeVersion", "2026-10", "acceptPrivacyNotice", true)));

		ops.add(new Operation("GET", STAFF + "/profile", Audience.ANY_STAFF, null, () -> null));
		ops.add(permission("GET", STAFF + "/audit", Permission.AUDIT_READ));
		ops.add(permission("GET", STAFF + "/notifications", Permission.NOTIFICATIONS_MANAGE));
		ops.add(permission("POST", STAFF + "/notifications/{id}/retry", Permission.NOTIFICATIONS_MANAGE));

		ops.add(permission("GET", STAFF + "/users", Permission.USERS_MANAGE));
		ops.add(permission("GET", STAFF + "/users/{id}", Permission.USERS_MANAGE));
		ops.add(permission("POST", STAFF + "/users", Permission.USERS_MANAGE,
				() -> ApiFixtures.invitation("matriz." + ApiFixtures.unique() + "@fundigsac.test", "SALES")));
		ops.add(permission("PUT", STAFF + "/users/{id}/roles", Permission.USERS_MANAGE, Map.of("roles", List.of("SALES"))));
		ops.add(permission("POST", STAFF + "/users/{id}/disable", Permission.USERS_MANAGE));
		ops.add(permission("POST", STAFF + "/users/{id}/enable", Permission.USERS_MANAGE));

		ops.add(permission("GET", STAFF + "/accounts", Permission.USERS_MANAGE));
		ops.add(permission("GET", STAFF + "/accounts/stats", Permission.USERS_MANAGE));
		ops.add(permission("GET", STAFF + "/accounts/activity", Permission.USERS_MANAGE));
		ops.add(permission("GET", STAFF + "/accounts/{id}", Permission.USERS_MANAGE));
		ops.add(permission("PUT", STAFF + "/accounts/{id}/notes", Permission.USERS_MANAGE, Map.of("notes", "Nota de la matriz")));
		ops.add(permission("PATCH", STAFF + "/accounts/{id}", Permission.USERS_MANAGE, PROFILE));
		ops.add(permission("POST", STAFF + "/accounts/{id}/disable", Permission.USERS_MANAGE));
		ops.add(permission("POST", STAFF + "/accounts/{id}/enable", Permission.USERS_MANAGE));
		ops.add(permission("POST", STAFF + "/accounts/{id}/verification-email", Permission.USERS_MANAGE));
		ops.add(permission("POST", STAFF + "/accounts/{id}/password-reset", Permission.USERS_MANAGE));
		ops.add(permission("DELETE", STAFF + "/accounts/{id}", Permission.USERS_MANAGE));

		ops.add(permission("GET", STAFF + "/quotes", Permission.QUOTE_READ));
		ops.add(permission("GET", STAFF + "/quotes/{id}", Permission.QUOTE_READ));
		ops.add(permission("POST", STAFF + "/quotes/{id}/assign", Permission.QUOTE_MANAGE, ASSIGN));
		ops.add(permission("POST", STAFF + "/quotes/{id}/status", Permission.QUOTE_MANAGE, Map.of("status", "IN_REVIEW")));

		String products = STAFF + "/catalog/products";
		ops.add(permission("GET", products, Permission.CATALOG_READ));
		ops.add(permission("GET", products + "/{id}", Permission.CATALOG_READ));
		ops.add(permission("POST", products, Permission.CATALOG_WRITE, () -> ApiFixtures.product("matriz-" + ApiFixtures.unique())));
		ops.add(permission("PUT", products + "/{id}", Permission.CATALOG_WRITE, () -> ApiFixtures.product("matriz-" + ApiFixtures.unique())));
		ops.add(permission("POST", products + "/{id}/submit", Permission.CATALOG_WRITE));
		ops.add(permission("POST", products + "/{id}/publish", Permission.CATALOG_PUBLISH));
		ops.add(permission("POST", products + "/{id}/reject", Permission.CATALOG_PUBLISH, Map.of("note", "Falta la ficha")));
		ops.add(permission("POST", products + "/{id}/archive", Permission.CATALOG_PUBLISH));
		ops.add(permission("POST", products + "/{id}/reopen", Permission.CATALOG_WRITE));

		String documents = STAFF + "/catalog/documents";
		ops.add(permission("GET", documents, Permission.CATALOG_READ));
		ops.add(permission("GET", documents + "/{id}", Permission.CATALOG_READ));
		ops.add(permission("POST", documents, Permission.CATALOG_WRITE, () -> ApiFixtures.document("matriz-doc-" + ApiFixtures.unique())));
		ops.add(permission("PUT", documents + "/{id}", Permission.CATALOG_WRITE, () -> ApiFixtures.document("matriz-doc-" + ApiFixtures.unique())));
		ops.add(permission("POST", documents + "/{id}/publish", Permission.CATALOG_PUBLISH));
		ops.add(permission("POST", documents + "/{id}/withdraw", Permission.CATALOG_PUBLISH));

		String complaints = STAFF + "/complaints";
		ops.add(permission("GET", complaints, Permission.COMPLAINT_READ));
		ops.add(permission("GET", complaints + "/{id}", Permission.COMPLAINT_READ));
		ops.add(permission("POST", complaints + "/{id}/assign", Permission.COMPLAINT_MANAGE, ASSIGN));
		ops.add(permission("POST", complaints + "/{id}/notes", Permission.COMPLAINT_MANAGE, Map.of("note", "Nota de la matriz")));
		ops.add(permission("POST", complaints + "/{id}/answer", Permission.COMPLAINT_MANAGE,
				Map.of("response", "Respuesta suficientemente larga para validarse.")));
		ops.add(permission("POST", complaints + "/{id}/close", Permission.COMPLAINT_MANAGE));

		String inquiries = STAFF + "/inquiries";
		ops.add(permission("GET", inquiries, Permission.INQUIRY_READ));
		ops.add(permission("GET", inquiries + "/{id}", Permission.INQUIRY_READ));
		ops.add(permission("POST", inquiries + "/{id}/assign", Permission.INQUIRY_MANAGE, ASSIGN));
		ops.add(permission("POST", inquiries + "/{id}/status", Permission.INQUIRY_MANAGE, Map.of("status", "IN_PROGRESS")));

		String privacy = STAFF + "/privacy/requests";
		ops.add(permission("GET", privacy, Permission.PRIVACY_READ));
		ops.add(permission("GET", privacy + "/{id}", Permission.PRIVACY_READ));
		ops.add(permission("POST", privacy + "/{id}/start", Permission.PRIVACY_MANAGE));
		ops.add(permission("POST", privacy + "/{id}/conclude", Permission.PRIVACY_MANAGE,
				Map.of("accepted", true, "resolutionNote", "Resolución de la matriz de permisos.")));

		ops.add(permission("POST", STAFF + "/legal/{type}/versions", Permission.LEGAL_PUBLISH,
				() -> ApiFixtures.legalVersion("m-" + ApiFixtures.unique())));
		return List.copyOf(ops);
	}

	static Stream<Operation> everyOperation() {
		return OPERATIONS.stream();
	}

	private String token(Role role) throws Exception {
		String cached = TOKENS.get(role);
		if (cached != null) {
			return cached;
		}
		String token = switch (role) {
			case ADMINISTRATOR -> adminToken();
			case CUSTOMER -> customerToken("matriz.cliente@empresa.pe", "frase-de-paso-muy-larga");
			default -> staffToken("matriz." + role.name().toLowerCase() + "@fundigsac.test", role.name());
		};
		TOKENS.put(role, token);
		return token;
	}

	private MvcResult call(Operation operation, @Nullable String token) throws Exception {
		MockHttpServletRequestBuilder builder = request(HttpMethod.valueOf(operation.method()), operation.url()).with(randomIp())
			.header("If-Match", "\"0\"");
		Object body = operation.body().get();
		if (body != null) {
			builder.contentType(MediaType.APPLICATION_JSON).content(this.json.writeValueAsString(body));
		}
		if (token != null) {
			authed(builder, token);
		}
		return this.mvc.perform(builder).andReturn();
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("everyOperation")
	void anonymousVisitorsAreRejected(Operation operation) throws Exception {
		MvcResult result = call(operation, null);
		assertThat(result.getResponse().getStatus()).isEqualTo(401);
		assertThat(body(result).get("code").asString()).isEqualTo("AUTHENTICATION_REQUIRED");
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("everyOperation")
	void eachRoleGetsExactlyItsPermissions(Operation operation) throws Exception {
		for (Role role : Role.values()) {
			MvcResult result = call(operation, token(role));
			int status = result.getResponse().getStatus();
			if (operation.allows(role)) {
				assertThat(status).as("%s con el rol %s debe estar permitida y sin error interno", operation, role).isNotIn(401, 403).isLessThan(500);
			}
			else {
				assertThat(status).as("%s con el rol %s debe negarse", operation, role).isEqualTo(403);
				assertThat(body(result).get("code").asString()).isEqualTo("FORBIDDEN");
			}
		}
	}

	@Test
	void everyRoleHasExactlyTheDocumentedPermissions() {
		assertThat(EXPECTED_PERMISSIONS).containsOnlyKeys(Role.values());
		for (Role role : Role.values()) {
			assertThat(role.permissions()).as("permisos del rol %s", role).isEqualTo(EXPECTED_PERMISSIONS.get(role));
		}
	}

	@Test
	void theTableListsEveryProtectedOperation() {
		Set<String> mapped = new TreeSet<>();
		this.mappings.getHandlerMethods().keySet().forEach((info) -> info.getPathPatternsCondition().getPatternValues().forEach((path) -> {
			if (path.startsWith(STAFF) || path.startsWith(ACCOUNT) || path.startsWith(CART)) {
				info.getMethodsCondition().getMethods().forEach((method) -> mapped.add(method + " " + normalize(path)));
			}
		}));
		Set<String> listed = new TreeSet<>();
		OPERATIONS.forEach((operation) -> listed.add(operation.key()));
		assertThat(listed).isEqualTo(mapped).hasSize(67);
	}

	private static String normalize(String path) {
		return path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
	}

}
