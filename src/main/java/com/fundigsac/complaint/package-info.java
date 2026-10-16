/**
 * Libro de Reclamaciones virtual (Ley N.° 29571 y su reglamento): hoja con número correlativo, constancia para el
 * consumidor, plazo de respuesta en días hábiles y bandeja restringida para Atención Legal. No requiere cuenta y no
 * reutiliza los datos de consultas ni de cotizaciones (otra finalidad).
 */
@ApplicationModule(displayName = "Libro de Reclamaciones", allowedDependencies = { "shared", "identity :: api" })
package com.fundigsac.complaint;

import org.springframework.modulith.ApplicationModule;
