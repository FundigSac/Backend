package com.fundigsac.shared.application;

import java.util.List;
import java.util.function.Function;

/** Página de resultados con totales para paginar en la interfaz. */
public record PageResult<T>(List<T> items, int page, int size, long totalItems, int totalPages) {

	public static <T> PageResult<T> of(List<T> items, PageQuery query, long totalItems) {
		int totalPages = (int) ((totalItems + query.size() - 1) / query.size());
		return new PageResult<>(List.copyOf(items), query.page(), query.size(), totalItems, totalPages);
	}

	public <R> PageResult<R> map(Function<T, R> mapper) {
		return new PageResult<>(this.items.stream().map(mapper).toList(), this.page, this.size, this.totalItems,
				this.totalPages);
	}

}
