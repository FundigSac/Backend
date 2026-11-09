package com.fundigsac.catalog;

import org.junit.jupiter.api.Test;

import com.fundigsac.support.IntegrationTest;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Detalle público de una familia y de un documento técnico. */
class PublicCatalogDetailIT extends IntegrationTest {

	@Test
	void categoryDetailShowsItsPublishedProducts() throws Exception {
		this.mvc.perform(get("/api/v1/catalog/categories/valvulas").with(randomIp()))
			.andExpect(status().isOk())
			.andExpect(header().string("Cache-Control", containsString("max-age=300")))
			.andExpect(jsonPath("$.slug").value("valvulas"))
			.andExpect(jsonPath("$.name").exists())
			.andExpect(jsonPath("$.productCount").value(greaterThan(10)));
	}

	@Test
	void unknownCategoryIsNotFoundAndOversizedSlugIsInvalid() throws Exception {
		this.mvc.perform(get("/api/v1/catalog/categories/no-existe").with(randomIp()))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("CATEGORY_NOT_FOUND"));
		this.mvc.perform(get("/api/v1/catalog/categories/" + "x".repeat(121)).with(randomIp()))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
	}

	@Test
	void resourceDetailExposesTheVerifiedFile() throws Exception {
		this.mvc.perform(get("/api/v1/resources/ficha-tecnica-valvula-compuerta-fundigsac").with(randomIp()))
			.andExpect(status().isOk())
			.andExpect(header().string("Cache-Control", containsString("max-age=300")))
			.andExpect(jsonPath("$.slug").value("ficha-tecnica-valvula-compuerta-fundigsac"))
			.andExpect(jsonPath("$.status").value("PUBLISHED"))
			.andExpect(jsonPath("$.availability").value("DOWNLOAD"))
			.andExpect(jsonPath("$.file.sha256").value("0a2c86f4c5234cbd538a50bf9836c62d95de283a3de94f459082953751d99ee9"));
	}

	@Test
	void onRequestResourceHasNoFile() throws Exception {
		this.mvc.perform(get("/api/v1/resources/catalogo-general-2026").with(randomIp()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.availability").value("ON_REQUEST"))
			.andExpect(jsonPath("$.file").isEmpty());
	}

	@Test
	void unknownResourceIsNotFoundAndOversizedSlugIsInvalid() throws Exception {
		this.mvc.perform(get("/api/v1/resources/no-existe").with(randomIp()))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("DOCUMENT_NOT_FOUND"));
		this.mvc.perform(get("/api/v1/resources/" + "x".repeat(121)).with(randomIp()))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
	}

}
