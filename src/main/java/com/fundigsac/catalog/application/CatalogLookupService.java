package com.fundigsac.catalog.application;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fundigsac.catalog.api.CatalogLookup;
import com.fundigsac.catalog.application.CatalogViews.VariantView;

/** Implementación del contrato público CatalogLookup sobre el modelo de lectura. */
@Service
class CatalogLookupService implements CatalogLookup {

	private final CatalogReadModel readModel;

	CatalogLookupService(CatalogReadModel readModel) {
		this.readModel = readModel;
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<ProductSnapshot> findPublished(String slug) {
		if (slug == null || !slug.matches("^[a-z0-9]+(-[a-z0-9]+)*$") || slug.length() > 120) {
			return Optional.empty();
		}
		return this.readModel.productBySlug(slug, true)
			.map((p) -> new ProductSnapshot(p.id(), p.slug(), p.name(), p.sku(), p.category().slug(),
					p.variants().stream().map(VariantView::label).toList()));
	}

}
