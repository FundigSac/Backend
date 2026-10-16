package com.fundigsac.identity.domain;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/** Puerto de persistencia de tokens de un solo uso. */
public interface OneTimeTokenRepository {

	void insert(OneTimeToken token);

	Optional<OneTimeToken> findByHash(String tokenHash);

	/** Marca como usado; false si otro proceso ya lo usó (evita doble canje). */
	boolean markUsed(UUID id, Instant now);

	/** Invalida los tokens pendientes del mismo propósito (al emitir uno nuevo o al cambiar la contraseña). */
	void invalidatePending(UUID userId, OneTimeToken.Purpose purpose, Instant now);

}
