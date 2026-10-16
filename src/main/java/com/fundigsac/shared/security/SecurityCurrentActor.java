package com.fundigsac.shared.security;

import java.util.Optional;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import com.fundigsac.shared.application.CurrentActor;

/** Actor actual a partir del JWT validado (sujeto = id del usuario, autoridades = roles y permisos). */
@Component
class SecurityCurrentActor implements CurrentActor {

	@Override
	public Optional<UUID> userId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt && jwt.getSubject() != null) {
			try {
				return Optional.of(UUID.fromString(jwt.getSubject()));
			}
			catch (IllegalArgumentException ex) {
				return Optional.empty();
			}
		}
		return Optional.empty();
	}

	@Override
	public boolean has(Permission permission) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null) {
			return false;
		}
		for (GrantedAuthority authority : authentication.getAuthorities()) {
			if (permission.authority().equals(authority.getAuthority())) {
				return true;
			}
		}
		return false;
	}

}
