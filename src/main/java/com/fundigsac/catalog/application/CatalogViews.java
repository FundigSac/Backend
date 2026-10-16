package com.fundigsac.catalog.application;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

/** Modelos de lectura del catálogo (respuestas de la API; sin entidades de dominio). */
public final class CatalogViews {

	private CatalogViews() {
	}

	public record CategoryView(String slug, String name, @Nullable String description, long productCount) {
	}

	public record LabeledCode(String code, String label) {
	}

	public record DnRangeView(int minMm, int maxMm) {
	}

	public record ImageView(String url, String alt, boolean illustrative) {
	}

	public record SpecView(String label, String value) {
	}

	public record VariantView(UUID id, @Nullable String sku, String label, @Nullable Integer dnMm, @Nullable Integer pn) {
	}

	public record SectorRef(String slug, String name) {
	}

	public record DocumentRef(String slug, String title, String type, String availability) {
	}

	public record ProductSummaryView(UUID id, String slug, @Nullable String sku, String name, String summary,
			CategoryRef category, LabeledCode family, @Nullable String material, @Nullable LabeledCode joint,
			@Nullable DnRangeView dn, List<Integer> pressureRatings, List<String> standards, List<LabeledCode> homologations,
			@Nullable ImageView image, int variantCount, boolean dataVerified, String status) {
	}

	public record CategoryRef(String slug, String name) {
	}

	public record ProductDetailView(UUID id, String slug, @Nullable String sku, String name, String summary,
			@Nullable String description, CategoryRef category, LabeledCode family, @Nullable String material,
			@Nullable LabeledCode joint, @Nullable DnRangeView dn, List<Integer> pressureRatings, List<String> standards,
			List<LabeledCode> homologations, List<SpecView> specs, List<ImageView> images, List<VariantView> variants,
			List<SectorRef> sectors, List<DocumentRef> documents, @Nullable String sourceReference, boolean dataVerified,
			String status, @Nullable String reviewNote, @Nullable Instant publishedAt, Instant updatedAt, long version) {
	}

	public record FacetValue(String value, String label, long count) {
	}

	public record CatalogFacets(List<FacetValue> categories, List<FacetValue> families, List<FacetValue> joints,
			List<FacetValue> homologations, List<FacetValue> pressureRatings, List<FacetValue> sectors,
			@Nullable DnRangeView dn) {
	}

	public record SectorView(String slug, String name, @Nullable String description, long productCount) {
	}

	public record FileView(String url, String mediaType, long sizeBytes, String sha256) {
	}

	public record DocumentView(UUID id, String slug, @Nullable String code, String title, @Nullable String description,
			String type, List<String> families, String availability, @Nullable FileView file, @Nullable String revision,
			String language, List<String> productSlugs, String status, @Nullable Instant publishedAt, long version) {
	}

}
