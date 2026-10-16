/**
 * Consultas generales del formulario de contacto (asesoría técnica, documentación, reuniones). Separadas de
 * cotizaciones y reclamaciones porque tienen otra finalidad y otro equipo responsable.
 */
@ApplicationModule(displayName = "Consultas de contacto", allowedDependencies = { "shared", "identity :: api" })
package com.fundigsac.inquiry;

import org.springframework.modulith.ApplicationModule;
