package com.fundigsac.catalog.domain;

import java.util.Optional;
import java.util.UUID;

/** Resolución de categorías y sectores por slug (datos de referencia). */
public interface TaxonomyRepository {

	record CategoryRef(UUID id, String slug) {
	}

	Optional<CategoryRef> findCategory(String slug);

	Optional<CategoryRef> findCategoryById(UUID id);

	Optional<UUID> findSectorId(String slug);

}
