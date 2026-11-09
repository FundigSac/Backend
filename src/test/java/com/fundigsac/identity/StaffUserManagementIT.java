package com.fundigsac.identity;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fundigsac.support.IntegrationTest;

import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Administración del personal: ficha, cambio de roles y reactivación, con If-Match y reglas de autoprotección. */
class StaffUserManagementIT extends IntegrationTest {

	@Test
	void theListValidatesPagingLikeTheOtherInboxes() throws Exception {
		String admin = adminToken();
		for (var invalid : java.util.List.of(Map.of("size", "1000"), Map.of("size", "0"), Map.of("page", "-1"))) {
			var entry = invalid.entrySet().iterator().next();
			this.mvc.perform(authed(get("/api/v1/staff/users").param(entry.getKey(), entry.getValue()).with(randomIp()), admin))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
		}
		this.mvc.perform(authed(get("/api/v1/staff/users").param("size", "100").param("page", "0").with(randomIp()), admin)).andExpect(status().isOk());
	}

	@Test
	void theInvitationEmailOnlyLinksToTheCodeLoginAndCarriesNoToken() throws Exception {
		String email = "invitacion.sin.token@fundigsac.test";
		this.mvc.perform(authed(postJson("/api/v1/staff/users", Map.of("email", email, "fullName", "Invitada Sin Token", "roles", java.util.List.of("SALES"))), adminToken()))
			.andExpect(status().isCreated());
		String link = tokenFromOutboxBody("STAFF_INVITATION", email, java.util.regex.Pattern.compile("(https?://\\S+)"));
		org.assertj.core.api.Assertions.assertThat(link).endsWith("/login").doesNotContain("token");
	}

	private static final String BASE = "/api/v1/staff/users";

	private static final String UNKNOWN = BASE + "/00000000-0000-7000-8000-00000000abcd";

	@Test
	void userDetailCarriesTheVersionAndOnlyShowsStaff() throws Exception {
		String email = "ficha.personal@fundigsac.test";
		String sales = staffToken(email, "SALES");
		String customer = customerToken("cliente.ficha.personal@andina.pe", "frase-de-paso-muy-larga");
		String admin = adminToken();
		String id = userId(email);

		this.mvc.perform(getAs(BASE + "/" + id, admin))
			.andExpect(status().isOk())
			.andExpect(header().exists("ETag"))
			.andExpect(jsonPath("$.email").value(email))
			.andExpect(jsonPath("$.roles[0]").value("SALES"))
			.andExpect(jsonPath("$.kind").value("STAFF"));
		this.mvc.perform(getAs(UNKNOWN, admin))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
		// Una cuenta de cliente no existe en el directorio del personal.
		this.mvc.perform(getAs(BASE + "/" + userId("cliente.ficha.personal@andina.pe"), admin)).andExpect(status().isNotFound());
		this.mvc.perform(getAs(BASE + "/no-es-uuid", admin)).andExpect(status().isBadRequest());
		this.mvc.perform(getAs(BASE + "/" + id, sales)).andExpect(status().isForbidden());
		this.mvc.perform(getAs(BASE + "/" + id, customer)).andExpect(status().isForbidden());
		this.mvc.perform(get(BASE + "/" + id).with(randomIp())).andExpect(status().isUnauthorized());
	}

	@Test
	void changingRolesRevokesTheCurrentTokens() throws Exception {
		String email = "roles.objetivo@fundigsac.test";
		String target = staffToken(email, "SALES");
		String admin = adminToken();
		String id = userId(email);
		String url = BASE + "/" + id + "/roles";
		String etag = etagOf(BASE + "/" + id, admin);
		Map<String, Object> editor = Map.of("roles", List.of("CATALOG_EDITOR"));

		this.mvc.perform(authed(putJson(url, editor), admin))
			.andExpect(status().isPreconditionRequired())
			.andExpect(jsonPath("$.code").value("IF_MATCH_REQUIRED"));
		this.mvc.perform(authed(putJson(url, editor), admin).header("If-Match", "\"99\"")).andExpect(status().isPreconditionFailed());
		this.mvc.perform(authed(putJson(url, editor), target).header("If-Match", etag)).andExpect(status().isForbidden());
		this.mvc.perform(putJson(url, editor).header("If-Match", etag)).andExpect(status().isUnauthorized());
		this.mvc.perform(authed(putJson(UNKNOWN + "/roles", editor), admin).header("If-Match", "\"0\"")).andExpect(status().isNotFound());

		this.mvc.perform(authed(putJson(url, Map.of("roles", List.of())), admin).header("If-Match", etag))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[0].code").value("REQUIRED"));
		this.mvc.perform(authed(putJson(url, Map.of("roles", List.of("JEFE"))), admin).header("If-Match", etag))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[0].code").value("INVALID_VALUE"));
		this.mvc.perform(authed(putJson(url, Map.of("roles", List.of("CUSTOMER"))), admin).header("If-Match", etag))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[0].code").value("INVALID_ROLE"));

		this.mvc.perform(authed(putJson(url, editor), admin).header("If-Match", etag))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.roles").value(contains("CATALOG_EDITOR")));
		// El sello de seguridad invalida en el acto el JWT con los roles anteriores.
		this.mvc.perform(getAs("/api/v1/staff/profile", target)).andExpect(status().isUnauthorized());
	}

	@Test
	void administratorsCannotDemoteThemselves() throws Exception {
		String admin = adminToken();
		String id = userId(ADMIN_EMAIL);
		this.mvc.perform(authed(putJson(BASE + "/" + id + "/roles", Map.of("roles", List.of("SALES"))), admin)
			.header("If-Match", etagOf(BASE + "/" + id, admin)))
			.andExpect(status().isUnprocessableContent())
			.andExpect(jsonPath("$.code").value("CANNOT_DEMOTE_SELF"));
	}

	@Test
	void disabledStaffIsReactivatedWithIfMatch() throws Exception {
		String email = "reactivar.personal@fundigsac.test";
		String target = staffToken(email, "SALES");
		String admin = adminToken();
		String id = userId(email);
		String url = BASE + "/" + id + "/enable";

		this.mvc.perform(authed(postJson(url, Map.of()), admin).header("If-Match", etagOf(BASE + "/" + id, admin)))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("NOT_DISABLED"));

		this.mvc.perform(authed(postJson(BASE + "/" + id + "/disable", Map.of()), admin).header("If-Match", etagOf(BASE + "/" + id, admin)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("DISABLED"));
		String etag = etagOf(BASE + "/" + id, admin);

		this.mvc.perform(authed(postJson(url, Map.of()), admin))
			.andExpect(status().isPreconditionRequired());
		this.mvc.perform(authed(postJson(url, Map.of()), admin).header("If-Match", "\"99\"")).andExpect(status().isPreconditionFailed());
		String sales = staffToken("ventas.reactivar@fundigsac.test", "SALES");
		this.mvc.perform(authed(postJson(url, Map.of()), sales).header("If-Match", etag)).andExpect(status().isForbidden());
		this.mvc.perform(postJson(url, Map.of()).header("If-Match", etag)).andExpect(status().isUnauthorized());
		this.mvc.perform(authed(postJson(UNKNOWN + "/enable", Map.of()), admin).header("If-Match", "\"0\"")).andExpect(status().isNotFound());

		this.mvc.perform(authed(postJson(url, Map.of()), admin).header("If-Match", etag))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("ACTIVE"));
		// Reactivar no resucita los tokens anteriores a la desactivación.
		this.mvc.perform(getAs("/api/v1/staff/profile", target)).andExpect(status().isUnauthorized());
	}

}
