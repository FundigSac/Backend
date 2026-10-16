package com.fundigsac.shared.application;

import com.fundigsac.shared.domain.Folio;

/** Asigna el siguiente folio correlativo del año (hora de Lima) para un prefijo. Debe llamarse dentro de la transacción. */
public interface FolioGenerator {

	Folio next(String prefix);

}
