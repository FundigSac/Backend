/**
 * Panel interno: modelo de lectura del directorio de cuentas (clientes y personal) con sus filtros, indicadores y
 * actividad, más las notas internas del personal. Junta cuentas, cotizaciones, carritos, sesiones y auditoría en
 * consultas SQL de solo lectura; los cambios sobre una cuenta los hace el módulo de identidad.
 */
@ApplicationModule(displayName = "Panel interno", allowedDependencies = { "shared", "identity :: api" })
package com.fundigsac.backoffice;

import org.springframework.modulith.ApplicationModule;
