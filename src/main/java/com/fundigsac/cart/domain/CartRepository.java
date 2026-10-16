package com.fundigsac.cart.domain;

import java.util.Optional;
import java.util.UUID;

/** Puerto de persistencia del carrito. */
public interface CartRepository {

	Optional<QuoteCart> find(UUID ownerId);

	/** Inserta o actualiza con bloqueo optimista (StaleVersionException si cambió en otra pestaña). */
	void save(QuoteCart cart);

	void delete(UUID ownerId);

}
