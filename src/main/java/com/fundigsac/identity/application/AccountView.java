package com.fundigsac.identity.application;

import java.time.Instant;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import com.fundigsac.identity.domain.UserAccount;

/** Vista de una cuenta para su dueño o para la administración (sin hash, tokens ni contadores internos). */
public record AccountView(UUID id, String kind, String email, String fullName, @Nullable String companyName,
		@Nullable String ruc, @Nullable String phone, String status, Set<String> roles, Set<String> permissions,
		boolean emailVerified, @Nullable Instant lastLoginAt, Instant createdAt, long version) {

	public static AccountView of(UserAccount account) {
		Set<String> roles = new TreeSet<>();
		account.roles().forEach((role) -> roles.add(role.name()));
		Set<String> permissions = new TreeSet<>();
		account.permissions().forEach((permission) -> permissions.add(permission.authority()));
		return new AccountView(account.id(), account.kind().name(), account.email().value(), account.fullName(),
				account.companyName(), account.ruc() == null ? null : account.ruc().value(),
				account.phone() == null ? null : account.phone().value(), account.status().name(), roles, permissions,
				account.emailVerifiedAt() != null, account.lastLoginAt(), account.createdAt(), account.version());
	}

}
