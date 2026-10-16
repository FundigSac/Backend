package com.fundigsac.catalog.infrastructure;

import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import com.fundigsac.catalog.domain.TaxonomyRepository;
import com.fundigsac.shared.infrastructure.SqlTypes;

/** Adaptador JDBC de categorías y sectores. */
@Repository
class JdbcTaxonomyRepository implements TaxonomyRepository {

	private final JdbcClient jdbc;

	JdbcTaxonomyRepository(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	@Override
	public Optional<CategoryRef> findCategory(String slug) {
		return this.jdbc.sql("SELECT id, slug FROM product_categories WHERE slug = :slug")
			.param("slug", slug)
			.query((rs, n) -> new CategoryRef(SqlTypes.uuid(rs, "id"), rs.getString("slug")))
			.optional();
	}

	@Override
	public Optional<CategoryRef> findCategoryById(UUID id) {
		return this.jdbc.sql("SELECT id, slug FROM product_categories WHERE id = :id")
			.param("id", id)
			.query((rs, n) -> new CategoryRef(SqlTypes.uuid(rs, "id"), rs.getString("slug")))
			.optional();
	}

	@Override
	public Optional<UUID> findSectorId(String slug) {
		return this.jdbc.sql("SELECT id FROM industry_sectors WHERE slug = :slug").param("slug", slug).query(UUID.class).optional();
	}

}
