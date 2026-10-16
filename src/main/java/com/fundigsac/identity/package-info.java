/**
 * Identidad y acceso: cuentas de clientes (registro con verificación de correo) y del personal (por invitación),
 * roles con permisos finos, inicio de sesión con bloqueo progresivo, sesiones con refresh tokens rotativos y
 * recuperación de contraseña. Emite los JWT que el resto de módulos solo valida.
 */
@ApplicationModule(displayName = "Identidad y acceso", allowedDependencies = "shared")
package com.fundigsac.identity;

import org.springframework.modulith.ApplicationModule;
