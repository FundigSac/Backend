package com.fundigsac.catalog.domain;

import org.jspecify.annotations.Nullable;

import com.fundigsac.shared.domain.ValidationException;

/** Rango de diámetro nominal en milímetros (DN mínimo y máximo que cubre la serie). */
public record DnRange(int minMm, int maxMm) {

	public DnRange {
		if (minMm < 1 || maxMm > 4000 || minMm > maxMm) {
			throw new ValidationException("dn", "INVALID_DN_RANGE", "El rango DN debe estar entre 1 y 4000 mm y el mínimo no puede superar al máximo.");
		}
	}

	public static @Nullable DnRange of(@Nullable Integer min, @Nullable Integer max) {
		if (min == null && max == null) {
			return null;
		}
		if (min == null || max == null) {
			throw new ValidationException("dn", "INVALID_DN_RANGE", "Indique DN mínimo y máximo.");
		}
		return new DnRange(min, max);
	}

	public boolean contains(int dn) {
		return dn >= this.minMm && dn <= this.maxMm;
	}

}
