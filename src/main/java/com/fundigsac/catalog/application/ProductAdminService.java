package com.fundigsac.catalog.application;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fundigsac.catalog.application.CatalogViews.ProductDetailView;
import com.fundigsac.catalog.application.CatalogViews.ProductSummaryView;
import com.fundigsac.catalog.domain.DnRange;
import com.fundigsac.catalog.domain.Homologation;
import com.fundigsac.catalog.domain.JointType;
import com.fundigsac.catalog.domain.Product;
import com.fundigsac.catalog.domain.ProductDetails;
import com.fundigsac.catalog.domain.ProductImage;
import com.fundigsac.catalog.domain.ProductRepository;
import com.fundigsac.catalog.domain.ProductStatus;
import com.fundigsac.catalog.domain.Sku;
import com.fundigsac.catalog.domain.TaxonomyRepository;
import com.fundigsac.catalog.domain.TechnicalSpec;
import com.fundigsac.catalog.domain.Variant;
import com.fundigsac.shared.application.CurrentActor;
import com.fundigsac.shared.application.DomainEvents;
import com.fundigsac.shared.application.PageQuery;
import com.fundigsac.shared.application.PageResult;
import com.fundigsac.shared.domain.ConflictException;
import com.fundigsac.shared.domain.Ids;
import com.fundigsac.shared.domain.NotFoundException;
import com.fundigsac.shared.domain.RuleViolationException;
import com.fundigsac.shared.domain.Slug;
import com.fundigsac.shared.domain.StaleVersionException;
import com.fundigsac.shared.domain.ValidationException;
import com.fundigsac.shared.security.Authz;

/** Gestión editorial de fichas por el personal (borradores, revisión, publicación y archivo). */
@Service
public class ProductAdminService {

	public record SpecInput(String label, String value) {
	}

	public record ImageInput(String url, String alt, boolean illustrative) {
	}

	public record VariantInput(@Nullable String sku, String label, @Nullable Integer dnMm, @Nullable Integer pn) {
	}

	public record ProductDraft(String categorySlug, String slug, @Nullable String sku, String name, String summary,
			@Nullable String description, String family, @Nullable String material, @Nullable String joint,
			@Nullable Integer dnMinMm, @Nullable Integer dnMaxMm, @Nullable List<Integer> pressureRatings,
			@Nullable List<String> standards, @Nullable List<String> homologations, @Nullable List<SpecInput> specs,
			@Nullable List<ImageInput> images, @Nullable List<String> sectors, @Nullable List<VariantInput> variants,
			@Nullable String sourceReference, boolean dataVerified) {
	}

	private final ProductRepository products;

	private final TaxonomyRepository taxonomy;

	private final CatalogReadModel readModel;

	private final CurrentActor actor;

	private final DomainEvents events;

	private final Clock clock;

	ProductAdminService(ProductRepository products, TaxonomyRepository taxonomy, CatalogReadModel readModel,
			CurrentActor actor, DomainEvents events, Clock clock) {
		this.products = products;
		this.taxonomy = taxonomy;
		this.readModel = readModel;
		this.actor = actor;
		this.events = events;
		this.clock = clock;
	}

	@PreAuthorize(Authz.CATALOG_READ)
	@Transactional(readOnly = true)
	public PageResult<ProductSummaryView> list(CatalogQueryService.Filters filters, @Nullable String status, PageQuery page) {
		String normalizedStatus = status == null || status.isBlank() ? null : status(status).name();
		return this.readModel.searchProducts(CatalogQueryService.toSearch(filters, false, normalizedStatus), page);
	}

	@PreAuthorize(Authz.CATALOG_READ)
	@Transactional(readOnly = true)
	public ProductDetailView get(UUID id) {
		return this.readModel.productById(id).orElseThrow(ProductAdminService::notFound);
	}

	@PreAuthorize(Authz.CATALOG_WRITE)
	@Transactional
	public ProductDetailView create(ProductDraft draft) {
		UUID id = Ids.newId();
		ProductDetails details = details(draft, id);
		Product product = Product.draft(id, details, variants(draft.variants(), Map.of()), this.actor.requireUserId(),
				this.clock.instant());
		this.products.insert(product, product.searchText(draft.categorySlug()));
		return get(id);
	}

	@PreAuthorize(Authz.CATALOG_WRITE)
	@Transactional
	public ProductDetailView update(UUID id, ProductDraft draft, long expectedVersion) {
		Product product = load(id, expectedVersion);
		Map<String, UUID> existingIds = new HashMap<>();
		product.variants().forEach((v) -> existingIds.put(v.label(), v.id()));
		product.edit(details(draft, id), variants(draft.variants(), existingIds), this.clock.instant());
		return save(product);
	}

	@PreAuthorize(Authz.CATALOG_WRITE)
	@Transactional
	public ProductDetailView submit(UUID id, long expectedVersion) {
		Product product = load(id, expectedVersion);
		product.submitForReview(this.actor.requireUserId(), this.clock.instant());
		return save(product);
	}

	/** Publicar exige datos confirmados por Ingeniería con documento de respaldo (arquitectura §3.3). */
	@PreAuthorize(Authz.CATALOG_PUBLISH)
	@Transactional
	public ProductDetailView publish(UUID id, long expectedVersion) {
		Product product = load(id, expectedVersion);
		if (!product.details().dataVerified() || product.details().sourceReference() == null) {
			throw new RuleViolationException("DATA_NOT_VERIFIED",
					"Solo se publican fichas con datos verificados y documento de respaldo indicado.");
		}
		product.publish(this.actor.requireUserId(), this.clock.instant());
		return save(product);
	}

	@PreAuthorize(Authz.CATALOG_PUBLISH)
	@Transactional
	public ProductDetailView reject(UUID id, String note, long expectedVersion) {
		Product product = load(id, expectedVersion);
		product.reject(this.actor.requireUserId(), note, this.clock.instant());
		return save(product);
	}

	@PreAuthorize(Authz.CATALOG_PUBLISH)
	@Transactional
	public ProductDetailView archive(UUID id, long expectedVersion) {
		Product product = load(id, expectedVersion);
		product.archive(this.actor.requireUserId(), this.clock.instant());
		return save(product);
	}

	@PreAuthorize(Authz.CATALOG_WRITE)
	@Transactional
	public ProductDetailView reopen(UUID id, long expectedVersion) {
		Product product = load(id, expectedVersion);
		product.reopen(this.actor.requireUserId(), this.clock.instant());
		return save(product);
	}

	private ProductDetailView save(Product product) {
		String categorySlug = this.taxonomy.findCategoryById(product.details().categoryId()).orElseThrow().slug();
		this.products.update(product, product.searchText(categorySlug));
		this.events.publishFrom(product);
		return get(product.id());
	}

	private Product load(UUID id, long expectedVersion) {
		Product product = this.products.findById(id).orElseThrow(ProductAdminService::notFound);
		if (product.version() != expectedVersion) {
			throw new StaleVersionException();
		}
		return product;
	}

	private ProductDetails details(ProductDraft d, UUID productId) {
		TaxonomyRepository.CategoryRef category = this.taxonomy.findCategory(Slug.of("categorySlug", d.categorySlug()).value())
			.orElseThrow(() -> new ValidationException("categorySlug", "UNKNOWN_CATEGORY", "La categoría no existe."));
		Slug slug = Slug.of("slug", d.slug());
		if (this.products.slugTaken(slug.value(), productId)) {
			throw new ConflictException("SLUG_TAKEN", "Ya existe un producto con ese slug.");
		}
		Sku sku = Sku.optional(d.sku());
		if (sku != null && this.products.skuTaken(sku.value(), productId)) {
			throw new ConflictException("SKU_TAKEN", "Ya existe un producto con ese código.");
		}
		Set<Homologation> homologations = EnumSet.noneOf(Homologation.class);
		orEmpty(d.homologations()).forEach((code) -> homologations.add(Homologation.fromCode(code)));
		Set<UUID> sectors = new HashSet<>();
		for (String sector : orEmpty(d.sectors())) {
			sectors.add(this.taxonomy.findSectorId(Slug.of("sectors", sector).value())
				.orElseThrow(() -> new ValidationException("sectors", "UNKNOWN_SECTOR", "Sector no reconocido: " + sector)));
		}
		List<TechnicalSpec> specs = orEmpty(d.specs()).stream().map((s) -> new TechnicalSpec(s.label(), s.value())).toList();
		List<ProductImage> images = orEmpty(d.images()).stream()
			.map((i) -> new ProductImage(i.url(), i.alt(), i.illustrative()))
			.toList();
		return new ProductDetails(category.id(), slug, sku, d.name(), d.summary(), d.description(), d.family(),
				d.material(), JointType.fromCode(d.joint()), DnRange.of(d.dnMinMm(), d.dnMaxMm()), orEmpty(d.pressureRatings()),
				orEmpty(d.standards()), homologations, specs, images, sectors, d.sourceReference(), d.dataVerified());
	}

	private static List<Variant> variants(@Nullable List<VariantInput> inputs, Map<String, UUID> existingIds) {
		List<Variant> variants = new ArrayList<>();
		for (VariantInput v : orEmpty(inputs)) {
			String label = v.label() == null ? "" : v.label().strip();
			variants.add(new Variant(existingIds.getOrDefault(label, Ids.newId()), Sku.optional(v.sku()), label, v.dnMm(),
					v.pn()));
		}
		return variants;
	}

	private static ProductStatus status(String value) {
		try {
			return ProductStatus.valueOf(value.strip().toUpperCase(java.util.Locale.ROOT));
		}
		catch (IllegalArgumentException ex) {
			throw new ValidationException("status", "INVALID_STATUS", "Estado no válido.");
		}
	}

	private static <T> List<T> orEmpty(@Nullable List<T> list) {
		return list == null ? List.of() : list;
	}

	private static NotFoundException notFound() {
		return new NotFoundException("PRODUCT_NOT_FOUND", "El producto no existe.");
	}

}
