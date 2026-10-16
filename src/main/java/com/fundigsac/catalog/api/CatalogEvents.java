package com.fundigsac.catalog.api;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import com.fundigsac.shared.domain.AuditableEvent;

/** Eventos editoriales del catálogo (todos auditables). */
public final class CatalogEvents {

	private CatalogEvents() {
	}

	/** Cambio de estado editorial de un producto (envío a revisión, publicación, rechazo, archivo, reapertura). */
	public record ProductStatusChanged(UUID productId, String slug, String fromStatus, String toStatus,
			@Nullable String note, @Nullable UUID actorId, Instant occurredAt) implements AuditableEvent {

		@Override
		public String auditAction() {
			return "PRODUCT_" + this.toStatus;
		}

		@Override
		public String auditResourceType() {
			return "PRODUCT";
		}

		@Override
		public String auditResourceId() {
			return this.productId.toString();
		}

		@Override
		public Map<String, String> auditDetails() {
			return this.note == null ? Map.of("slug", this.slug, "from", this.fromStatus)
					: Map.of("slug", this.slug, "from", this.fromStatus, "note", this.note);
		}

	}

	public record DocumentStatusChanged(UUID documentId, String slug, String toStatus, @Nullable UUID actorId,
			Instant occurredAt) implements AuditableEvent {

		@Override
		public String auditAction() {
			return "DOCUMENT_" + this.toStatus;
		}

		@Override
		public String auditResourceType() {
			return "DOCUMENT";
		}

		@Override
		public String auditResourceId() {
			return this.documentId.toString();
		}

		@Override
		public Map<String, String> auditDetails() {
			return Map.of("slug", this.slug);
		}

	}

}
