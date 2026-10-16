package com.fundigsac.catalog.domain;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import com.fundigsac.shared.domain.Guard;
import com.fundigsac.shared.domain.Slug;
import com.fundigsac.shared.domain.ValidationException;

/**
 * Contenido editable de una ficha. Valida tipos, rangos y tamaños: lo que se guarda es exactamente lo validado.
 *
 * @param dataVerified Ingeniería confirmó los datos con documentación de respaldo
 * @param sourceReference documento de origen de los datos (procedencia, arquitectura §9.7)
 */
public record ProductDetails(UUID categoryId, Slug slug, @Nullable Sku sku, String name, String summary,
		@Nullable String description, String family, @Nullable String material, @Nullable JointType joint,
		@Nullable DnRange dn, List<Integer> pressureRatings, List<String> standards, Set<Homologation> homologations,
		List<TechnicalSpec> specs, List<ProductImage> images, Set<UUID> sectorIds, @Nullable String sourceReference,
		boolean dataVerified) {

	public ProductDetails {
		Guard.notNull("category", categoryId);
		Guard.notNull("slug", slug);
		name = Guard.text("name", name, 3, 200);
		summary = Guard.text("summary", summary, 10, 600);
		description = Guard.optionalMultiline("description", description, 8000);
		family = Slug.of("family", family).value();
		if (family.length() > 60) {
			throw new ValidationException("family", "TOO_LONG", "Debe tener como máximo 60 caracteres.");
		}
		material = Guard.optionalText("material", material, 200);
		pressureRatings = List.copyOf(new LinkedHashSet<>(Guard.maxSize("pressureRatings", pressureRatings, 10)));
		for (Integer pn : pressureRatings) {
			if (pn == null || pn < 1 || pn > 100) {
				throw new ValidationException("pressureRatings", "OUT_OF_RANGE", "Cada PN debe estar entre 1 y 100.");
			}
		}
		standards = Guard.maxSize("standards", standards, 20).stream().map((s) -> Guard.text("standards", s, 2, 60)).distinct().toList();
		homologations = Set.copyOf(homologations);
		specs = List.copyOf(Guard.maxSize("specs", specs, 40));
		images = List.copyOf(Guard.maxSize("images", images, 12));
		sectorIds = Set.copyOf(new HashSet<>(Guard.maxSize("sectors", sectorIds, 10)));
		sourceReference = Guard.optionalText("sourceReference", sourceReference, 300);
	}

}
