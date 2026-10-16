package com.fundigsac.identity.api;

import java.util.UUID;

import com.fundigsac.shared.security.Permission;

/** Verifica que una persona del personal exista, esté activa y tenga un permiso (p. ej. para asignarle una bandeja). */
public interface StaffDirectory {

	boolean isActiveStaffWith(UUID userId, Permission permission);

}
