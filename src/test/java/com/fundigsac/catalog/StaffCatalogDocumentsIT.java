package com.fundigsac.catalog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import com.fundigsac.support.IntegrationTest;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Documentos técnicos: registro con checksum, edición en borrador, publicación, retiro y visibilidad pública. */
class StaffCatalogDocumentsIT extends IntegrationTest {

	private static final String BASE = "/api/v1/staff/catalog/documents";

	private static final String UNKNOWN = BASE + "/00000000-0000-7000-8000-00000000abcd";

	private static final String SHA = "a".repeat(64);

	private static Map<String, Object> file() {
		Map<String, Object> file = new HashMap<>();
		file.put("url", "/docs/manual-de-prueba.pdf");
		file.put("mediaType", "application/pdf");
		file.put("sizeBytes", 12345);
		file.put("sha256", SHA);
		return file;
	}

	private static Map<String, Object> document(String slug) {
		Map<String, Object> body = new HashMap<>();
		body.put("slug", slug);
		body.put("code", "DOC-" + slug.length());
		body.put("title", "Manual de ensayo " + slug);
		body.put("description", "Documento de prueba de la biblioteca técnica.");
		body.put("type", "MANUAL");
		body.put("families", List.of("compuerta"));
		body.put("availability", "DOWNLOAD");
		body.put("file", file());
		body.put("revision", "A");
		body.put("language", "es");
		body.put("productSlugs", List.of("vcp-f4-01"));
		return body;
	}

	private MvcResult create(String slug, String token) throws Exception {
		return this.mvc.perform(authed(postJson(BASE, document(slug)), token)).andExpect(status().isCreated()).andReturn();
	}

	private String createId(String slug, String token) throws Exception {
		return body(create(slug, token)).get("id").asString();
	}

	private static String etag(MvcResult result) {
		return result.getResponse().getHeader("ETag");
	}

	@Test
	void editorRegistersADraftThatIsNotPublic() throws Exception {
		String editor = staffToken("editor.documentos@fundigsac.test", "CATALOG_EDITOR");
		this.mvc.perform(authed(postJson(BASE, document("manual-borrador-privado")), editor))
			.andExpect(status().isCreated())
			.andExpect(header().string("Location", org.hamcrest.Matchers.containsString(BASE + "/")))
			.andExpect(header().string("ETag", "\"0\""))
			.andExpect(jsonPath("$.status").value("DRAFT"))
			.andExpect(jsonPath("$.productSlugs[0]").value("vcp-f4-01"))
			.andExpect(jsonPath("$.file.sha256").value(SHA));
		this.mvc.perform(get("/api/v1/resources/manual-borrador-privado").with(randomIp())).andExpect(status().isNotFound());
		this.mvc.perform(get("/api/v1/resources/manual-borrador-privado/download").with(randomIp())).andExpect(status().isNotFound());
	}

	@Test
	void creationValidatesContent() throws Exception {
		String admin = adminToken();

		Map<String, Object> withoutFile = document("manual-sin-archivo");
		withoutFile.remove("file");
		this.mvc.perform(authed(postJson(BASE, withoutFile), admin))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[0].field").value("file"))
			.andExpect(jsonPath("$.errors[0].code").value("REQUIRED"));

		Map<String, Object> badChecksum = document("manual-checksum-malo");
		Map<String, Object> shortSha = file();
		shortSha.put("sha256", "abc");
		badChecksum.put("file", shortSha);
		this.mvc.perform(authed(postJson(BASE, badChecksum), admin))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[0].field").value("file.sha256"))
			.andExpect(jsonPath("$.errors[0].code").value("INVALID_FORMAT"));

		Map<String, Object> badUrl = document("manual-url-mala");
		Map<String, Object> plainHttp = file();
		plainHttp.put("url", "http://inseguro.example/manual.pdf");
		badUrl.put("file", plainHttp);
		this.mvc.perform(authed(postJson(BASE, badUrl), admin))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[0].code").value("INVALID_URL"));

		Map<String, Object> badType = document("manual-tipo-malo");
		badType.put("type", "NOVELA");
		this.mvc.perform(authed(postJson(BASE, badType), admin))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[0].code").value("INVALID_VALUE"));

		Map<String, Object> badProduct = document("manual-producto-malo");
		badProduct.put("productSlugs", List.of("producto-inventado"));
		this.mvc.perform(authed(postJson(BASE, badProduct), admin))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[0].code").value("UNKNOWN_PRODUCT"));

		Map<String, Object> massAssignment = document("manual-campo-extra");
		massAssignment.put("status", "PUBLISHED");
		this.mvc.perform(authed(postJson(BASE, massAssignment), admin))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("UNKNOWN_FIELD"));

		create("manual-slug-unico", admin);
		this.mvc.perform(authed(postJson(BASE, document("manual-slug-unico")), admin))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("SLUG_TAKEN"));
	}

	@Test
	void creationRequiresCatalogWritePermission() throws Exception {
		String reviewer = staffToken("revisor.documentos@fundigsac.test", "CATALOG_REVIEWER");
		String customer = customerToken("cliente.documentos@andina.pe", "frase-de-paso-muy-larga");
		this.mvc.perform(authed(postJson(BASE, document("manual-sin-permiso")), reviewer)).andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("FORBIDDEN"));
		this.mvc.perform(authed(postJson(BASE, document("manual-sin-permiso")), customer)).andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("FORBIDDEN"));
		this.mvc.perform(postJson(BASE, document("manual-sin-permiso"))).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
	}

	@Test
	void staffListsAndReadsDocumentsInAnyStatus() throws Exception {
		String admin = adminToken();
		String reviewer = staffToken("lector.documentos@fundigsac.test", "CATALOG_REVIEWER");
		String id = createId("manual-listado-urraca", admin);

		this.mvc.perform(authed(get(BASE).param("q", "urraca").with(randomIp()), reviewer))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.totalItems").value(1))
			.andExpect(jsonPath("$.items[0].status").value("DRAFT"));
		this.mvc.perform(authed(get(BASE).param("size", "1000").with(randomIp()), reviewer)).andExpect(status().isBadRequest());
		this.mvc.perform(getAs(BASE + "/" + id, reviewer))
			.andExpect(status().isOk())
			.andExpect(header().string("ETag", "\"0\""))
			.andExpect(jsonPath("$.slug").value("manual-listado-urraca"));
		this.mvc.perform(getAs(UNKNOWN, reviewer))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("DOCUMENT_NOT_FOUND"));
		this.mvc.perform(getAs(BASE + "/no-es-uuid", reviewer)).andExpect(status().isBadRequest());

		String customer = customerToken("cliente.lectura.docs@andina.pe", "frase-de-paso-muy-larga");
		this.mvc.perform(getAs(BASE, customer)).andExpect(status().isForbidden());
		this.mvc.perform(getAs(BASE + "/" + id, customer)).andExpect(status().isForbidden());
		this.mvc.perform(get(BASE).with(randomIp())).andExpect(status().isUnauthorized());
	}

	@Test
	void draftsAreEditedWithIfMatchAndFrozenOncePublished() throws Exception {
		String admin = adminToken();
		String reviewer = staffToken("revisor.edicion.docs@fundigsac.test", "CATALOG_REVIEWER");
		String id = createId("manual-edicion", admin);
		String url = BASE + "/" + id;
		Map<String, Object> changed = document("manual-edicion");
		changed.put("title", "Manual de ensayo corregido");

		this.mvc.perform(authed(putJson(url, changed), admin)).andExpect(status().isPreconditionRequired());
		this.mvc.perform(authed(putJson(url, changed), admin).header("If-Match", "\"99\"")).andExpect(status().isPreconditionFailed());
		this.mvc.perform(authed(putJson(url, changed), reviewer).header("If-Match", "\"0\"")).andExpect(status().isForbidden());
		this.mvc.perform(putJson(url, changed).header("If-Match", "\"0\"")).andExpect(status().isUnauthorized());
		this.mvc.perform(authed(putJson(UNKNOWN, changed), admin).header("If-Match", "\"0\"")).andExpect(status().isNotFound());
		Map<String, Object> invalid = document("manual-edicion");
		invalid.put("title", "x");
		this.mvc.perform(authed(putJson(url, invalid), admin).header("If-Match", "\"0\""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[0].field").value("title"));

		String edited = etag(this.mvc.perform(authed(putJson(url, changed), admin).header("If-Match", "\"0\""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.title").value("Manual de ensayo corregido"))
			.andReturn());
		String published = etag(this.mvc.perform(authed(postJson(url + "/publish", Map.of()), admin).header("If-Match", edited))
			.andExpect(status().isOk())
			.andReturn());
		this.mvc.perform(authed(putJson(url, changed), admin).header("If-Match", published))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("INVALID_TRANSITION"));
	}

	@Test
	void publishingExposesTheDocumentAndItsChecksum() throws Exception {
		String admin = adminToken();
		String editor = staffToken("editor.publica.docs@fundigsac.test", "CATALOG_EDITOR");
		String reviewer = staffToken("revisor.publica.docs@fundigsac.test", "CATALOG_REVIEWER");
		String id = createId("manual-publicable", editor);
		String url = BASE + "/" + id + "/publish";

		this.mvc.perform(authed(postJson(url, Map.of()), reviewer)).andExpect(status().isPreconditionRequired());
		this.mvc.perform(authed(postJson(url, Map.of()), reviewer).header("If-Match", "\"99\"")).andExpect(status().isPreconditionFailed());
		this.mvc.perform(authed(postJson(url, Map.of()), editor).header("If-Match", "\"0\"")).andExpect(status().isForbidden());
		this.mvc.perform(postJson(url, Map.of()).header("If-Match", "\"0\"")).andExpect(status().isUnauthorized());
		this.mvc.perform(authed(postJson(UNKNOWN + "/publish", Map.of()), reviewer).header("If-Match", "\"0\""))
			.andExpect(status().isNotFound());

		String published = etag(this.mvc.perform(authed(postJson(url, Map.of()), reviewer).header("If-Match", "\"0\""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("PUBLISHED"))
			.andExpect(jsonPath("$.publishedAt").exists())
			.andReturn());
		this.mvc.perform(get("/api/v1/resources/manual-publicable").with(randomIp()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("PUBLISHED"));
		this.mvc.perform(get("/api/v1/resources/manual-publicable/download").with(randomIp()))
			.andExpect(status().isFound())
			.andExpect(header().string("Location", endsWith("/docs/manual-de-prueba.pdf")))
			.andExpect(header().string("X-Checksum-SHA256", SHA));
		this.mvc.perform(get("/api/v1/resources").param("type", "MANUAL").with(randomIp()))
			.andExpect(jsonPath("$.items[*].slug").value(hasItem("manual-publicable")));
		this.mvc.perform(authed(postJson(url, Map.of()), reviewer).header("If-Match", published))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("INVALID_TRANSITION"));
	}

	@Test
	void onRequestDocumentsPublishWithoutFileAndCannotBeDownloaded() throws Exception {
		String admin = adminToken();
		Map<String, Object> onRequest = document("manual-bajo-solicitud");
		onRequest.put("availability", "ON_REQUEST");
		onRequest.remove("file");
		String id = body(this.mvc.perform(authed(postJson(BASE, onRequest), admin)).andExpect(status().isCreated()).andReturn())
			.get("id").asString();
		this.mvc.perform(authed(postJson(BASE + "/" + id + "/publish", Map.of()), admin).header("If-Match", "\"0\""))
			.andExpect(status().isOk());
		this.mvc.perform(get("/api/v1/resources/manual-bajo-solicitud/download").with(randomIp()))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("DOCUMENT_ON_REQUEST"));
	}

	@Test
	void withdrawingStopsPublicAccess() throws Exception {
		String admin = adminToken();
		String editor = staffToken("editor.retiro.docs@fundigsac.test", "CATALOG_EDITOR");
		String id = createId("manual-retirable", admin);
		String url = BASE + "/" + id;

		this.mvc.perform(authed(postJson(url + "/withdraw", Map.of()), admin).header("If-Match", "\"0\""))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("INVALID_TRANSITION"));
		String published = etag(this.mvc.perform(authed(postJson(url + "/publish", Map.of()), admin).header("If-Match", "\"0\""))
			.andExpect(status().isOk())
			.andReturn());

		this.mvc.perform(authed(postJson(url + "/withdraw", Map.of()), admin)).andExpect(status().isPreconditionRequired());
		this.mvc.perform(authed(postJson(url + "/withdraw", Map.of()), admin).header("If-Match", "\"99\""))
			.andExpect(status().isPreconditionFailed());
		this.mvc.perform(authed(postJson(url + "/withdraw", Map.of()), editor).header("If-Match", published))
			.andExpect(status().isForbidden());
		this.mvc.perform(postJson(url + "/withdraw", Map.of()).header("If-Match", published)).andExpect(status().isUnauthorized());
		this.mvc.perform(authed(postJson(UNKNOWN + "/withdraw", Map.of()), admin).header("If-Match", "\"0\""))
			.andExpect(status().isNotFound());

		String withdrawn = etag(this.mvc.perform(authed(postJson(url + "/withdraw", Map.of()), admin).header("If-Match", published))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("WITHDRAWN"))
			.andReturn());
		this.mvc.perform(get("/api/v1/resources/manual-retirable").with(randomIp())).andExpect(status().isNotFound());
		this.mvc.perform(get("/api/v1/resources/manual-retirable/download").with(randomIp())).andExpect(status().isNotFound());
		this.mvc.perform(authed(postJson(url + "/withdraw", Map.of()), admin).header("If-Match", withdrawn))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("INVALID_TRANSITION"));
	}

}
