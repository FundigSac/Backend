package com.fundigsac.identity.api;

import java.util.Optional;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

/** Datos de contacto de un cliente activo, para precargar formularios (p. ej. enviar el carrito a cotizar). */
public interface CustomerDirectory {

	record CustomerContact(UUID id, String fullName, String email, @Nullable String companyName, @Nullable String ruc,
			@Nullable String phone) {
	}

	Optional<CustomerContact> findActiveCustomer(UUID customerId);

}
