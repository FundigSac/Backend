package com.fundigsac.catalog.domain;

import com.fundigsac.shared.domain.Guard;
import com.fundigsac.shared.domain.ValidationException;

/**
 * Imagen del producto: ruta del sitio (/media/…) o URL https, con texto alternativo obligatorio (accesibilidad).
 *
 * @param illustrative la imagen es referencial (no corresponde exactamente a la pieza)
 */
public record ProductImage(String url, String alt, boolean illustrative) {

	public ProductImage {
		url = Guard.text("images.url", url, 2, 500);
		if (!url.matches("^(/[A-Za-z0-9._~/-]+|https://[A-Za-z0-9.-]+/[A-Za-z0-9._~/%-]*)$")) {
			throw new ValidationException("images.url", "INVALID_URL", "Use una ruta del sitio (/media/…) o una URL https.");
		}
		alt = Guard.text("images.alt", alt, 3, 200);
	}

}
