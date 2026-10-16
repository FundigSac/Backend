package com.fundigsac.catalog.domain;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import com.fundigsac.catalog.api.CatalogEvents.DocumentStatusChanged;
import com.fundigsac.shared.domain.AggregateRoot;
import com.fundigsac.shared.domain.ConflictException;
import com.fundigsac.shared.domain.Guard;
import com.fundigsac.shared.domain.RuleViolationException;
import com.fundigsac.shared.domain.Slug;
import com.fundigsac.shared.domain.ValidationException;

/**
 * Documento técnico (ficha, catálogo, manual, plano o certificado). Si es descargable, conserva URL, tipo, tamaño y
 * SHA-256 del archivo publicado; si no hay archivo autorizado, se ofrece "bajo solicitud" (nunca una descarga falsa).
 */
public final class TechnicalDocument extends AggregateRoot {

	public enum Type {
		CATALOG, DATASHEET, MANUAL, DRAWING, CERTIFICATE, OTHER
	}

	public enum Availability {
		DOWNLOAD, ON_REQUEST
	}

	public enum Status {
		DRAFT, PUBLISHED, WITHDRAWN
	}

	/** Archivo publicado y verificable. */
	public record FileReference(String url, String mediaType, long sizeBytes, String sha256) {

		public FileReference {
			url = Guard.text("file.url", url, 2, 500);
			if (!url.matches("^(/[A-Za-z0-9._~/-]+|https://[A-Za-z0-9.-]+/[A-Za-z0-9._~/%-]*)$")) {
				throw new ValidationException("file.url", "INVALID_URL", "Use una ruta del sitio (/docs/…) o una URL https.");
			}
			mediaType = Guard.text("file.mediaType", mediaType, 3, 80);
			if (!mediaType.matches("^[a-z]+/[a-z0-9.+-]+$")) {
				throw new ValidationException("file.mediaType", "INVALID_MEDIA_TYPE", "Tipo de contenido no válido.");
			}
			if (sizeBytes < 1 || sizeBytes > 500L * 1024 * 1024) {
				throw new ValidationException("file.sizeBytes", "OUT_OF_RANGE", "Tamaño de archivo no válido.");
			}
			sha256 = Guard.text("file.sha256", sha256, 64, 64).toLowerCase(java.util.Locale.ROOT);
			if (!sha256.matches("^[0-9a-f]{64}$")) {
				throw new ValidationException("file.sha256", "INVALID_CHECKSUM", "El SHA-256 debe tener 64 caracteres hexadecimales.");
			}
		}

	}

	/** Contenido editable. */
	public record Content(Slug slug, @Nullable String code, String title, @Nullable String description, Type type,
			List<String> families, Availability availability, @Nullable FileReference file, @Nullable String revision,
			String language, Set<UUID> productIds) {

		public Content {
			Guard.notNull("slug", slug);
			code = Guard.optionalText("code", code, 60);
			title = Guard.text("title", title, 3, 200);
			description = Guard.optionalText("description", description, 1000);
			Guard.notNull("type", type);
			families = Guard.maxSize("families", families, 10).stream().map((f) -> Slug.of("families", f).value()).distinct().toList();
			Guard.notNull("availability", availability);
			if (availability == Availability.DOWNLOAD && file == null) {
				throw new ValidationException("file", "REQUIRED", "Un documento descargable necesita su archivo verificado.");
			}
			revision = Guard.optionalText("revision", revision, 40);
			language = Guard.text("language", language, 2, 2);
			productIds = Set.copyOf(Guard.maxSize("products", productIds, 100));
		}

	}

	private final UUID id;

	private Content content;

	private Status status;

	private @Nullable Instant publishedAt;

	private final Instant createdAt;

	private Instant updatedAt;

	private final long version;

	private TechnicalDocument(UUID id, Content content, Status status, @Nullable Instant publishedAt, Instant createdAt,
			Instant updatedAt, long version) {
		this.id = id;
		this.content = content;
		this.status = status;
		this.publishedAt = publishedAt;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.version = version;
	}

	public static TechnicalDocument draft(UUID id, Content content, Instant now) {
		return new TechnicalDocument(id, content, Status.DRAFT, null, now, now, 0);
	}

	public static TechnicalDocument rehydrate(UUID id, Content content, Status status, @Nullable Instant publishedAt,
			Instant createdAt, Instant updatedAt, long version) {
		return new TechnicalDocument(id, content, status, publishedAt, createdAt, updatedAt, version);
	}

	public void edit(Content newContent, Instant now) {
		if (this.status != Status.DRAFT) {
			throw new ConflictException("INVALID_TRANSITION", "Solo se edita un documento en borrador; publique una nueva revisión.");
		}
		this.content = newContent;
		this.updatedAt = now;
	}

	public void publish(UUID actorId, Instant now) {
		if (this.status == Status.PUBLISHED) {
			throw new ConflictException("INVALID_TRANSITION", "El documento ya está publicado.");
		}
		if (this.content.availability() == Availability.DOWNLOAD && this.content.file() == null) {
			throw new RuleViolationException("FILE_REQUIRED", "Falta el archivo verificado.");
		}
		this.status = Status.PUBLISHED;
		this.publishedAt = now;
		this.updatedAt = now;
		register(new DocumentStatusChanged(this.id, this.content.slug().value(), this.status.name(), actorId, now));
	}

	/** Retira un documento obsoleto: deja de listarse y de descargarse. */
	public void withdraw(UUID actorId, Instant now) {
		if (this.status != Status.PUBLISHED) {
			throw new ConflictException("INVALID_TRANSITION", "Solo se retira un documento publicado.");
		}
		this.status = Status.WITHDRAWN;
		this.updatedAt = now;
		register(new DocumentStatusChanged(this.id, this.content.slug().value(), this.status.name(), actorId, now));
	}

	public UUID id() {
		return this.id;
	}

	public Content content() {
		return this.content;
	}

	public Status status() {
		return this.status;
	}

	public @Nullable Instant publishedAt() {
		return this.publishedAt;
	}

	public Instant createdAt() {
		return this.createdAt;
	}

	public Instant updatedAt() {
		return this.updatedAt;
	}

	public long version() {
		return this.version;
	}

}
