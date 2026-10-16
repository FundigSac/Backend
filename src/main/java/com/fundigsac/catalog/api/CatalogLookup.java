package com.fundigsac.catalog.api;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

/** Consulta de productos publicados para validar y copiar datos en cotizaciones y carritos. */
public interface CatalogLookup {

	/**
	 * Copia de los datos de un producto publicado en el momento de la consulta.
	 *
	 * @param variantLabels medidas o variantes válidas (vacía si el producto no tiene variantes)
	 */
	record ProductSnapshot(UUID id, String slug, String name, @Nullable String sku, String categorySlug,
			List<String> variantLabels) {

		public boolean acceptsVariant(@Nullable String label) {
			return label == null || this.variantLabels.isEmpty() || this.variantLabels.contains(label);
		}

	}

	Optional<ProductSnapshot> findPublished(String slug);

}
