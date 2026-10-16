package com.fundigsac.catalog;

import org.junit.jupiter.api.Test;

import com.fundigsac.support.IntegrationTest;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Lectura pública del catálogo sobre los datos iniciales (los mismos que muestra el sitio). */
class CatalogIT extends IntegrationTest {

	@Test
	void listsCategoriesWithPublishedCounts() throws Exception {
		this.mvc.perform(get("/api/v1/catalog/categories").with(randomIp()))
			.andExpect(status().isOk())
			.andExpect(header().string("Cache-Control", "max-age=300, public, stale-while-revalidate=60"))
			.andExpect(jsonPath("$[0].slug").value("valvulas"))
			.andExpect(jsonPath("$[0].productCount").value(greaterThan(10)))
			.andExpect(jsonPath("$[*].slug").value(hasItem("marcos-y-tapas")));
	}

	@Test
	void searchIgnoresAccentsAndPlurals() throws Exception {
		this.mvc.perform(get("/api/v1/catalog/products").param("q", "válvulas compuerta").with(randomIp()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.totalItems").value(greaterThan(3)))
			.andExpect(jsonPath("$.items[*].category.slug").value(everyItem(org.hamcrest.Matchers.is("valvulas"))));
	}

	@Test
	void filtersByTechnicalAttributes() throws Exception {
		this.mvc.perform(get("/api/v1/catalog/products").param("joint", "bridada-f4").param("dn", "150").param("pn", "16")
			.param("homologation", "sedapal")
			.with(randomIp()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.items[*].slug").value(hasItem("vcp-f4-01")))
			.andExpect(jsonPath("$.items[*].joint.code").value(everyItem(org.hamcrest.Matchers.is("bridada-f4"))));
		// Medida de variante (catálogo de los PDF): DN100.
		this.mvc.perform(get("/api/v1/catalog/products").param("category", "acoples").param("dn", "100").with(randomIp()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.totalItems").value(greaterThan(0)));
	}

	@Test
	void rejectsUnknownFilterValues() throws Exception {
		this.mvc.perform(get("/api/v1/catalog/products").param("joint", "soldada").with(randomIp()))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
		this.mvc.perform(get("/api/v1/catalog/products").param("dn", "99999").with(randomIp()))
			.andExpect(status().isBadRequest());
		this.mvc.perform(get("/api/v1/catalog/products").param("size", "1000").with(randomIp()))
			.andExpect(status().isBadRequest());
	}

	@Test
	void returnsProductDetailWithVariantsAndDocuments() throws Exception {
		this.mvc.perform(get("/api/v1/catalog/products/valvula-compuerta-fundigsac").with(randomIp()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value("Válvula de compuerta bridada FUNDIGSAC"))
			.andExpect(jsonPath("$.dataVerified").value(false))
			.andExpect(jsonPath("$.documents[*].slug").value(hasItem("ficha-tecnica-valvula-compuerta-fundigsac")));
		this.mvc.perform(get("/api/v1/catalog/products/acople-gran-rango-agr").with(randomIp()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.variants.length()").value(greaterThan(5)));
		this.mvc.perform(get("/api/v1/catalog/products/no-existe").with(randomIp()))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
	}

	@Test
	void exposesRealFacets() throws Exception {
		this.mvc.perform(get("/api/v1/catalog/filters").param("category", "valvulas").with(randomIp()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.families[*].value").value(hasItem("compuerta")))
			.andExpect(jsonPath("$.homologations[*].value").value(hasItem("sedapal")))
			.andExpect(jsonPath("$.pressureRatings[*].value").value(hasItem("16")))
			.andExpect(jsonPath("$.dn.minMm").exists());
		this.mvc.perform(get("/api/v1/catalog/sectors").with(randomIp()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[*].slug").value(hasItem("saneamiento")));
	}

	@Test
	void downloadsOnlyVerifiedFiles() throws Exception {
		this.mvc.perform(get("/api/v1/resources/ficha-tecnica-valvula-compuerta-fundigsac/download").with(randomIp()))
			.andExpect(status().isFound())
			.andExpect(header().string("Location", startsWith("https://fundigsac-web.vercel.app/docs/")))
			.andExpect(header().string("X-Checksum-SHA256", "0a2c86f4c5234cbd538a50bf9836c62d95de283a3de94f459082953751d99ee9"));
		this.mvc.perform(get("/api/v1/resources/catalogo-general-2026/download").with(randomIp()))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("DOCUMENT_ON_REQUEST"));
		this.mvc.perform(get("/api/v1/resources").param("type", "DATASHEET").with(randomIp()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.items[*].type").value(everyItem(org.hamcrest.Matchers.is("DATASHEET"))));
	}

}
