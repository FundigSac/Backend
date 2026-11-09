package com.fundigsac.catalog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import com.fundigsac.support.IntegrationTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Bandeja editorial de fichas: listado, detalle, archivo y reapertura con If-Match y permisos separados. */
class StaffCatalogProductsIT extends IntegrationTest {

	private static final String BASE = "/api/v1/staff/catalog/products";

	private static final String UNKNOWN = BASE + "/00000000-0000-7000-8000-00000000abcd";

	private static Map<String, Object> draft(String slug, String sku, String name) {
		Map<String, Object> body = new HashMap<>();
		body.put("categorySlug", "valvulas");
		body.put("slug", slug);
		body.put("sku", sku);
		body.put("name", name);
		body.put("summary", "Ficha creada para comprobar la gestión editorial del catálogo.");
		body.put("family", "ventosa");
		body.put("joint", "bridada");
		body.put("dnMinMm", 50);
		body.put("dnMaxMm", 200);
		body.put("pressureRatings", List.of(10, 16));
		body.put("standards", List.of("EN 1074-4"));
		body.put("specs", List.of(Map.of("label", "Material", "value", "Hierro dúctil")));
		body.put("images", List.of(Map.of("url", "/media/catalog/ventosa.webp", "alt", "Ventosa de prueba", "illustrative", true)));
		body.put("sectors", List.of("agricultura"));
		body.put("variants", List.of(Map.of("label", "DN80 · PN16", "dnMm", 80, "pn", 16)));
		body.put("sourceReference", "Ficha técnica del fabricante, rev. 3");
		body.put("dataVerified", true);
		return body;
	}

	private String create(String slug, String sku, String name, String token) throws Exception {
		return body(this.mvc.perform(authed(postJson(BASE, draft(slug, sku, name)), token))
			.andExpect(status().isCreated())
			.andReturn()).get("id").asString();
	}

	private static String etag(MvcResult result) {
		return result.getResponse().getHeader("ETag");
	}

	@Test
	void staffListsEveryStatusAndFilters() throws Exception {
		String admin = adminToken();
		String reviewer = staffToken("revisor.listado@fundigsac.test", "CATALOG_REVIEWER");
		create("valvula-zorzal-listado", "ZOR-80", "Válvula zorzal de listado", admin);

		this.mvc.perform(authed(get(BASE).param("status", "DRAFT").param("q", "zorzal").with(randomIp()), reviewer))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.totalItems").value(1))
			.andExpect(jsonPath("$.items[0].slug").value("valvula-zorzal-listado"));
		this.mvc.perform(authed(get(BASE).param("status", "PUBLISHED").param("q", "zorzal").with(randomIp()), reviewer))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.totalItems").value(0));
		this.mvc.perform(authed(get(BASE).param("status", "OTRO").with(randomIp()), reviewer))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[0].code").value("INVALID_STATUS"));
		this.mvc.perform(authed(get(BASE).param("size", "1000").with(randomIp()), reviewer)).andExpect(status().isBadRequest());
	}

	@Test
	void listAndDetailRequireCatalogPermission() throws Exception {
		String customer = customerToken("cliente.fichas@andina.pe", "frase-de-paso-muy-larga");
		this.mvc.perform(getAs(BASE, customer)).andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("FORBIDDEN"));
		this.mvc.perform(getAs(UNKNOWN, customer)).andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("FORBIDDEN"));
		this.mvc.perform(get(BASE).with(randomIp())).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
		this.mvc.perform(get(UNKNOWN).with(randomIp())).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
	}

	@Test
	void detailReturnsVersionAsEtagAndAnswersNotFound() throws Exception {
		String admin = adminToken();
		String id = create("valvula-detalle-personal", "DET-80", "Válvula de detalle interno", admin);

		this.mvc.perform(getAs(BASE + "/" + id, admin))
			.andExpect(status().isOk())
			.andExpect(header().string("ETag", "\"0\""))
			.andExpect(jsonPath("$.status").value("DRAFT"))
			.andExpect(jsonPath("$.slug").value("valvula-detalle-personal"));
		this.mvc.perform(getAs(UNKNOWN, admin))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
		this.mvc.perform(getAs(BASE + "/no-es-uuid", admin)).andExpect(status().isBadRequest());
	}

	@Test
	void archiveRemovesThePublishedProductFromTheSite() throws Exception {
		String admin = adminToken();
		String reviewer = staffToken("revisor.archivo@fundigsac.test", "CATALOG_REVIEWER");
		String editor = staffToken("editor.archivo@fundigsac.test", "CATALOG_EDITOR");
		String id = create("valvula-para-archivar", "ARC-80", "Válvula para archivar", admin);
		String url = BASE + "/" + id;

		String submitted = etag(this.mvc.perform(authed(postJson(url + "/submit", Map.of()), admin).header("If-Match", "\"0\""))
			.andExpect(status().isOk())
			.andReturn());
		String published = etag(this.mvc.perform(authed(postJson(url + "/publish", Map.of()), reviewer).header("If-Match", submitted))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("PUBLISHED"))
			.andReturn());
		this.mvc.perform(get("/api/v1/catalog/products/valvula-para-archivar").with(randomIp())).andExpect(status().isOk());

		this.mvc.perform(authed(postJson(url + "/archive", Map.of()), reviewer))
			.andExpect(status().isPreconditionRequired())
			.andExpect(jsonPath("$.code").value("IF_MATCH_REQUIRED"));
		this.mvc.perform(authed(postJson(url + "/archive", Map.of()), reviewer).header("If-Match", "\"99\""))
			.andExpect(status().isPreconditionFailed());
		this.mvc.perform(authed(postJson(url + "/archive", Map.of()), reviewer).header("If-Match", "abc"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[0].code").value("INVALID_ETAG"));
		// Editar no da permiso para retirar fichas del sitio.
		this.mvc.perform(authed(postJson(url + "/archive", Map.of()), editor).header("If-Match", published))
			.andExpect(status().isForbidden());
		this.mvc.perform(postJson(url + "/archive", Map.of()).header("If-Match", published)).andExpect(status().isUnauthorized());
		this.mvc.perform(authed(postJson(UNKNOWN + "/archive", Map.of()), reviewer).header("If-Match", "\"0\""))
			.andExpect(status().isNotFound());

		String archived = etag(this.mvc.perform(authed(postJson(url + "/archive", Map.of()), reviewer).header("If-Match", published))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("ARCHIVED"))
			.andReturn());
		this.mvc.perform(get("/api/v1/catalog/products/valvula-para-archivar").with(randomIp())).andExpect(status().isNotFound());
		this.mvc.perform(authed(postJson(url + "/archive", Map.of()), reviewer).header("If-Match", archived))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("INVALID_TRANSITION"));
	}

	@Test
	void reopenReturnsAnArchivedProductToDraftForEditing() throws Exception {
		String admin = adminToken();
		String reviewer = staffToken("revisor.reabrir@fundigsac.test", "CATALOG_REVIEWER");
		String editor = staffToken("editor.reabrir@fundigsac.test", "CATALOG_EDITOR");
		String id = create("valvula-para-reabrir", "REA-80", "Válvula para reabrir", admin);
		String url = BASE + "/" + id;

		this.mvc.perform(authed(postJson(url + "/reopen", Map.of()), editor).header("If-Match", "\"0\""))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("INVALID_TRANSITION"));
		String archived = etag(this.mvc.perform(authed(postJson(url + "/archive", Map.of()), admin).header("If-Match", "\"0\""))
			.andExpect(status().isOk())
			.andReturn());

		this.mvc.perform(authed(postJson(url + "/reopen", Map.of()), editor)).andExpect(status().isPreconditionRequired());
		this.mvc.perform(authed(postJson(url + "/reopen", Map.of()), editor).header("If-Match", "\"99\""))
			.andExpect(status().isPreconditionFailed());
		this.mvc.perform(authed(postJson(url + "/reopen", Map.of()), reviewer).header("If-Match", archived))
			.andExpect(status().isForbidden());
		this.mvc.perform(postJson(url + "/reopen", Map.of()).header("If-Match", archived)).andExpect(status().isUnauthorized());
		this.mvc.perform(authed(postJson(UNKNOWN + "/reopen", Map.of()), editor).header("If-Match", "\"0\""))
			.andExpect(status().isNotFound());

		String reopened = etag(this.mvc.perform(authed(postJson(url + "/reopen", Map.of()), editor).header("If-Match", archived))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("DRAFT"))
			.andReturn());
		this.mvc.perform(authed(putJson(url, draft("valvula-para-reabrir", "REA-80", "Válvula reabierta y editada")), editor)
			.header("If-Match", reopened))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value("Válvula reabierta y editada"));
		this.mvc.perform(getAs(BASE + "/" + id, editor))
			.andExpect(jsonPath("$.status").value("DRAFT"))
			.andExpect(jsonPath("$.name").value("Válvula reabierta y editada"));
	}

}
