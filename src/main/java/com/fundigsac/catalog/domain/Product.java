package com.fundigsac.catalog.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import com.fundigsac.catalog.api.CatalogEvents.ProductStatusChanged;
import com.fundigsac.shared.domain.AggregateRoot;
import com.fundigsac.shared.domain.ConflictException;
import com.fundigsac.shared.domain.ForbiddenException;
import com.fundigsac.shared.domain.Guard;
import com.fundigsac.shared.domain.RuleViolationException;
import com.fundigsac.shared.domain.Slug;
import com.fundigsac.shared.domain.ValidationException;

/**
 * Ficha de producto (agregado con sus variantes). Flujo editorial: DRAFT → IN_REVIEW → PUBLISHED → ARCHIVED; un
 * revisor distinto de quien envió a revisión publica o devuelve a borrador. Solo se edita en DRAFT.
 */
public final class Product extends AggregateRoot {

	public static final int MAX_VARIANTS = 200;

	private final UUID id;

	private ProductDetails details;

	private List<Variant> variants;

	private ProductStatus status;

	private @Nullable String reviewNote;

	private final @Nullable UUID createdBy;

	private @Nullable UUID submittedBy;

	private @Nullable Instant submittedAt;

	private @Nullable UUID publishedBy;

	private @Nullable Instant publishedAt;

	private final Instant createdAt;

	private Instant updatedAt;

	private final long version;

	private Product(UUID id, ProductDetails details, List<Variant> variants, ProductStatus status,
			@Nullable String reviewNote, @Nullable UUID createdBy, @Nullable UUID submittedBy,
			@Nullable Instant submittedAt, @Nullable UUID publishedBy, @Nullable Instant publishedAt, Instant createdAt,
			Instant updatedAt, long version) {
		this.id = id;
		this.details = details;
		this.variants = checkVariants(variants);
		this.status = status;
		this.reviewNote = reviewNote;
		this.createdBy = createdBy;
		this.submittedBy = submittedBy;
		this.submittedAt = submittedAt;
		this.publishedBy = publishedBy;
		this.publishedAt = publishedAt;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.version = version;
	}

	public static Product draft(UUID id, ProductDetails details, List<Variant> variants, UUID createdBy, Instant now) {
		return new Product(id, details, variants, ProductStatus.DRAFT, null, createdBy, null, null, null, null, now, now, 0);
	}

	public static Product rehydrate(UUID id, ProductDetails details, List<Variant> variants, ProductStatus status,
			@Nullable String reviewNote, @Nullable UUID createdBy, @Nullable UUID submittedBy,
			@Nullable Instant submittedAt, @Nullable UUID publishedBy, @Nullable Instant publishedAt, Instant createdAt,
			Instant updatedAt, long version) {
		return new Product(id, details, variants, status, reviewNote, createdBy, submittedBy, submittedAt, publishedBy,
				publishedAt, createdAt, updatedAt, version);
	}

	public void edit(ProductDetails newDetails, List<Variant> newVariants, Instant now) {
		requireStatus(ProductStatus.DRAFT, "Solo se editan fichas en borrador. Reábrala primero.");
		this.details = newDetails;
		this.variants = checkVariants(newVariants);
		this.updatedAt = now;
	}

	public void submitForReview(UUID actorId, Instant now) {
		requireStatus(ProductStatus.DRAFT, "Solo un borrador puede enviarse a revisión.");
		if (this.details.images().isEmpty()) {
			throw new RuleViolationException("IMAGE_REQUIRED", "La ficha necesita al menos una imagen antes de revisión.");
		}
		this.submittedBy = actorId;
		this.submittedAt = now;
		this.reviewNote = null;
		transition(ProductStatus.IN_REVIEW, null, actorId, now);
	}

	/** Control de cuatro ojos: quien envió a revisión no puede publicar su propia ficha. */
	public void publish(UUID reviewerId, Instant now) {
		requireStatus(ProductStatus.IN_REVIEW, "Solo se publica una ficha en revisión.");
		if (Objects.equals(reviewerId, this.submittedBy)) {
			throw new ForbiddenException("FOUR_EYES", "Otra persona debe revisar y publicar esta ficha.");
		}
		this.publishedBy = reviewerId;
		this.publishedAt = now;
		transition(ProductStatus.PUBLISHED, null, reviewerId, now);
	}

	public void reject(UUID reviewerId, String note, Instant now) {
		requireStatus(ProductStatus.IN_REVIEW, "Solo se devuelve una ficha en revisión.");
		this.reviewNote = Guard.text("note", note, 5, 500);
		transition(ProductStatus.DRAFT, this.reviewNote, reviewerId, now);
	}

	public void archive(UUID actorId, Instant now) {
		if (this.status != ProductStatus.PUBLISHED && this.status != ProductStatus.DRAFT) {
			throw new ConflictException("INVALID_TRANSITION", "Solo se archiva una ficha publicada o en borrador.");
		}
		transition(ProductStatus.ARCHIVED, null, actorId, now);
	}

	/** Vuelve a borrador para editar (deja de ser pública hasta que se publique de nuevo). */
	public void reopen(UUID actorId, Instant now) {
		if (this.status != ProductStatus.PUBLISHED && this.status != ProductStatus.ARCHIVED) {
			throw new ConflictException("INVALID_TRANSITION", "Solo se reabre una ficha publicada o archivada.");
		}
		transition(ProductStatus.DRAFT, null, actorId, now);
	}

	/**
	 * Texto de búsqueda sin tildes (misma receta que el generador de datos iniciales): nombre, código, resumen,
	 * material, familia, categoría, normas, homologaciones y variantes.
	 */
	public String searchText(String categorySlug) {
		List<String> parts = new ArrayList<>();
		parts.add(this.details.name());
		parts.add(this.details.sku() == null ? "" : this.details.sku().value());
		parts.add(this.details.summary());
		parts.add(this.details.material() == null ? "" : this.details.material());
		parts.add(Families.label(this.details.family()));
		parts.add(categorySlug);
		parts.addAll(this.details.standards());
		this.details.homologations().forEach((h) -> parts.add(h.code()));
		this.variants.forEach((v) -> parts.add(v.label()));
		return Slug.fold(String.join(" ", parts));
	}

	private void transition(ProductStatus to, @Nullable String note, UUID actorId, Instant now) {
		ProductStatus from = this.status;
		this.status = to;
		this.updatedAt = now;
		register(new ProductStatusChanged(this.id, this.details.slug().value(), from.name(), to.name(), note, actorId, now));
	}

	private void requireStatus(ProductStatus expected, String message) {
		if (this.status != expected) {
			throw new ConflictException("INVALID_TRANSITION", message);
		}
	}

	private static List<Variant> checkVariants(List<Variant> variants) {
		Guard.maxSize("variants", variants, MAX_VARIANTS);
		Set<String> labels = new HashSet<>();
		Set<String> skus = new HashSet<>();
		for (Variant v : variants) {
			if (!labels.add(v.label())) {
				throw new ValidationException("variants", "DUPLICATE_VARIANT", "Variante repetida: " + v.label());
			}
			if (v.sku() != null && !skus.add(v.sku().value())) {
				throw new ValidationException("variants", "DUPLICATE_SKU", "Código repetido: " + v.sku().value());
			}
		}
		return List.copyOf(variants);
	}

	public UUID id() {
		return this.id;
	}

	public ProductDetails details() {
		return this.details;
	}

	public List<Variant> variants() {
		return this.variants;
	}

	public ProductStatus status() {
		return this.status;
	}

	public @Nullable String reviewNote() {
		return this.reviewNote;
	}

	public @Nullable UUID createdBy() {
		return this.createdBy;
	}

	public @Nullable UUID submittedBy() {
		return this.submittedBy;
	}

	public @Nullable Instant submittedAt() {
		return this.submittedAt;
	}

	public @Nullable UUID publishedBy() {
		return this.publishedBy;
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
