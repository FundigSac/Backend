package com.fundigsac.shared.application;

import org.jspecify.annotations.Nullable;

/**
 * Token de constancia: permite a quien envió un formulario consultar su constancia sin cuenta. Se deriva con HMAC
 * del folio, así no se guarda ningún secreto en la base de datos y un reintento devuelve el mismo token.
 */
public interface ReceiptTokens {

	String issue(String purpose, String reference);

	boolean matches(String purpose, String reference, @Nullable String token);

}
