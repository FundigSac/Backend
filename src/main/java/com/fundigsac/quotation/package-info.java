/**
 * Cotizaciones B2B: solicitud sin cuenta (o desde el carrito del cliente) con folio correlativo, copia de los datos
 * del producto al momento de pedir, idempotencia, constancia con token y bandeja de Ventas con estados y responsables.
 * La recepción no promete precio: la cotización económica la emite Ventas.
 */
@ApplicationModule(displayName = "Cotizaciones", allowedDependencies = { "shared", "catalog :: api", "identity :: api" })
package com.fundigsac.quotation;

import org.springframework.modulith.ApplicationModule;
