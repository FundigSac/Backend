/**
 * Carrito de cotización del cliente con cuenta: lista de productos a cotizar (sin precios) que se convierte en una
 * solicitud de cotización. Equivale al carrito local del sitio, pero sincronizado entre dispositivos.
 */
@ApplicationModule(displayName = "Carrito de cotización",
		allowedDependencies = { "shared", "catalog :: api", "quotation :: api", "identity :: api" })
package com.fundigsac.cart;

import org.springframework.modulith.ApplicationModule;
