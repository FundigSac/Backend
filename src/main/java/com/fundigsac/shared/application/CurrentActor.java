package com.fundigsac.shared.application;

import java.util.Optional;
import java.util.UUID;

import com.fundigsac.shared.domain.ForbiddenException;
import com.fundigsac.shared.security.Permission;

/** Quién ejecuta el caso de uso. La capa de aplicación lo usa para autorizar por objeto, no solo por ruta. */
public interface CurrentActor {

	Optional<UUID> userId();

	boolean has(Permission permission);

	default UUID requireUserId() {
		return userId().orElseThrow(() -> new ForbiddenException("AUTHENTICATION_REQUIRED", "Inicie sesión para continuar."));
	}

}
