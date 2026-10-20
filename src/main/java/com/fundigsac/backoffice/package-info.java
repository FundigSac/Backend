/**
 * Panel interno: modelo de lectura del directorio de cuentas (clientes y personal) con sus filtros, indicadores y
 * actividad. Solo lee: junta cuentas, cotizaciones, carritos, sesiones y auditoría en consultas SQL de solo lectura.
 * Los cambios sobre una cuenta los hace el módulo de identidad.
 */
@ApplicationModule(displayName = "Panel interno", allowedDependencies = "shared")
package com.fundigsac.backoffice;

import org.springframework.modulith.ApplicationModule;
