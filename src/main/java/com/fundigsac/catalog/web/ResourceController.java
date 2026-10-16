package com.fundigsac.catalog.web;

import java.net.URI;
import java.time.Duration;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fundigsac.catalog.application.CatalogQueryService;
import com.fundigsac.catalog.application.CatalogViews.DocumentView;
import com.fundigsac.shared.application.PageQuery;
import com.fundigsac.shared.application.PageResult;

/** Biblioteca técnica pública: documentos publicados y descarga de la versión vigente. */
@RestController
@RequestMapping("/api/v1/resources")
@Tag(name = "Recursos técnicos", description = "Fichas, catálogos, manuales y planos publicados")
class ResourceController {

	private static final CacheControl PUBLIC_CACHE = CacheControl.maxAge(Duration.ofMinutes(5)).cachePublic();

	private final CatalogQueryService catalog;

	private final CatalogWebProperties properties;

	ResourceController(CatalogQueryService catalog, CatalogWebProperties properties) {
		this.catalog = catalog;
		this.properties = properties;
	}

	@GetMapping
	@Operation(summary = "Listar documentos publicados", description = "type: CATALOG, DATASHEET, MANUAL, DRAWING, CERTIFICATE u OTHER.")
	ResponseEntity<PageResult<DocumentView>> list(@RequestParam(required = false) @Size(max = 20) String type,
			@RequestParam(required = false) @Size(max = 120) String family,
			@RequestParam(required = false) @Size(max = 120) String product,
			@RequestParam(required = false) @Size(max = 100) String q,
			@RequestParam(required = false) @Min(0) @Max(10_000) Integer page,
			@RequestParam(required = false) @Min(1) @Max(100) Integer size) {
		return ResponseEntity.ok()
			.cacheControl(PUBLIC_CACHE)
			.body(this.catalog.documents(type, family, product, q, PageQuery.of(page, size)));
	}

	@GetMapping("/{slug}")
	@Operation(summary = "Detalle de un documento")
	ResponseEntity<DocumentView> get(@PathVariable @Size(max = 120) String slug) {
		return ResponseEntity.ok().cacheControl(PUBLIC_CACHE).body(this.catalog.document(slug));
	}

	@GetMapping("/{slug}/download")
	@Operation(summary = "Descargar la versión vigente", description = "Redirige (302) al archivo publicado e informa su SHA-256 para verificar la integridad. 409 si el documento es bajo solicitud.")
	ResponseEntity<Void> download(@PathVariable @Size(max = 120) String slug) {
		DocumentView document = this.catalog.downloadable(slug);
		String url = document.file().url();
		URI location = URI.create(url.startsWith("/") ? this.properties.filesBaseUrl() + url : url);
		return ResponseEntity.status(HttpStatus.FOUND)
			.location(location)
			.header("X-Checksum-SHA256", document.file().sha256())
			.cacheControl(CacheControl.noStore())
			.build();
	}

}
