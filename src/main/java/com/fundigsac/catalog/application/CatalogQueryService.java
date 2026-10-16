package com.fundigsac.catalog.application;

import java.util.List;
import java.util.Locale;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fundigsac.catalog.application.CatalogReadModel.DocumentSearch;
import com.fundigsac.catalog.application.CatalogReadModel.ProductSearch;
import com.fundigsac.catalog.application.CatalogReadModel.Sort;
import com.fundigsac.catalog.application.CatalogViews.CatalogFacets;
import com.fundigsac.catalog.application.CatalogViews.CategoryView;
import com.fundigsac.catalog.application.CatalogViews.DocumentView;
import com.fundigsac.catalog.application.CatalogViews.ProductDetailView;
import com.fundigsac.catalog.application.CatalogViews.ProductSummaryView;
import com.fundigsac.catalog.application.CatalogViews.SectorView;
import com.fundigsac.catalog.domain.Homologation;
import com.fundigsac.catalog.domain.JointType;
import com.fundigsac.catalog.domain.TechnicalDocument;
import com.fundigsac.shared.application.PageQuery;
import com.fundigsac.shared.application.PageResult;
import com.fundigsac.shared.domain.ConflictException;
import com.fundigsac.shared.domain.Guard;
import com.fundigsac.shared.domain.NotFoundException;
import com.fundigsac.shared.domain.Slug;
import com.fundigsac.shared.domain.ValidationException;

/** Consultas públicas del catálogo: solo contenido publicado y validación estricta de filtros. */
@Service
@Transactional(readOnly = true)
public class CatalogQueryService {

	/** Filtros públicos de búsqueda de productos (crudos, tal como llegan en la URL). */
	public record Filters(@Nullable String category, @Nullable String family, @Nullable String joint,
			@Nullable Integer dn, @Nullable Integer pn, @Nullable String homologation, @Nullable String sector,
			@Nullable String q, @Nullable String sort) {
	}

	private final CatalogReadModel readModel;

	CatalogQueryService(CatalogReadModel readModel) {
		this.readModel = readModel;
	}

	public List<CategoryView> categories() {
		return this.readModel.categories();
	}

	public CategoryView category(String slug) {
		return this.readModel.categories()
			.stream()
			.filter((c) -> c.slug().equals(slug))
			.findFirst()
			.orElseThrow(() -> new NotFoundException("CATEGORY_NOT_FOUND", "La categoría no existe."));
	}

	public PageResult<ProductSummaryView> search(Filters filters, PageQuery page) {
		return this.readModel.searchProducts(toSearch(filters, true, null), page);
	}

	public ProductDetailView product(String slug) {
		return this.readModel.productBySlug(slug, true)
			.orElseThrow(() -> new NotFoundException("PRODUCT_NOT_FOUND", "El producto no existe o no está publicado."));
	}

	public CatalogFacets facets(@Nullable String category) {
		return this.readModel.facets(slugOrNull("category", category));
	}

	public List<SectorView> sectors() {
		return this.readModel.sectors();
	}

	public PageResult<DocumentView> documents(@Nullable String type, @Nullable String family, @Nullable String product,
			@Nullable String q, PageQuery page) {
		return this.readModel.documents(new DocumentSearch(documentType(type), slugOrNull("family", family),
				slugOrNull("product", product), query(q), true), page);
	}

	public DocumentView document(String slug) {
		return this.readModel.documentBySlug(slug, true)
			.orElseThrow(() -> new NotFoundException("DOCUMENT_NOT_FOUND", "El documento no existe o no está publicado."));
	}

	/** Archivo a descargar; si el documento no tiene archivo autorizado se pide por contacto (409). */
	public DocumentView downloadable(String slug) {
		DocumentView document = document(slug);
		if (document.file() == null) {
			throw new ConflictException("DOCUMENT_ON_REQUEST",
					"Este documento se entrega bajo solicitud. Pídalo desde el formulario de contacto.");
		}
		return document;
	}

	static ProductSearch toSearch(Filters f, boolean publishedOnly, @Nullable String status) {
		JointType joint = JointType.fromCode(f.joint());
		String homologation = f.homologation() == null || f.homologation().isBlank() ? null
				: Homologation.fromCode(f.homologation()).code();
		if (f.dn() != null && (f.dn() < 1 || f.dn() > 4000)) {
			throw new ValidationException("dn", "OUT_OF_RANGE", "El DN debe estar entre 1 y 4000 mm.");
		}
		if (f.pn() != null && (f.pn() < 1 || f.pn() > 100)) {
			throw new ValidationException("pn", "OUT_OF_RANGE", "El PN debe estar entre 1 y 100.");
		}
		return new ProductSearch(slugOrNull("category", f.category()), slugOrNull("family", f.family()),
				joint == null ? null : joint.code(), f.dn(), f.pn(), homologation, slugOrNull("sector", f.sector()),
				query(f.q()), sort(f.sort(), f.q()), publishedOnly, status);
	}

	private static Sort sort(@Nullable String sort, @Nullable String q) {
		if (sort == null || sort.isBlank()) {
			return q == null || q.isBlank() ? Sort.NAME : Sort.RELEVANCE;
		}
		try {
			return Sort.valueOf(sort.strip().toUpperCase(Locale.ROOT));
		}
		catch (IllegalArgumentException ex) {
			throw new ValidationException("sort", "INVALID_SORT", "Orden no válido (relevance, name o newest).");
		}
	}

	private static @Nullable String query(@Nullable String q) {
		String clean = Guard.optionalText("q", q, 100);
		return clean == null ? null : Slug.fold(clean);
	}

	private static @Nullable String slugOrNull(String field, @Nullable String value) {
		return value == null || value.isBlank() ? null : Slug.of(field, value).value();
	}

	private static @Nullable String documentType(@Nullable String type) {
		if (type == null || type.isBlank()) {
			return null;
		}
		try {
			return TechnicalDocument.Type.valueOf(type.strip().toUpperCase(Locale.ROOT)).name();
		}
		catch (IllegalArgumentException ex) {
			throw new ValidationException("type", "INVALID_TYPE", "Tipo de documento no válido.");
		}
	}

}
