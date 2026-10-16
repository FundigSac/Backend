package com.fundigsac.shared.security;

/**
 * Permisos finos del personal. Los roles (módulo identity) agrupan permisos; cada caso de uso exige el permiso
 * concreto, nunca solo "es personal". El texto es la autoridad que viaja en el JWT y usa @PreAuthorize.
 */
public enum Permission {

	CATALOG_READ("catalog:read"),
	CATALOG_WRITE("catalog:write"),
	CATALOG_PUBLISH("catalog:publish"),
	QUOTE_READ("quote:read"),
	QUOTE_MANAGE("quote:manage"),
	INQUIRY_READ("inquiry:read"),
	INQUIRY_MANAGE("inquiry:manage"),
	COMPLAINT_READ("complaint:read"),
	COMPLAINT_MANAGE("complaint:manage"),
	PRIVACY_READ("privacy:read"),
	PRIVACY_MANAGE("privacy:manage"),
	LEGAL_PUBLISH("legal:publish"),
	USERS_MANAGE("users:manage"),
	AUDIT_READ("audit:read"),
	NOTIFICATIONS_MANAGE("notifications:manage");

	private final String authority;

	Permission(String authority) {
		this.authority = authority;
	}

	public String authority() {
		return this.authority;
	}

}
