package com.fundigsac.catalog.application;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import com.fundigsac.catalog.application.CatalogViews.CatalogFacets;
import com.fundigsac.catalog.application.CatalogViews.CategoryView;
import com.fundigsac.catalog.application.CatalogViews.DocumentView;
import com.fundigsac.catalog.application.CatalogViews.ProductDetailView;
import com.fundigsac.catalog.application.CatalogViews.ProductSummaryView;
import com.fundigsac.catalog.application.CatalogViews.SectorView;
import com.fundigsac.shared.application.PageQuery;
import com.fundigsac.shared.application.PageResult;

/** Puerto de lectura del catálogo (consultas SQL optimizadas, separadas del modelo de escritura). */
public interface CatalogReadModel {

	enum Sort {
		RELEVANCE, NAME, NEWEST
	}

	/**
	 * Filtros de búsqueda. Todos opcionales; se combinan con AND.
	 *
	 * @param publishedOnly true para el público; false para el personal (con status opcional)
	 */
	record ProductSearch(@Nullable String category, @Nullable String family, @Nullable String joint, @Nullable Integer dn,
			@Nullable Integer pn, @Nullable String homologation, @Nullable String sector, @Nullable String query,
			Sort sort, boolean publishedOnly, @Nullable String status) {
	}

	record DocumentSearch(@Nullable String type, @Nullable String family, @Nullable String product,
			@Nullable String query, boolean publishedOnly) {
	}

	List<CategoryView> categories();

	PageResult<ProductSummaryView> searchProducts(ProductSearch search, PageQuery page);

	Optional<ProductDetailView> productBySlug(String slug, boolean publishedOnly);

	Optional<ProductDetailView> productById(UUID id);

	CatalogFacets facets(@Nullable String category);

	List<SectorView> sectors();

	PageResult<DocumentView> documents(DocumentSearch search, PageQuery page);

	Optional<DocumentView> documentBySlug(String slug, boolean publishedOnly);

	Optional<DocumentView> documentById(UUID id);

}
