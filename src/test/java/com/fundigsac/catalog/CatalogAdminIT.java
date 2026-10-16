package com.fundigsac.catalog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fundigsac.support.IntegrationTest;
import tools.jackson.databind.JsonNode;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Flujo editorial con permisos separados y control de cuatro ojos. */
class CatalogAdminIT extends IntegrationTest {

	private static Map<String, Object> draft(boolean verified) {
		Map<String, Object> body = new HashMap<>();
		body.put("categorySlug", "valvulas");
		body.put("slug", verified ? "valvula-ventosa-triple-funcion" : "valvula-sin-verificar");
		body.put("sku", verified ? "VTF-80" : null);
		body.put("name", "Válvula ventosa de triple función");
		body.put("summary", "Admisión y expulsión de aire en redes de conducción.");
		body.put("family", "ventosa");
		body.put("joint", "bridada");
		body.put("dnMinMm", 50);
		body.put("dnMaxMm", 200);
		body.put("pressureRatings", List.of(10, 16));
		body.put("standards", List.of("EN 1074-4"));
		body.put("specs", List.of(Map.of("label", "Material", "value", "Hierro dúctil")));
		body.put("images", List.of(Map.of("url", "/media/catalog/ventosa.webp", "alt", "Ventosa de triple función", "illustrative", true)));
		body.put("sectors", List.of("agricultura"));
		body.put("variants", List.of(Map.of("label", "DN80 · PN16", "dnMm", 80, "pn", 16)));
		body.put("sourceReference", verified ? "Ficha técnica del fabricante, rev. 3" : null);
		body.put("dataVerified", verified);
		return body;
	}

	@Test
	void editorDraftsAndAnotherPersonPublishes() throws Exception {
		String editor = staffToken("editor@fundigsac.test", "palabra-secreta-de-edicion", "CATALOG_EDITOR");
		String reviewer = staffToken("revisor@fundigsac.test", "palabra-secreta-de-revision", "CATALOG_REVIEWER");

		JsonNode created = body(this.mvc.perform(authed(postJson("/api/v1/staff/catalog/products", draft(true)), editor))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.status").value("DRAFT"))
			.andReturn());
		String id = created.get("id").asString();
		// El borrador no es público.
		this.mvc.perform(get("/api/v1/catalog/products/valvula-ventosa-triple-funcion").with(randomIp())).andExpect(status().isNotFound());
		// El revisor no edita y el editor no publica.
		this.mvc.perform(authed(put("/api/v1/staff/catalog/products/" + id).with(randomIp())
			.contentType("application/json").content(this.json.writeValueAsString(draft(true))), reviewer)
			.header("If-Match", "\"0\"")).andExpect(status().isForbidden());

		this.mvc.perform(authed(postJson("/api/v1/staff/catalog/products/" + id + "/submit", Map.of()), editor).header("If-Match", "\"0\""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("IN_REVIEW"));
		this.mvc.perform(authed(postJson("/api/v1/staff/catalog/products/" + id + "/publish", Map.of()), editor).header("If-Match", "\"1\""))
			.andExpect(status().isForbidden());
		this.mvc.perform(authed(postJson("/api/v1/staff/catalog/products/" + id + "/publish", Map.of()), reviewer).header("If-Match", "\"1\""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("PUBLISHED"));

		this.mvc.perform(get("/api/v1/catalog/products/valvula-ventosa-triple-funcion").with(randomIp()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.sectors[0].slug").value("agricultura"))
			.andExpect(jsonPath("$.variants[0].label").value("DN80 · PN16"));
		this.mvc.perform(get("/api/v1/catalog/products").param("q", "ventosa triple").with(randomIp()))
			.andExpect(jsonPath("$.items[0].slug").value("valvula-ventosa-triple-funcion"));
	}

	@Test
	void unverifiedDataCannotBePublishedAndSubmitterCannotSelfApprove() throws Exception {
		String admin = adminToken();
		String id = body(this.mvc.perform(authed(postJson("/api/v1/staff/catalog/products", draft(false)), admin))
			.andExpect(status().isCreated())
			.andReturn()).get("id").asString();
		this.mvc.perform(authed(postJson("/api/v1/staff/catalog/products/" + id + "/submit", Map.of()), admin).header("If-Match", "\"0\""))
			.andExpect(status().isOk());
		this.mvc.perform(authed(postJson("/api/v1/staff/catalog/products/" + id + "/publish", Map.of()), admin).header("If-Match", "\"1\""))
			.andExpect(status().isUnprocessableContent())
			.andExpect(jsonPath("$.code").value("DATA_NOT_VERIFIED"));
		this.mvc.perform(authed(postJson("/api/v1/staff/catalog/products/" + id + "/reject",
				Map.of("note", "Falta la ficha del fabricante")), admin).header("If-Match", "\"1\""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("DRAFT"))
			.andExpect(jsonPath("$.reviewNote").value("Falta la ficha del fabricante"));
	}

}
