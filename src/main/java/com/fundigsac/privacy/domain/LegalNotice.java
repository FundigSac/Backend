package com.fundigsac.privacy.domain;

import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import com.fundigsac.shared.domain.Guard;
import com.fundigsac.shared.domain.NotFoundException;
import com.fundigsac.shared.domain.ValidationException;

/**
 * Versión publicada de un texto legal. Es evidencia: no se edita ni se borra; un cambio es una versión nueva con su
 * fecha de vigencia y el SHA-256 de su contenido.
 */
public record LegalNotice(UUID id, Type type, String version, String title, @Nullable String summary, String contentUrl,
		@Nullable String body, @Nullable String contentSha256, Instant effectiveFrom, @Nullable UUID publishedBy,
		Instant publishedAt) {

	public enum Type {

		PRIVACY_POLICY("privacy-policy"), COOKIE_POLICY("cookie-policy"), TERMS_OF_USE("terms-of-use"),
		B2B_TERMS("b2b-terms"), COMPLAINTS_BOOK("complaints-book");

		private final String slug;

		Type(String slug) {
			this.slug = slug;
		}

		public String slug() {
			return this.slug;
		}

		public static Type fromSlug(String slug) {
			return Arrays.stream(values())
				.filter((t) -> t.slug.equals(slug) || t.name().equalsIgnoreCase(slug))
				.findFirst()
				.orElseThrow(() -> new NotFoundException("LEGAL_NOTICE_NOT_FOUND", "El texto legal no existe."));
		}

	}

	public LegalNotice {
		Guard.notNull("type", type);
		version = Guard.text("version", version, 1, 20);
		if (!version.matches("^[0-9A-Za-z._-]{1,20}$")) {
			throw new ValidationException("version", "INVALID_VERSION", "Use letras, números, punto, guion o guion bajo.");
		}
		title = Guard.text("title", title, 3, 200);
		summary = Guard.optionalText("summary", summary, 1000);
		contentUrl = Guard.text("contentUrl", contentUrl, 1, 300);
		if (!contentUrl.matches("^(/[A-Za-z0-9._~/-]*|https://[A-Za-z0-9.-]+/[A-Za-z0-9._~/%-]*)$")) {
			throw new ValidationException("contentUrl", "INVALID_URL", "Use una ruta del sitio o una URL https.");
		}
		body = Guard.optionalMultiline("body", body, 200_000);
		Guard.notNull("effectiveFrom", effectiveFrom);
	}

}
