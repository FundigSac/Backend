package com.fundigsac.catalog.infrastructure;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import com.fundigsac.catalog.domain.DnRange;
import com.fundigsac.catalog.domain.Homologation;
import com.fundigsac.catalog.domain.JointType;
import com.fundigsac.catalog.domain.Product;
import com.fundigsac.catalog.domain.ProductDetails;
import com.fundigsac.catalog.domain.ProductImage;
import com.fundigsac.catalog.domain.ProductRepository;
import com.fundigsac.catalog.domain.ProductStatus;
import com.fundigsac.catalog.domain.Sku;
import com.fundigsac.catalog.domain.TechnicalSpec;
import com.fundigsac.catalog.domain.Variant;
import com.fundigsac.shared.domain.Slug;
import com.fundigsac.shared.domain.StaleVersionException;
import com.fundigsac.shared.infrastructure.SqlTypes;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/** Adaptador JDBC del agregado Product (ficha + variantes + sectores) con bloqueo optimista. */
@Repository
class JdbcProductRepository implements ProductRepository {

	private static final TypeReference<List<Map<String, Object>>> JSON_LIST = new TypeReference<>() {
	};

	private final JdbcClient jdbc;

	private final JsonMapper json;

	JdbcProductRepository(JdbcClient jdbc, JsonMapper json) {
		this.jdbc = jdbc;
		this.json = json;
	}

	@Override
	public Optional<Product> findById(UUID id) {
		Optional<Row> row = this.jdbc.sql("SELECT * FROM products WHERE id = :id").param("id", id).query(this::row).optional();
		if (row.isEmpty()) {
			return Optional.empty();
		}
		List<Variant> variants = this.jdbc
			.sql("SELECT id, sku, label, dn_mm, pn FROM product_variants WHERE product_id = :id ORDER BY position, label")
			.param("id", id)
			.query((rs, n) -> new Variant(SqlTypes.uuid(rs, "id"), Sku.optional(rs.getString("sku")), rs.getString("label"),
					SqlTypes.nullableInt(rs, "dn_mm"), SqlTypes.nullableInt(rs, "pn")))
			.list();
		Set<UUID> sectors = new HashSet<>(this.jdbc.sql("SELECT sector_id FROM product_sectors WHERE product_id = :id")
			.param("id", id)
			.query(UUID.class)
			.list());
		Row r = row.get();
		ProductDetails details = new ProductDetails(r.categoryId, new Slug(r.slug), Sku.optional(r.sku), r.name, r.summary,
				r.description, r.family, r.material, JointType.fromCode(r.joint), DnRange.of(r.dnMin, r.dnMax), r.pressure,
				r.standards, r.homologations, r.specs, r.images, sectors, r.sourceReference, r.dataVerified);
		return Optional.of(Product.rehydrate(r.id, details, variants, r.status, r.reviewNote, r.createdBy, r.submittedBy,
				r.submittedAt, r.publishedBy, r.publishedAt, r.createdAt, r.updatedAt, r.version));
	}

	@Override
	public boolean slugTaken(String slug, UUID excludingId) {
		return this.jdbc.sql("SELECT EXISTS (SELECT 1 FROM products WHERE slug = :slug AND id <> :id)")
			.param("slug", slug)
			.param("id", excludingId)
			.query(Boolean.class)
			.single();
	}

	@Override
	public boolean skuTaken(String sku, UUID excludingId) {
		return this.jdbc.sql("SELECT EXISTS (SELECT 1 FROM products WHERE sku = :sku AND id <> :id)")
			.param("sku", sku)
			.param("id", excludingId)
			.query(Boolean.class)
			.single();
	}

	@Override
	public void insert(Product p, String searchText) {
		ProductDetails d = p.details();
		this.jdbc.sql("""
				INSERT INTO products (id, category_id, slug, sku, name, summary, description, family, material, joint_type,
				    dn_min_mm, dn_max_mm, pressure_ratings, standards, homologations, specs, images, source_reference,
				    data_verified, status, review_note, search_text, created_by, submitted_by, submitted_at, published_by,
				    published_at, created_at, updated_at, version)
				VALUES (:id, :categoryId, :slug, :sku, :name, :summary, :description, :family, :material, :joint,
				    :dnMin, :dnMax, CAST(:pressure AS smallint[]), CAST(:standards AS text[]), CAST(:homologations AS text[]),
				    CAST(:specs AS jsonb), CAST(:images AS jsonb), :sourceReference, :dataVerified, :status, :reviewNote,
				    :searchText, :createdBy, :submittedBy, :submittedAt, :publishedBy, :publishedAt, :createdAt, :updatedAt, 0)""")
			.params(columns(p, d, searchText))
			.param("createdBy", p.createdBy())
			.param("createdAt", SqlTypes.ts(p.createdAt()))
			.update();
		writeChildren(p);
	}

	@Override
	public void update(Product p, String searchText) {
		ProductDetails d = p.details();
		int rows = this.jdbc.sql("""
				UPDATE products SET category_id = :categoryId, slug = :slug, sku = :sku, name = :name, summary = :summary,
				    description = :description, family = :family, material = :material, joint_type = :joint,
				    dn_min_mm = :dnMin, dn_max_mm = :dnMax, pressure_ratings = CAST(:pressure AS smallint[]),
				    standards = CAST(:standards AS text[]), homologations = CAST(:homologations AS text[]),
				    specs = CAST(:specs AS jsonb), images = CAST(:images AS jsonb), source_reference = :sourceReference,
				    data_verified = :dataVerified, status = :status, review_note = :reviewNote, search_text = :searchText,
				    submitted_by = :submittedBy, submitted_at = :submittedAt, published_by = :publishedBy,
				    published_at = :publishedAt, updated_at = :updatedAt, version = version + 1
				 WHERE id = :id AND version = :version""")
			.params(columns(p, d, searchText))
			.param("version", p.version())
			.update();
		if (rows != 1) {
			throw new StaleVersionException();
		}
		this.jdbc.sql("DELETE FROM product_variants WHERE product_id = :id").param("id", p.id()).update();
		this.jdbc.sql("DELETE FROM product_sectors WHERE product_id = :id").param("id", p.id()).update();
		writeChildren(p);
	}

	private Map<String, Object> columns(Product p, ProductDetails d, String searchText) {
		Map<String, Object> m = new java.util.HashMap<>();
		m.put("id", p.id());
		m.put("categoryId", d.categoryId());
		m.put("slug", d.slug().value());
		m.put("sku", d.sku() == null ? null : d.sku().value());
		m.put("name", d.name());
		m.put("summary", d.summary());
		m.put("description", d.description());
		m.put("family", d.family());
		m.put("material", d.material());
		m.put("joint", d.joint() == null ? null : d.joint().code());
		m.put("dnMin", d.dn() == null ? null : d.dn().minMm());
		m.put("dnMax", d.dn() == null ? null : d.dn().maxMm());
		m.put("pressure", SqlTypes.shorts(d.pressureRatings()));
		m.put("standards", SqlTypes.texts(d.standards()));
		m.put("homologations", d.homologations().stream().map(Homologation::code).sorted().toArray(String[]::new));
		m.put("specs", this.json.writeValueAsString(d.specs().stream().map((s) -> Map.of("label", s.label(), "value", s.value())).toList()));
		m.put("images", this.json.writeValueAsString(d.images()
			.stream()
			.map((i) -> Map.of("url", i.url(), "alt", i.alt(), "illustrative", i.illustrative()))
			.toList()));
		m.put("sourceReference", d.sourceReference());
		m.put("dataVerified", d.dataVerified());
		m.put("status", p.status().name());
		m.put("reviewNote", p.reviewNote());
		m.put("searchText", searchText);
		m.put("submittedBy", p.submittedBy());
		m.put("submittedAt", SqlTypes.ts(p.submittedAt()));
		m.put("publishedBy", p.publishedBy());
		m.put("publishedAt", SqlTypes.ts(p.publishedAt()));
		m.put("updatedAt", SqlTypes.ts(p.updatedAt()));
		return m;
	}

	private void writeChildren(Product p) {
		int position = 1;
		for (Variant v : p.variants()) {
			this.jdbc.sql("""
					INSERT INTO product_variants (id, product_id, sku, label, dn_mm, pn, position)
					VALUES (:id, :productId, :sku, :label, :dn, :pn, :position)""")
				.param("id", v.id())
				.param("productId", p.id())
				.param("sku", v.sku() == null ? null : v.sku().value())
				.param("label", v.label())
				.param("dn", v.dnMm())
				.param("pn", v.pn())
				.param("position", position++)
				.update();
		}
		for (UUID sector : p.details().sectorIds()) {
			this.jdbc.sql("INSERT INTO product_sectors (product_id, sector_id) VALUES (:productId, :sectorId)")
				.param("productId", p.id())
				.param("sectorId", sector)
				.update();
		}
	}

	private Row row(ResultSet rs, int n) throws SQLException {
		Row r = new Row();
		r.id = SqlTypes.uuid(rs, "id");
		r.categoryId = SqlTypes.uuid(rs, "category_id");
		r.slug = rs.getString("slug");
		r.sku = rs.getString("sku");
		r.name = rs.getString("name");
		r.summary = rs.getString("summary");
		r.description = rs.getString("description");
		r.family = rs.getString("family");
		r.material = rs.getString("material");
		r.joint = rs.getString("joint_type");
		r.dnMin = SqlTypes.nullableInt(rs, "dn_min_mm");
		r.dnMax = SqlTypes.nullableInt(rs, "dn_max_mm");
		r.pressure = SqlTypes.intArray(rs, "pressure_ratings");
		r.standards = SqlTypes.textArray(rs, "standards");
		r.homologations = EnumSet.noneOf(Homologation.class);
		SqlTypes.textArray(rs, "homologations").forEach((h) -> r.homologations.add(Homologation.fromCode(h)));
		r.specs = new ArrayList<>();
		this.json.readValue(rs.getString("specs"), JSON_LIST)
			.forEach((m) -> r.specs.add(new TechnicalSpec(String.valueOf(m.get("label")), String.valueOf(m.get("value")))));
		r.images = new ArrayList<>();
		this.json.readValue(rs.getString("images"), JSON_LIST)
			.forEach((m) -> r.images.add(new ProductImage(String.valueOf(m.get("url")), String.valueOf(m.get("alt")),
					Boolean.TRUE.equals(m.get("illustrative")))));
		r.sourceReference = rs.getString("source_reference");
		r.dataVerified = rs.getBoolean("data_verified");
		r.status = ProductStatus.valueOf(rs.getString("status"));
		r.reviewNote = rs.getString("review_note");
		r.createdBy = SqlTypes.uuid(rs, "created_by");
		r.submittedBy = SqlTypes.uuid(rs, "submitted_by");
		r.submittedAt = SqlTypes.instant(rs, "submitted_at");
		r.publishedBy = SqlTypes.uuid(rs, "published_by");
		r.publishedAt = SqlTypes.instant(rs, "published_at");
		r.createdAt = SqlTypes.instant(rs, "created_at");
		r.updatedAt = SqlTypes.instant(rs, "updated_at");
		r.version = rs.getLong("version");
		return r;
	}

	/** Fila intermedia (los campos se validan al construir ProductDetails). */
	@SuppressWarnings("NullAway")
	private static final class Row {

		UUID id;

		UUID categoryId;

		String slug;

		String sku;

		String name;

		String summary;

		String description;

		String family;

		String material;

		String joint;

		Integer dnMin;

		Integer dnMax;

		List<Integer> pressure;

		List<String> standards;

		Set<Homologation> homologations;

		List<TechnicalSpec> specs;

		List<ProductImage> images;

		String sourceReference;

		boolean dataVerified;

		ProductStatus status;

		String reviewNote;

		UUID createdBy;

		UUID submittedBy;

		java.time.Instant submittedAt;

		UUID publishedBy;

		java.time.Instant publishedAt;

		java.time.Instant createdAt;

		java.time.Instant updatedAt;

		long version;

	}

}
