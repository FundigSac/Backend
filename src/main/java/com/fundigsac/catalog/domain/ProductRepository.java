package com.fundigsac.catalog.domain;

import java.util.Optional;
import java.util.UUID;

/** Puerto de persistencia de fichas (escritura). La lectura pública usa el modelo de lectura CatalogReadModel. */
public interface ProductRepository {

	Optional<Product> findById(UUID id);

	boolean slugTaken(String slug, UUID excludingId);

	boolean skuTaken(String sku, UUID excludingId);

	void insert(Product product, String searchText);

	/** Bloqueo optimista: falla con StaleVersionException si la versión cambió. */
	void update(Product product, String searchText);

}
