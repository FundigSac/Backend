/**
 * Notificaciones por correo: escucha los eventos de los demás módulos, escribe cada correo en una bandeja de salida
 * (outbox) y un despachador lo envía con reintentos y espera exponencial. Un correo que falla nunca deshace el
 * registro que lo originó (arquitectura §10.5).
 */
@ApplicationModule(displayName = "Notificaciones", allowedDependencies = { "shared", "identity :: api",
		"quotation :: api", "inquiry :: api", "complaint :: api", "privacy :: api" })
package com.fundigsac.notification;

import org.springframework.modulith.ApplicationModule;
