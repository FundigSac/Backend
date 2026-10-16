package com.fundigsac.catalog.web;

import java.time.Duration;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fundigsac.catalog.application.CatalogQueryService;
import com.fundigsac.catalog.application.CatalogViews.CatalogFacets;
import com.fundigsac.catalog.application.CatalogViews.CategoryView;
import com.fundigsac.catalog.application.CatalogViews.ProductDetailView;
import com.fundigsac.catalog.application.CatalogViews.ProductSummaryView;
import com.fundigsac.catalog.application.CatalogViews.SectorView;
import com.fundigsac.shared.application.PageQuery;
import com.fundigsac.shared.application.PageResult;

/** Catálogo público: familias, búsqueda con filtros técnicos, fichas, facetas y sectores. Respuestas cacheables. */
@RestController
@RequestMapping("/api/v1/catalog")
@Tag(name = "Catálogo", description = "Lectura pública de productos publicados")
class CatalogController {

	private static final CacheControl PUBLIC_CACHE = CacheControl.maxAge(Duration.ofMinutes(5))
		.cachePublic()
		.staleWhileRevalidate(Duration.ofMinutes(1));

	private final CatalogQueryService catalog;

	CatalogController(CatalogQueryService catalog) {
		this.catalog = catalog;
	}

	@GetMapping("/categories")
	@Operation(summary = "Familias con productos publicados")
	ResponseEntity<List<CategoryView>> categories() {
		return ResponseEntity.ok().cacheControl(PUBLIC_CACHE).body(this.catalog.categories());
	}

	@GetMapping("/categories/{slug}")
	@Operation(summary = "Detalle de una familia")
	ResponseEntity<CategoryView> category(@PathVariable @Size(max = 120) String slug) {
		return ResponseEntity.ok().cacheControl(PUBLIC_CACHE).body(this.catalog.category(slug));
	}

	@GetMapping("/products")
	@Operation(summary = "Buscar productos", description = "Filtros combinables. `q` busca sin tildes y por raíz (\"válvulas\" encuentra \"válvula\").")
	ResponseEntity<PageResult<ProductSummaryView>> products(
			@Parameter(description = "Slug de familia, p. ej. valvulas") @RequestParam(required = false) @Size(max = 120) String category,
			@Parameter(description = "Familia técnica, p. ej. compuerta") @RequestParam(required = false) @Size(max = 60) String family,
			@Parameter(description = "Unión: bridada, bridada-f4, tyton, …") @RequestParam(required = false) @Size(max = 20) String joint,
			@Parameter(description = "Diámetro nominal en mm") @RequestParam(required = false) @Min(1) @Max(4000) Integer dn,
			@Parameter(description = "Presión nominal (bar)") @RequestParam(required = false) @Min(1) @Max(100) Integer pn,
			@Parameter(description = "sedapal, otass o wras") @RequestParam(required = false) @Size(max = 20) String homologation,
			@Parameter(description = "Sector, p. ej. saneamiento") @RequestParam(required = false) @Size(max = 120) String sector,
			@Parameter(description = "Texto libre") @RequestParam(required = false) @Size(max = 100) String q,
			@Parameter(description = "relevance, name o newest") @RequestParam(required = false) @Size(max = 12) String sort,
			@RequestParam(required = false) @Min(0) @Max(10_000) Integer page,
			@RequestParam(required = false) @Min(1) @Max(100) Integer size) {
		var filters = new CatalogQueryService.Filters(category, family, joint, dn, pn, homologation, sector, q, sort);
		return ResponseEntity.ok().cacheControl(PUBLIC_CACHE).body(this.catalog.search(filters, PageQuery.of(page, size)));
	}

	@GetMapping("/products/{slug}")
	@Operation(summary = "Ficha de producto publicada", description = "Incluye variantes, especificaciones, sectores y documentos.")
	ResponseEntity<ProductDetailView> product(@PathVariable @Size(max = 120) String slug) {
		return ResponseEntity.ok().cacheControl(PUBLIC_CACHE).body(this.catalog.product(slug));
	}

	@GetMapping("/filters")
	@Operation(summary = "Facetas reales para filtros", description = "Conteos por familia, unión, homologación, PN, sector y rango DN.")
	ResponseEntity<CatalogFacets> filters(@RequestParam(required = false) @Size(max = 120) String category) {
		return ResponseEntity.ok().cacheControl(PUBLIC_CACHE).body(this.catalog.facets(category));
	}

	@GetMapping("/sectors")
	@Operation(summary = "Sectores atendidos con conteo de productos")
	ResponseEntity<List<SectorView>> sectors() {
		return ResponseEntity.ok().cacheControl(PUBLIC_CACHE).body(this.catalog.sectors());
	}

}
