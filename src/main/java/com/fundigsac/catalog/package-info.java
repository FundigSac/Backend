/**
 * Catálogo técnico: familias, productos con atributos tipados (DN, PN, unión, normas, homologaciones), variantes,
 * sectores y documentos técnicos con checksum. Lectura pública solo de lo publicado; edición con flujo
 * borrador → revisión → publicación y control de cuatro ojos (quien envía a revisión no publica).
 */
@ApplicationModule(displayName = "Catálogo técnico", allowedDependencies = "shared")
package com.fundigsac.catalog;

import org.springframework.modulith.ApplicationModule;
