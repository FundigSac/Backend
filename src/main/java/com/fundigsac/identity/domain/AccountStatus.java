package com.fundigsac.identity.domain;

/** Ciclo de vida de una cuenta. Solo ACTIVE puede iniciar sesión. */
public enum AccountStatus {
	PENDING_VERIFICATION, ACTIVE, DISABLED, DELETED
}
