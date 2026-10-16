package com.fundigsac.cart.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import com.fundigsac.shared.domain.Guard;
import com.fundigsac.shared.domain.Ids;
import com.fundigsac.shared.domain.NotFoundException;
import com.fundigsac.shared.domain.RuleViolationException;
import com.fundigsac.shared.domain.ValidationException;

/**
 * Carrito de cotización (agregado, uno por cliente). Una misma pieza y medida se acumula en una sola línea; cantidad
 * de 1 a 999 y como máximo 50 líneas (mismos límites que el carrito del sitio).
 */
public final class QuoteCart {

	public static final int MAX_ITEMS = 50;

	public static final int MAX_QUANTITY = 999;

	public record Item(UUID id, UUID productId, String productSlug, String productName, @Nullable String variantLabel,
			int quantity, @Nullable String note, Instant addedAt) {

		public Item {
			if (quantity < 1 || quantity > MAX_QUANTITY) {
				throw new ValidationException("quantity", "OUT_OF_RANGE", "La cantidad debe estar entre 1 y " + MAX_QUANTITY + ".");
			}
			note = Guard.optionalText("note", note, 500);
		}

		boolean sameLine(String slug, @Nullable String variant) {
			return this.productSlug.equals(slug) && Objects.equals(this.variantLabel, variant);
		}

	}

	private final UUID ownerId;

	private final List<Item> items;

	private Instant updatedAt;

	private final long version;

	private final boolean persisted;

	private QuoteCart(UUID ownerId, List<Item> items, Instant updatedAt, long version, boolean persisted) {
		this.ownerId = ownerId;
		this.items = new ArrayList<>(items);
		this.updatedAt = updatedAt;
		this.version = version;
		this.persisted = persisted;
	}

	public static QuoteCart empty(UUID ownerId, Instant now) {
		return new QuoteCart(ownerId, List.of(), now, 0, false);
	}

	public static QuoteCart rehydrate(UUID ownerId, List<Item> items, Instant updatedAt, long version) {
		return new QuoteCart(ownerId, items, updatedAt, version, true);
	}

	/** Agrega o acumula (hasta el máximo) la pieza y medida indicadas. */
	public Item add(UUID productId, String slug, String name, @Nullable String variant, int quantity, @Nullable String note,
			Instant now) {
		String cleanVariant = Guard.optionalText("variantLabel", variant, 120);
		for (int i = 0; i < this.items.size(); i++) {
			Item existing = this.items.get(i);
			if (existing.sameLine(slug, cleanVariant)) {
				Item merged = new Item(existing.id(), productId, slug, name, cleanVariant,
						Math.min(MAX_QUANTITY, existing.quantity() + quantity), note == null ? existing.note() : note,
						existing.addedAt());
				this.items.set(i, merged);
				this.updatedAt = now;
				return merged;
			}
		}
		if (this.items.size() >= MAX_ITEMS) {
			throw new RuleViolationException("CART_FULL", "El carrito admite como máximo " + MAX_ITEMS + " productos.");
		}
		Item item = new Item(Ids.newId(), productId, slug, name, cleanVariant, quantity, note, now);
		this.items.add(item);
		this.updatedAt = now;
		return item;
	}

	public Item update(UUID itemId, int quantity, @Nullable String note, Instant now) {
		for (int i = 0; i < this.items.size(); i++) {
			Item existing = this.items.get(i);
			if (existing.id().equals(itemId)) {
				Item changed = new Item(existing.id(), existing.productId(), existing.productSlug(), existing.productName(),
						existing.variantLabel(), quantity, note, existing.addedAt());
				this.items.set(i, changed);
				this.updatedAt = now;
				return changed;
			}
		}
		throw new NotFoundException("CART_ITEM_NOT_FOUND", "El producto no está en el carrito.");
	}

	public void remove(UUID itemId, Instant now) {
		if (!this.items.removeIf((i) -> i.id().equals(itemId))) {
			throw new NotFoundException("CART_ITEM_NOT_FOUND", "El producto no está en el carrito.");
		}
		this.updatedAt = now;
	}

	public void clear(Instant now) {
		this.items.clear();
		this.updatedAt = now;
	}

	public UUID ownerId() {
		return this.ownerId;
	}

	public List<Item> items() {
		return List.copyOf(this.items);
	}

	public Instant updatedAt() {
		return this.updatedAt;
	}

	public long version() {
		return this.version;
	}

	public boolean persisted() {
		return this.persisted;
	}

}
