package com.fundigsac.identity.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import com.fundigsac.shared.domain.Email;

/** Puerto de persistencia de cuentas. save() aplica bloqueo optimista con la versión leída. */
public interface UserAccountRepository {

	Optional<UserAccount> findById(UUID id);

	/** Cuenta viva (no eliminada) con ese correo. */
	Optional<UserAccount> findByEmail(Email email);

	boolean existsByEmail(Email email);

	void insert(UserAccount account);

	void update(UserAccount account);

	long countActiveAdministrators();

	List<UserAccount> findStaff(@Nullable String query, int limit, long offset);

	long countStaff(@Nullable String query);

	/** Sello de seguridad y estado actuales (para validar JWT sin cargar la cuenta entera). */
	Optional<SecurityState> findSecurityState(UUID id);

	record SecurityState(int securityStamp, AccountStatus status) {
	}

}
