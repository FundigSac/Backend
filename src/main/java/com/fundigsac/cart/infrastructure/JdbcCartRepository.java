package com.fundigsac.cart.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import com.fundigsac.cart.domain.CartRepository;
import com.fundigsac.cart.domain.QuoteCart;
import com.fundigsac.shared.domain.StaleVersionException;
import com.fundigsac.shared.infrastructure.SqlTypes;

/** Adaptador JDBC del carrito (cabecera con versión + líneas reescritas en cada cambio). */
@Repository
class JdbcCartRepository implements CartRepository {

	private final JdbcClient jdbc;

	JdbcCartRepository(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	@Override
	public Optional<QuoteCart> find(UUID ownerId) {
		return this.jdbc.sql("SELECT updated_at, version FROM quote_carts WHERE owner_id = :owner")
			.param("owner", ownerId)
			.query((rs, n) -> new Object[] { SqlTypes.instant(rs, "updated_at"), rs.getLong("version") })
			.optional()
			.map((row) -> QuoteCart.rehydrate(ownerId, items(ownerId), (java.time.Instant) row[0], (Long) row[1]));
	}

	@Override
	public void save(QuoteCart cart) {
		if (!cart.persisted()) {
			int inserted = this.jdbc.sql("""
					INSERT INTO quote_carts (owner_id, updated_at, version) VALUES (:owner, :updatedAt, 0)
					ON CONFLICT (owner_id) DO NOTHING""")
				.param("owner", cart.ownerId())
				.param("updatedAt", SqlTypes.ts(cart.updatedAt()))
				.update();
			if (inserted != 1) {
				throw new StaleVersionException();
			}
		}
		else {
			int rows = this.jdbc.sql("""
					UPDATE quote_carts SET updated_at = :updatedAt, version = version + 1
					 WHERE owner_id = :owner AND version = :version""")
				.param("updatedAt", SqlTypes.ts(cart.updatedAt()))
				.param("owner", cart.ownerId())
				.param("version", cart.version())
				.update();
			if (rows != 1) {
				throw new StaleVersionException();
			}
		}
		this.jdbc.sql("DELETE FROM quote_cart_items WHERE owner_id = :owner").param("owner", cart.ownerId()).update();
		int position = 1;
		for (QuoteCart.Item i : cart.items()) {
			this.jdbc.sql("""
					INSERT INTO quote_cart_items (id, owner_id, product_id, product_slug, product_name, variant_label,
					    quantity, note, position, added_at)
					VALUES (:id, :owner, :productId, :slug, :name, :variant, :quantity, :note, :position, :addedAt)""")
				.param("id", i.id())
				.param("owner", cart.ownerId())
				.param("productId", i.productId())
				.param("slug", i.productSlug())
				.param("name", i.productName())
				.param("variant", i.variantLabel())
				.param("quantity", i.quantity())
				.param("note", i.note())
				.param("position", position++)
				.param("addedAt", SqlTypes.ts(i.addedAt()))
				.update();
		}
	}

	@Override
	public void delete(UUID ownerId) {
		this.jdbc.sql("DELETE FROM quote_carts WHERE owner_id = :owner").param("owner", ownerId).update();
	}

	private List<QuoteCart.Item> items(UUID ownerId) {
		return this.jdbc.sql("SELECT * FROM quote_cart_items WHERE owner_id = :owner ORDER BY position")
			.param("owner", ownerId)
			.query((rs, n) -> new QuoteCart.Item(SqlTypes.uuid(rs, "id"), SqlTypes.uuid(rs, "product_id"),
					rs.getString("product_slug"), rs.getString("product_name"), rs.getString("variant_label"),
					rs.getInt("quantity"), rs.getString("note"), SqlTypes.instant(rs, "added_at")))
			.list();
	}

}
