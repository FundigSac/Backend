package com.fundigsac.catalog.web;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fundigsac.catalog.application.CatalogQueryService;
import com.fundigsac.catalog.application.CatalogViews.DocumentView;
import com.fundigsac.catalog.application.CatalogViews.ProductDetailView;
import com.fundigsac.catalog.application.CatalogViews.ProductSummaryView;
import com.fundigsac.catalog.application.DocumentAdminService;
import com.fundigsac.catalog.application.ProductAdminService;
import com.fundigsac.shared.application.PageQuery;
import com.fundigsac.shared.application.PageResult;
import com.fundigsac.shared.web.ApiDocs;
import com.fundigsac.shared.web.ETags;

/** Gestión editorial del catálogo por el personal. Toda modificación exige If-Match (control de concurrencia). */
@RestController
@RequestMapping("/api/v1/staff/catalog")
@SecurityRequirement(name = ApiDocs.BEARER)
@Tag(name = "Personal: catálogo", description = "Borradores, revisión de cuatro ojos, publicación y documentos")
class StaffCatalogController {

	record SpecRequest(@NotBlank @Size(max = 80) String label, @NotBlank @Size(max = 300) String value) {
	}

	record ImageRequest(@NotBlank @Size(max = 500) String url, @NotBlank @Size(max = 200) String alt, boolean illustrative) {
	}

	record VariantRequest(@Size(max = 40) String sku, @NotBlank @Size(max = 120) String label,
			@Min(1) @Max(4000) Integer dnMm, @Min(1) @Max(100) Integer pn) {
	}

	record ProductRequest(@NotBlank @Size(max = 120) String categorySlug, @NotBlank @Size(max = 120) String slug,
			@Size(max = 40) String sku, @NotBlank @Size(min = 3, max = 200) String name,
			@NotBlank @Size(min = 10, max = 600) String summary, @Size(max = 8000) String description,
			@NotBlank @Size(max = 60) String family, @Size(max = 200) String material, @Size(max = 20) String joint,
			@Min(1) @Max(4000) Integer dnMinMm, @Min(1) @Max(4000) Integer dnMaxMm,
			@Size(max = 10) List<@Min(1) @Max(100) Integer> pressureRatings, @Size(max = 20) List<@Size(max = 60) String> standards,
			@Size(max = 3) List<@Size(max = 20) String> homologations, @Size(max = 40) List<@Valid SpecRequest> specs,
			@Size(max = 12) List<@Valid ImageRequest> images, @Size(max = 10) List<@Size(max = 120) String> sectors,
			@Size(max = 200) List<@Valid VariantRequest> variants, @Size(max = 300) String sourceReference,
			boolean dataVerified) {

		ProductAdminService.ProductDraft toDraft() {
			return new ProductAdminService.ProductDraft(this.categorySlug, this.slug, this.sku, this.name, this.summary,
					this.description, this.family, this.material, this.joint, this.dnMinMm, this.dnMaxMm, this.pressureRatings,
					this.standards, this.homologations,
					this.specs == null ? null : this.specs.stream().map((s) -> new ProductAdminService.SpecInput(s.label(), s.value())).toList(),
					this.images == null ? null : this.images.stream()
						.map((i) -> new ProductAdminService.ImageInput(i.url(), i.alt(), i.illustrative()))
						.toList(),
					this.sectors,
					this.variants == null ? null : this.variants.stream()
						.map((v) -> new ProductAdminService.VariantInput(v.sku(), v.label(), v.dnMm(), v.pn()))
						.toList(),
					this.sourceReference, this.dataVerified);
		}

	}

	record RejectRequest(@NotBlank @Size(min = 5, max = 500) String note) {
	}

	record FileRequest(@NotBlank @Size(max = 500) String url, @NotBlank @Size(max = 80) String mediaType,
			@NotNull @Positive Long sizeBytes, @NotBlank @Pattern(regexp = "^[0-9a-fA-F]{64}$") String sha256) {
	}

	record DocumentRequest(@NotBlank @Size(max = 120) String slug, @Size(max = 60) String code,
			@NotBlank @Size(min = 3, max = 200) String title, @Size(max = 1000) String description,
			@NotBlank @Size(max = 20) String type, @Size(max = 10) List<@Size(max = 120) String> families,
			@NotBlank @Size(max = 12) String availability, @Valid FileRequest file, @Size(max = 40) String revision,
			@Size(min = 2, max = 2) String language, @Size(max = 100) List<@Size(max = 120) String> productSlugs) {

		DocumentAdminService.DocumentDraft toDraft() {
			return new DocumentAdminService.DocumentDraft(this.slug, this.code, this.title, this.description, this.type,
					this.families, this.availability,
					this.file == null ? null
							: new DocumentAdminService.FileInput(this.file.url(), this.file.mediaType(), this.file.sizeBytes(),
									this.file.sha256()),
					this.revision, this.language, this.productSlugs);
		}

	}

	private final ProductAdminService products;

	private final DocumentAdminService documents;

	StaffCatalogController(ProductAdminService products, DocumentAdminService documents) {
		this.products = products;
		this.documents = documents;
	}

	// ----- productos -----

	@GetMapping("/products")
	@Operation(summary = "Listar fichas (todos los estados)", description = "status: DRAFT, IN_REVIEW, PUBLISHED o ARCHIVED.")
	PageResult<ProductSummaryView> list(@RequestParam(required = false) @Size(max = 12) String status,
			@RequestParam(required = false) @Size(max = 120) String category, @RequestParam(required = false) @Size(max = 100) String q,
			@RequestParam(required = false) @Min(0) Integer page, @RequestParam(required = false) @Min(1) @Max(100) Integer size) {
		var filters = new CatalogQueryService.Filters(category, null, null, null, null, null, null, q, null);
		return this.products.list(filters, status, PageQuery.of(page, size));
	}

	@GetMapping("/products/{id}")
	@Operation(summary = "Ver ficha (cualquier estado)")
	ResponseEntity<ProductDetailView> get(@PathVariable UUID id) {
		return withEtag(this.products.get(id));
	}

	@PostMapping("/products")
	@Operation(summary = "Crear borrador")
	ResponseEntity<ProductDetailView> create(@Valid @RequestBody ProductRequest request) {
		ProductDetailView view = this.products.create(request.toDraft());
		return ResponseEntity.created(URI.create("/api/v1/staff/catalog/products/" + view.id()))
			.eTag(ETags.of(view.version()))
			.body(view);
	}

	@PutMapping("/products/{id}")
	@Operation(summary = "Editar borrador", description = "Requiere If-Match.")
	ResponseEntity<ProductDetailView> update(@PathVariable UUID id, @RequestHeader(name = "If-Match", required = false) String ifMatch,
			@Valid @RequestBody ProductRequest request) {
		return withEtag(this.products.update(id, request.toDraft(), ETags.requireVersion(ifMatch)));
	}

	@PostMapping("/products/{id}/submit")
	@Operation(summary = "Enviar a revisión", description = "Requiere If-Match.")
	ResponseEntity<ProductDetailView> submit(@PathVariable UUID id, @RequestHeader(name = "If-Match", required = false) String ifMatch) {
		return withEtag(this.products.submit(id, ETags.requireVersion(ifMatch)));
	}

	@PostMapping("/products/{id}/publish")
	@Operation(summary = "Publicar (revisor distinto de quien envió)", description = "Requiere If-Match y datos verificados con documento de respaldo.")
	ResponseEntity<ProductDetailView> publish(@PathVariable UUID id, @RequestHeader(name = "If-Match", required = false) String ifMatch) {
		return withEtag(this.products.publish(id, ETags.requireVersion(ifMatch)));
	}

	@PostMapping("/products/{id}/reject")
	@Operation(summary = "Devolver a borrador con observaciones", description = "Requiere If-Match.")
	ResponseEntity<ProductDetailView> reject(@PathVariable UUID id, @RequestHeader(name = "If-Match", required = false) String ifMatch,
			@Valid @RequestBody RejectRequest request) {
		return withEtag(this.products.reject(id, request.note(), ETags.requireVersion(ifMatch)));
	}

	@PostMapping("/products/{id}/archive")
	@Operation(summary = "Archivar (deja de ser pública)", description = "Requiere If-Match.")
	ResponseEntity<ProductDetailView> archive(@PathVariable UUID id, @RequestHeader(name = "If-Match", required = false) String ifMatch) {
		return withEtag(this.products.archive(id, ETags.requireVersion(ifMatch)));
	}

	@PostMapping("/products/{id}/reopen")
	@Operation(summary = "Reabrir como borrador para editar", description = "Requiere If-Match.")
	ResponseEntity<ProductDetailView> reopen(@PathVariable UUID id, @RequestHeader(name = "If-Match", required = false) String ifMatch) {
		return withEtag(this.products.reopen(id, ETags.requireVersion(ifMatch)));
	}

	// ----- documentos -----

	@GetMapping("/documents")
	@Operation(summary = "Listar documentos (todos los estados)")
	PageResult<DocumentView> documents(@RequestParam(required = false) @Size(max = 100) String q,
			@RequestParam(required = false) @Min(0) Integer page, @RequestParam(required = false) @Min(1) @Max(100) Integer size) {
		return this.documents.list(q, PageQuery.of(page, size));
	}

	@GetMapping("/documents/{id}")
	@Operation(summary = "Ver documento")
	ResponseEntity<DocumentView> document(@PathVariable UUID id) {
		DocumentView view = this.documents.get(id);
		return ResponseEntity.ok().eTag(ETags.of(view.version())).body(view);
	}

	@PostMapping("/documents")
	@Operation(summary = "Registrar documento (borrador)", description = "Si es descargable, indique URL, tipo, tamaño y SHA-256 del archivo.")
	ResponseEntity<DocumentView> createDocument(@Valid @RequestBody DocumentRequest request) {
		DocumentView view = this.documents.create(request.toDraft());
		return ResponseEntity.created(URI.create("/api/v1/staff/catalog/documents/" + view.id()))
			.eTag(ETags.of(view.version()))
			.body(view);
	}

	@PutMapping("/documents/{id}")
	@Operation(summary = "Editar documento en borrador", description = "Requiere If-Match.")
	ResponseEntity<DocumentView> updateDocument(@PathVariable UUID id, @RequestHeader(name = "If-Match", required = false) String ifMatch,
			@Valid @RequestBody DocumentRequest request) {
		DocumentView view = this.documents.update(id, request.toDraft(), ETags.requireVersion(ifMatch));
		return ResponseEntity.ok().eTag(ETags.of(view.version())).body(view);
	}

	@PostMapping("/documents/{id}/publish")
	@Operation(summary = "Publicar documento", description = "Requiere If-Match.")
	ResponseEntity<DocumentView> publishDocument(@PathVariable UUID id, @RequestHeader(name = "If-Match", required = false) String ifMatch) {
		DocumentView view = this.documents.publish(id, ETags.requireVersion(ifMatch));
		return ResponseEntity.ok().eTag(ETags.of(view.version())).body(view);
	}

	@PostMapping("/documents/{id}/withdraw")
	@Operation(summary = "Retirar documento obsoleto", description = "Requiere If-Match.")
	ResponseEntity<DocumentView> withdrawDocument(@PathVariable UUID id, @RequestHeader(name = "If-Match", required = false) String ifMatch) {
		DocumentView view = this.documents.withdraw(id, ETags.requireVersion(ifMatch));
		return ResponseEntity.ok().eTag(ETags.of(view.version())).body(view);
	}

	private static ResponseEntity<ProductDetailView> withEtag(ProductDetailView view) {
		return ResponseEntity.ok().eTag(ETags.of(view.version())).body(view);
	}

}
