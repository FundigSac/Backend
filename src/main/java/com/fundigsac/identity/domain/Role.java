package com.fundigsac.identity.domain;

import java.util.EnumSet;
import java.util.Set;

import com.fundigsac.shared.security.Permission;

/**
 * Roles iniciales (arquitectura §12.2; política final sujeta a aprobación de Dirección). Menor privilegio: cada rol
 * recibe solo los permisos de su función. CUSTOMER es el único rol de clientes.
 */
public enum Role {

	CUSTOMER(false, EnumSet.noneOf(Permission.class)),
	ADMINISTRATOR(true, EnumSet.allOf(Permission.class)),
	SALES(true, EnumSet.of(Permission.QUOTE_READ, Permission.QUOTE_MANAGE, Permission.INQUIRY_READ,
			Permission.INQUIRY_MANAGE, Permission.CATALOG_READ)),
	CATALOG_EDITOR(true, EnumSet.of(Permission.CATALOG_READ, Permission.CATALOG_WRITE)),
	CATALOG_REVIEWER(true, EnumSet.of(Permission.CATALOG_READ, Permission.CATALOG_PUBLISH)),
	LEGAL_SUPPORT(true, EnumSet.of(Permission.COMPLAINT_READ, Permission.COMPLAINT_MANAGE, Permission.PRIVACY_READ,
			Permission.PRIVACY_MANAGE, Permission.LEGAL_PUBLISH, Permission.INQUIRY_READ)),
	READ_ONLY(true, EnumSet.of(Permission.CATALOG_READ, Permission.QUOTE_READ, Permission.INQUIRY_READ,
			Permission.COMPLAINT_READ));

	private final boolean staff;

	private final Set<Permission> permissions;

	Role(boolean staff, Set<Permission> permissions) {
		this.staff = staff;
		this.permissions = permissions;
	}

	public boolean isStaff() {
		return this.staff;
	}

	public Set<Permission> permissions() {
		return EnumSet.copyOf(this.permissions.isEmpty() ? EnumSet.noneOf(Permission.class) : this.permissions);
	}

}
