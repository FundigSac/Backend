package com.fundigsac.shared.application;

import org.jspecify.annotations.Nullable;

/** Página pedida por el cliente; el servidor fija el tamaño máximo para no devolver listas sin límite. */
public record PageQuery(int page, int size) {

	public static final int DEFAULT_SIZE = 20;

	public static final int MAX_SIZE = 100;

	public PageQuery {
		page = Math.max(0, page);
		size = size <= 0 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
	}

	public static PageQuery of(@Nullable Integer page, @Nullable Integer size) {
		return new PageQuery(page == null ? 0 : page, size == null ? DEFAULT_SIZE : size);
	}

	public long offset() {
		return (long) this.page * this.size;
	}

}
