package com.fundigsac.catalog.infrastructure;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import com.fundigsac.catalog.application.CatalogReadModel;
import com.fundigsac.catalog.application.CatalogViews.CatalogFacets;
import com.fundigsac.catalog.application.CatalogViews.CategoryRef;
import com.fundigsac.catalog.application.CatalogViews.CategoryView;
import com.fundigsac.catalog.application.CatalogViews.DnRangeView;
import com.fundigsac.catalog.application.CatalogViews.DocumentRef;
import com.fundigsac.catalog.application.CatalogViews.DocumentView;
import com.fundigsac.catalog.application.CatalogViews.FacetValue;
import com.fundigsac.catalog.application.CatalogViews.FileView;
import com.fundigsac.catalog.application.CatalogViews.ImageView;
import com.fundigsac.catalog.application.CatalogViews.LabeledCode;
import com.fundigsac.catalog.application.CatalogViews.ProductDetailView;
import com.fundigsac.catalog.application.CatalogViews.ProductSummaryView;
import com.fundigsac.catalog.application.CatalogViews.SectorRef;
import com.fundigsac.catalog.application.CatalogViews.SectorView;
import com.fundigsac.catalog.application.CatalogViews.SpecView;
import com.fundigsac.catalog.application.CatalogViews.VariantView;
import com.fundigsac.catalog.domain.Families;
import com.fundigsac.catalog.domain.Homologation;
import com.fundigsac.catalog.domain.JointType;
import com.fundigsac.shared.application.PageQuery;
import com.fundigsac.shared.application.PageResult;
import com.fundigsac.shared.infrastructure.SqlTypes;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Consultas de lectura del catálogo con SQL explícito: filtros que usan los índices (GIN de búsqueda, homologaciones y
 * PN), búsqueda de texto completo en español (sin tildes, con raíces: "válvulas" encuentra "valvula") y paginación con
 * orden determinista.
 */
@Repository
class JdbcCatalogReadModel implements CatalogReadModel {

	private static final String SUMMARY_COLUMNS = """
			p.id, p.slug, p.sku, p.name, p.summary, c.slug AS category_slug, c.name AS category_name, p.family,
			p.material, p.joint_type, p.dn_min_mm, p.dn_max_mm, p.pressure_ratings, p.standards, p.homologations,
			p.images -> 0 AS first_image, p.data_verified, p.status,
			(SELECT count(*) FROM product_variants v WHERE v.product_id = p.id) AS variant_count""";

	private static final TypeReference<List<Map<String, Object>>> JSON_LIST = new TypeReference<>() {
	};

	private static final TypeReference<Map<String, Object>> JSON_OBJECT = new TypeReference<>() {
	};

	private final JdbcClient jdbc;

	private final JsonMapper json;

	JdbcCatalogReadModel(JdbcClient jdbc, JsonMapper json) {
		this.jdbc = jdbc;
		this.json = json;
	}

	@Override
	public List<CategoryView> categories() {
		return this.jdbc.sql("""
				SELECT c.slug, c.name, c.description,
				       count(p.id) FILTER (WHERE p.status = 'PUBLISHED') AS product_count
				  FROM product_categories c LEFT JOIN products p ON p.category_id = c.id
				 WHERE c.visible
				 GROUP BY c.id ORDER BY c.position, c.name""")
			.query((rs, n) -> new CategoryView(rs.getString("slug"), rs.getString("name"), rs.getString("description"),
					rs.getLong("product_count")))
			.list();
	}

	@Override
	public PageResult<ProductSummaryView> searchProducts(ProductSearch search, PageQuery page) {
		Map<String, Object> params = new HashMap<>();
		String where = productFilters(search, params);
		String from = " FROM products p JOIN product_categories c ON c.id = p.category_id" + where;
		long total = this.jdbc.sql("SELECT count(*)" + from).params(params).query(Long.class).single();
		params.put("limit", page.size());
		params.put("offset", page.offset());
		List<ProductSummaryView> items = this.jdbc
			.sql("SELECT " + SUMMARY_COLUMNS + from + orderBy(search) + " LIMIT :limit OFFSET :offset")
			.params(params)
			.query(this::summary)
			.list();
		return PageResult.of(items, page, total);
	}

	@Override
	public Optional<ProductDetailView> productBySlug(String slug, boolean publishedOnly) {
		return product("p.slug = :key" + (publishedOnly ? " AND p.status = 'PUBLISHED' AND c.visible" : ""), slug,
				publishedOnly);
	}

	@Override
	public Optional<ProductDetailView> productById(UUID id) {
		return product("p.id = :key", id, false);
	}

	@Override
	public CatalogFacets facets(@Nullable String category) {
		Map<String, Object> params = new HashMap<>();
		String base = "WITH base AS (SELECT p.* FROM products p JOIN product_categories c ON c.id = p.category_id"
				+ " WHERE p.status = 'PUBLISHED' AND c.visible";
		if (category != null) {
			base += " AND c.slug = :category";
			params.put("category", category);
		}
		base += ") ";
		List<FacetValue> categories = this.jdbc.sql("""
				SELECT c.slug, c.name, count(p.id) AS n
				  FROM product_categories c JOIN products p ON p.category_id = c.id AND p.status = 'PUBLISHED'
				 WHERE c.visible GROUP BY c.id ORDER BY c.position""")
			.query((rs, n) -> new FacetValue(rs.getString("slug"), rs.getString("name"), rs.getLong("n")))
			.list();
		List<FacetValue> families = this.jdbc.sql(base + "SELECT family, count(*) AS n FROM base GROUP BY family ORDER BY n DESC, family")
			.params(params)
			.query((rs, n) -> new FacetValue(rs.getString("family"), Families.label(rs.getString("family")), rs.getLong("n")))
			.list();
		List<FacetValue> joints = this.jdbc
			.sql(base + "SELECT joint_type, count(*) AS n FROM base WHERE joint_type IS NOT NULL GROUP BY joint_type ORDER BY n DESC, joint_type")
			.params(params)
			.query((rs, n) -> new FacetValue(rs.getString("joint_type"), jointLabel(rs.getString("joint_type")), rs.getLong("n")))
			.list();
		List<FacetValue> homologations = this.jdbc
			.sql(base + "SELECT h, count(*) AS n FROM base, unnest(homologations) AS h GROUP BY h ORDER BY n DESC, h")
			.params(params)
			.query((rs, n) -> new FacetValue(rs.getString("h"), Homologation.fromCode(rs.getString("h")).label(), rs.getLong("n")))
			.list();
		List<FacetValue> pressure = this.jdbc.sql(base + """
				SELECT pn, count(DISTINCT id) AS n FROM (
				    SELECT id, unnest(pressure_ratings)::int AS pn FROM base
				    UNION ALL
				    SELECT b.id, v.pn::int FROM base b JOIN product_variants v ON v.product_id = b.id WHERE v.pn IS NOT NULL
				) x GROUP BY pn ORDER BY pn""")
			.params(params)
			.query((rs, n) -> new FacetValue(Integer.toString(rs.getInt("pn")), "PN " + rs.getInt("pn"), rs.getLong("n")))
			.list();
		List<FacetValue> sectors = this.jdbc.sql(base + """
				SELECT s.slug, s.name, count(*) AS n
				  FROM industry_sectors s JOIN product_sectors ps ON ps.sector_id = s.id JOIN base b ON b.id = ps.product_id
				 GROUP BY s.id ORDER BY s.position""")
			.params(params)
			.query((rs, n) -> new FacetValue(rs.getString("slug"), rs.getString("name"), rs.getLong("n")))
			.list();
		DnRangeView dn = this.jdbc.sql(base + """
				SELECT min(dn) AS min_dn, max(dn) AS max_dn FROM (
				    SELECT dn_min_mm AS dn FROM base UNION ALL SELECT dn_max_mm FROM base
				    UNION ALL SELECT v.dn_mm FROM base b JOIN product_variants v ON v.product_id = b.id
				) x WHERE dn IS NOT NULL""")
			.params(params)
			.query((rs, n) -> {
				Integer min = SqlTypes.nullableInt(rs, "min_dn");
				return min == null ? null : new DnRangeView(min, rs.getInt("max_dn"));
			})
			.optional()
			.orElse(null);
		return new CatalogFacets(categories, families, joints, homologations, pressure, sectors, dn);
	}

	@Override
	public List<SectorView> sectors() {
		return this.jdbc.sql("""
				SELECT s.slug, s.name, s.description,
				       count(p.id) FILTER (WHERE p.status = 'PUBLISHED') AS product_count
				  FROM industry_sectors s
				  LEFT JOIN product_sectors ps ON ps.sector_id = s.id
				  LEFT JOIN products p ON p.id = ps.product_id
				 GROUP BY s.id ORDER BY s.position, s.name""")
			.query((rs, n) -> new SectorView(rs.getString("slug"), rs.getString("name"), rs.getString("description"),
					rs.getLong("product_count")))
			.list();
	}

	@Override
	public PageResult<DocumentView> documents(DocumentSearch search, PageQuery page) {
		Map<String, Object> params = new HashMap<>();
		StringBuilder where = new StringBuilder(" WHERE true");
		if (search.publishedOnly()) {
			where.append(" AND d.status = 'PUBLISHED'");
		}
		if (search.type() != null) {
			where.append(" AND d.doc_type = :type");
			params.put("type", search.type());
		}
		if (search.family() != null) {
			where.append(" AND :family = ANY(d.families)");
			params.put("family", search.family());
		}
		if (search.product() != null) {
			where.append(" AND EXISTS (SELECT 1 FROM product_documents pd JOIN products p ON p.id = pd.product_id"
					+ " WHERE pd.document_id = d.id AND p.slug = :product)");
			params.put("product", search.product());
		}
		if (search.query() != null) {
			where.append(" AND translate(lower(d.title || ' ' || coalesce(d.description, '') || ' ' || coalesce(d.code, '')),"
					+ " 'áéíóúüñ', 'aeiouun') LIKE :qLike ESCAPE '\\'");
			params.put("qLike", "%" + escapeLike(search.query()) + "%");
		}
		long total = this.jdbc.sql("SELECT count(*) FROM technical_documents d" + where).params(params).query(Long.class).single();
		params.put("limit", page.size());
		params.put("offset", page.offset());
		params.put("publishedProducts", search.publishedOnly());
		List<DocumentView> items = this.jdbc
			.sql(documentSelect() + where + " ORDER BY d.doc_type, d.title, d.id LIMIT :limit OFFSET :offset")
			.params(params)
			.query(this::document)
			.list();
		return PageResult.of(items, page, total);
	}

	@Override
	public Optional<DocumentView> documentBySlug(String slug, boolean publishedOnly) {
		return this.jdbc.sql(documentSelect() + " WHERE d.slug = :slug" + (publishedOnly ? " AND d.status = 'PUBLISHED'" : ""))
			.param("slug", slug)
			.param("publishedProducts", publishedOnly)
			.query(this::document)
			.optional();
	}

	@Override
	public Optional<DocumentView> documentById(UUID id) {
		return this.jdbc.sql(documentSelect() + " WHERE d.id = :id")
			.param("id", id)
			.param("publishedProducts", false)
			.query(this::document)
			.optional();
	}

	// ----- productos -----

	private Optional<ProductDetailView> product(String condition, Object key, boolean publishedOnly) {
		Optional<ProductRow> row = this.jdbc.sql("""
				SELECT p.*, c.slug AS category_slug, c.name AS category_name
				  FROM products p JOIN product_categories c ON c.id = p.category_id
				 WHERE\s""" + condition)
			.param("key", key)
			.query((rs, n) -> new ProductRow(rs))
			.optional();
		return row.map((r) -> detail(r, publishedOnly));
	}

	private ProductDetailView detail(ProductRow r, boolean publishedOnly) {
		List<VariantView> variants = this.jdbc
			.sql("SELECT id, sku, label, dn_mm, pn FROM product_variants WHERE product_id = :id ORDER BY position, label")
			.param("id", r.id)
			.query((rs, n) -> new VariantView(SqlTypes.uuid(rs, "id"), rs.getString("sku"), rs.getString("label"),
					SqlTypes.nullableInt(rs, "dn_mm"), SqlTypes.nullableInt(rs, "pn")))
			.list();
		List<SectorRef> sectors = this.jdbc.sql("""
				SELECT s.slug, s.name FROM product_sectors ps JOIN industry_sectors s ON s.id = ps.sector_id
				 WHERE ps.product_id = :id ORDER BY s.position""")
			.param("id", r.id)
			.query((rs, n) -> new SectorRef(rs.getString("slug"), rs.getString("name")))
			.list();
		List<DocumentRef> documents = this.jdbc.sql("""
				SELECT d.slug, d.title, d.doc_type, d.availability
				  FROM product_documents pd JOIN technical_documents d ON d.id = pd.document_id
				 WHERE pd.product_id = :id""" + (publishedOnly ? " AND d.status = 'PUBLISHED'" : "") + " ORDER BY d.doc_type, d.title")
			.param("id", r.id)
			.query((rs, n) -> new DocumentRef(rs.getString("slug"), rs.getString("title"), rs.getString("doc_type"),
					rs.getString("availability")))
			.list();
		List<SpecView> specs = readList(r.specs).stream()
			.map((m) -> new SpecView(String.valueOf(m.get("label")), String.valueOf(m.get("value"))))
			.toList();
		List<ImageView> images = readList(r.images).stream().map(JdbcCatalogReadModel::image).toList();
		return new ProductDetailView(r.id, r.slug, r.sku, r.name, r.summary, r.description,
				new CategoryRef(r.categorySlug, r.categoryName), family(r.family), r.material, joint(r.joint), r.dn,
				r.pressure, r.standards, homologations(r.homologations), specs, images, variants, sectors, documents,
				r.sourceReference, r.dataVerified, r.status, r.reviewNote, r.publishedAt, r.updatedAt, r.version);
	}

	private ProductSummaryView summary(ResultSet rs, int row) throws SQLException {
		Integer dnMin = SqlTypes.nullableInt(rs, "dn_min_mm");
		String image = rs.getString("first_image");
		return new ProductSummaryView(SqlTypes.uuid(rs, "id"), rs.getString("slug"), rs.getString("sku"),
				rs.getString("name"), rs.getString("summary"),
				new CategoryRef(rs.getString("category_slug"), rs.getString("category_name")), family(rs.getString("family")),
				rs.getString("material"), joint(rs.getString("joint_type")),
				dnMin == null ? null : new DnRangeView(dnMin, rs.getInt("dn_max_mm")), SqlTypes.intArray(rs, "pressure_ratings"),
				SqlTypes.textArray(rs, "standards"), homologations(SqlTypes.textArray(rs, "homologations")),
				image == null ? null : image(this.json.readValue(image, JSON_OBJECT)), rs.getInt("variant_count"),
				rs.getBoolean("data_verified"), rs.getString("status"));
	}

	private static String productFilters(ProductSearch s, Map<String, Object> params) {
		StringBuilder where = new StringBuilder(" WHERE true");
		if (s.publishedOnly()) {
			where.append(" AND p.status = 'PUBLISHED' AND c.visible");
		}
		else if (s.status() != null) {
			where.append(" AND p.status = :status");
			params.put("status", s.status());
		}
		if (s.category() != null) {
			where.append(" AND c.slug = :category");
			params.put("category", s.category());
		}
		if (s.family() != null) {
			where.append(" AND p.family = :family");
			params.put("family", s.family());
		}
		if (s.joint() != null) {
			where.append(" AND p.joint_type = :joint");
			params.put("joint", s.joint());
		}
		if (s.homologation() != null) {
			where.append(" AND :homologation = ANY(p.homologations)");
			params.put("homologation", s.homologation());
		}
		if (s.pn() != null) {
			where.append(" AND (:pn = ANY(p.pressure_ratings)"
					+ " OR EXISTS (SELECT 1 FROM product_variants v WHERE v.product_id = p.id AND v.pn = :pn))");
			params.put("pn", s.pn());
		}
		if (s.dn() != null) {
			where.append(" AND ((p.dn_min_mm <= :dn AND p.dn_max_mm >= :dn)"
					+ " OR EXISTS (SELECT 1 FROM product_variants v WHERE v.product_id = p.id AND v.dn_mm = :dn))");
			params.put("dn", s.dn());
		}
		if (s.sector() != null) {
			where.append(" AND EXISTS (SELECT 1 FROM product_sectors ps JOIN industry_sectors s ON s.id = ps.sector_id"
					+ " WHERE ps.product_id = p.id AND s.slug = :sector)");
			params.put("sector", s.sector());
		}
		if (s.query() != null) {
			where.append(" AND (p.search_vector @@ websearch_to_tsquery('spanish', :q) OR p.search_text LIKE :qLike ESCAPE '\\')");
			params.put("q", s.query());
			params.put("qLike", "%" + escapeLike(s.query()) + "%");
		}
		return where.toString();
	}

	private static String orderBy(ProductSearch s) {
		return switch (s.sort()) {
			case RELEVANCE -> s.query() == null ? " ORDER BY p.name, p.id"
					: " ORDER BY ts_rank(p.search_vector, websearch_to_tsquery('spanish', :q)) DESC, p.name, p.id";
			case NEWEST -> " ORDER BY p.published_at DESC NULLS LAST, p.id";
			case NAME -> " ORDER BY p.name, p.id";
		};
	}

	// ----- documentos -----

	private static String documentSelect() {
		return """
				SELECT d.*, COALESCE((SELECT array_agg(p.slug ORDER BY p.slug)
				                        FROM product_documents pd JOIN products p ON p.id = pd.product_id
				                       WHERE pd.document_id = d.id AND (NOT :publishedProducts OR p.status = 'PUBLISHED')),
				                     '{}') AS product_slugs
				  FROM technical_documents d""";
	}

	private DocumentView document(ResultSet rs, int row) throws SQLException {
		String url = rs.getString("file_url");
		FileView file = url == null ? null
				: new FileView(url, rs.getString("media_type"), rs.getLong("size_bytes"), rs.getString("sha256"));
		return new DocumentView(SqlTypes.uuid(rs, "id"), rs.getString("slug"), rs.getString("code"), rs.getString("title"),
				rs.getString("description"), rs.getString("doc_type"), SqlTypes.textArray(rs, "families"),
				rs.getString("availability"), file, rs.getString("revision"), rs.getString("language"),
				SqlTypes.textArray(rs, "product_slugs"), rs.getString("status"), SqlTypes.instant(rs, "published_at"),
				rs.getLong("version"));
	}

	// ----- utilidades -----

	private List<Map<String, Object>> readList(String jsonArray) {
		return jsonArray == null ? List.of() : this.json.readValue(jsonArray, JSON_LIST);
	}

	private static ImageView image(Map<String, Object> m) {
		return new ImageView(String.valueOf(m.get("url")), String.valueOf(m.getOrDefault("alt", "")),
				Boolean.TRUE.equals(m.get("illustrative")));
	}

	private static LabeledCode family(String code) {
		return new LabeledCode(code, Families.label(code));
	}

	private static @Nullable LabeledCode joint(@Nullable String code) {
		return code == null ? null : new LabeledCode(code, jointLabel(code));
	}

	private static String jointLabel(String code) {
		JointType joint = JointType.fromCode(code);
		return joint == null ? code : joint.label();
	}

	private static List<LabeledCode> homologations(List<String> codes) {
		List<LabeledCode> result = new ArrayList<>();
		codes.forEach((c) -> result.add(new LabeledCode(c, Homologation.fromCode(c).label())));
		return result;
	}

	static String escapeLike(String value) {
		return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
	}

	/** Fila de producto leída de una vez (el ResultSet no se usa fuera del mapeo). */
	private static final class ProductRow {

		final UUID id;

		final String slug;

		final @Nullable String sku;

		final String name;

		final String summary;

		final @Nullable String description;

		final String categorySlug;

		final String categoryName;

		final String family;

		final @Nullable String material;

		final @Nullable String joint;

		final @Nullable DnRangeView dn;

		final List<Integer> pressure;

		final List<String> standards;

		final List<String> homologations;

		final String specs;

		final String images;

		final @Nullable String sourceReference;

		final boolean dataVerified;

		final String status;

		final @Nullable String reviewNote;

		final java.time.@Nullable Instant publishedAt;

		final java.time.Instant updatedAt;

		final long version;

		ProductRow(ResultSet rs) throws SQLException {
			this.id = SqlTypes.uuid(rs, "id");
			this.slug = rs.getString("slug");
			this.sku = rs.getString("sku");
			this.name = rs.getString("name");
			this.summary = rs.getString("summary");
			this.description = rs.getString("description");
			this.categorySlug = rs.getString("category_slug");
			this.categoryName = rs.getString("category_name");
			this.family = rs.getString("family");
			this.material = rs.getString("material");
			this.joint = rs.getString("joint_type");
			Integer dnMin = SqlTypes.nullableInt(rs, "dn_min_mm");
			this.dn = dnMin == null ? null : new DnRangeView(dnMin, rs.getInt("dn_max_mm"));
			this.pressure = SqlTypes.intArray(rs, "pressure_ratings");
			this.standards = SqlTypes.textArray(rs, "standards");
			this.homologations = SqlTypes.textArray(rs, "homologations");
			this.specs = rs.getString("specs");
			this.images = rs.getString("images");
			this.sourceReference = rs.getString("source_reference");
			this.dataVerified = rs.getBoolean("data_verified");
			this.status = rs.getString("status");
			this.reviewNote = rs.getString("review_note");
			this.publishedAt = SqlTypes.instant(rs, "published_at");
			this.updatedAt = SqlTypes.instant(rs, "updated_at");
			this.version = rs.getLong("version");
		}

	}

}
