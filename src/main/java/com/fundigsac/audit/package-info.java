/**
 * Auditoría: registra quién hizo qué y sobre qué recurso a partir de los eventos auditables de todos los módulos.
 * Solo depende del núcleo compartido; la base de datos impide modificar o borrar registros.
 */
@ApplicationModule(displayName = "Auditoría", allowedDependencies = "shared")
package com.fundigsac.audit;

import org.springframework.modulith.ApplicationModule;
