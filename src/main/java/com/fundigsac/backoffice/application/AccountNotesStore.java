package com.fundigsac.backoffice.application;

import java.time.Instant;
import java.util.UUID;

/** Puerto de persistencia de las notas internas del personal sobre una cuenta. */
public interface AccountNotesStore {

	/** Guarda (o reemplaza) la nota; una nota vacía la borra. */
	void save(UUID userId, String notes, UUID updatedBy, Instant now);

	void delete(UUID userId);

}
