/**
 * Núcleo compartido: valores de dominio comunes (correo, RUC, folio…), errores, paginación, idempotencia y la
 * plataforma web/seguridad. Es el único módulo abierto; no contiene reglas de ningún contexto de negocio.
 */
@ApplicationModule(displayName = "Shared kernel", type = ApplicationModule.Type.OPEN)
package com.fundigsac.shared;

import org.springframework.modulith.ApplicationModule;
