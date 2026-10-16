package com.fundigsac.shared.security;

/** Expresiones de autorización para @PreAuthorize (constantes de compilación, una por permiso). */
public final class Authz {

	public static final String CUSTOMER = "hasRole('CUSTOMER')";

	public static final String CATALOG_READ = "hasAuthority('catalog:read')";

	public static final String CATALOG_WRITE = "hasAuthority('catalog:write')";

	public static final String CATALOG_PUBLISH = "hasAuthority('catalog:publish')";

	public static final String QUOTE_READ = "hasAuthority('quote:read')";

	public static final String QUOTE_MANAGE = "hasAuthority('quote:manage')";

	public static final String INQUIRY_READ = "hasAuthority('inquiry:read')";

	public static final String INQUIRY_MANAGE = "hasAuthority('inquiry:manage')";

	public static final String COMPLAINT_READ = "hasAuthority('complaint:read')";

	public static final String COMPLAINT_MANAGE = "hasAuthority('complaint:manage')";

	public static final String PRIVACY_READ = "hasAuthority('privacy:read')";

	public static final String PRIVACY_MANAGE = "hasAuthority('privacy:manage')";

	public static final String LEGAL_PUBLISH = "hasAuthority('legal:publish')";

	public static final String USERS_MANAGE = "hasAuthority('users:manage')";

	public static final String AUDIT_READ = "hasAuthority('audit:read')";

	public static final String NOTIFICATIONS_MANAGE = "hasAuthority('notifications:manage')";

	private Authz() {
	}

}
