package com.fundigsac.catalog.infrastructure;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import com.fundigsac.catalog.domain.DocumentRepository;
import com.fundigsac.catalog.domain.TechnicalDocument;
import com.fundigsac.shared.domain.Slug;
import com.fundigsac.shared.domain.StaleVersionException;
import com.fundigsac.shared.infrastructure.SqlTypes;

/** Adaptador JDBC de documentos técnicos (con su vínculo a productos). */
@Repository
class JdbcDocumentRepository implements DocumentRepository {

	private final JdbcClient jdbc;

	JdbcDocumentRepository(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	@Override
	public Optional<TechnicalDocument> findById(UUID id) {
		var productIds = new HashSet<>(this.jdbc.sql("SELECT product_id FROM product_documents WHERE document_id = :id")
			.param("id", id)
			.query(UUID.class)
			.list());
		return this.jdbc.sql("SELECT * FROM technical_documents WHERE id = :id").param("id", id).query((rs, n) -> {
			String url = rs.getString("file_url");
			TechnicalDocument.FileReference file = url == null ? null
					: new TechnicalDocument.FileReference(url, rs.getString("media_type"), rs.getLong("size_bytes"),
							rs.getString("sha256"));
			TechnicalDocument.Content content = new TechnicalDocument.Content(new Slug(rs.getString("slug")),
					rs.getString("code"), rs.getString("title"), rs.getString("description"),
					TechnicalDocument.Type.valueOf(rs.getString("doc_type")), SqlTypes.textArray(rs, "families"),
					TechnicalDocument.Availability.valueOf(rs.getString("availability")), file, rs.getString("revision"),
					rs.getString("language"), productIds);
			return TechnicalDocument.rehydrate(SqlTypes.uuid(rs, "id"), content,
					TechnicalDocument.Status.valueOf(rs.getString("status")), SqlTypes.instant(rs, "published_at"),
					SqlTypes.instant(rs, "created_at"), SqlTypes.instant(rs, "updated_at"), rs.getLong("version"));
		}).optional();
	}

	@Override
	public boolean slugTaken(String slug, UUID excludingId) {
		return this.jdbc.sql("SELECT EXISTS (SELECT 1 FROM technical_documents WHERE slug = :slug AND id <> :id)")
			.param("slug", slug)
			.param("id", excludingId)
			.query(Boolean.class)
			.single();
	}

	@Override
	public void insert(TechnicalDocument d) {
		this.jdbc.sql("""
				INSERT INTO technical_documents (id, slug, code, title, description, doc_type, families, availability,
				    file_url, media_type, size_bytes, sha256, revision, language, status, published_at, created_at,
				    updated_at, version)
				VALUES (:id, :slug, :code, :title, :description, :type, CAST(:families AS text[]), :availability, :fileUrl,
				    :mediaType, :sizeBytes, :sha256, :revision, :language, :status, :publishedAt, :createdAt, :updatedAt, 0)""")
			.params(columns(d))
			.param("createdAt", SqlTypes.ts(d.createdAt()))
			.update();
		writeProducts(d);
	}

	@Override
	public void update(TechnicalDocument d) {
		int rows = this.jdbc.sql("""
				UPDATE technical_documents SET slug = :slug, code = :code, title = :title, description = :description,
				    doc_type = :type, families = CAST(:families AS text[]), availability = :availability,
				    file_url = :fileUrl, media_type = :mediaType, size_bytes = :sizeBytes, sha256 = :sha256,
				    revision = :revision, language = :language, status = :status, published_at = :publishedAt,
				    updated_at = :updatedAt, version = version + 1
				 WHERE id = :id AND version = :version""")
			.params(columns(d))
			.param("version", d.version())
			.update();
		if (rows != 1) {
			throw new StaleVersionException();
		}
		this.jdbc.sql("DELETE FROM product_documents WHERE document_id = :id").param("id", d.id()).update();
		writeProducts(d);
	}

	private static Map<String, Object> columns(TechnicalDocument d) {
		TechnicalDocument.Content c = d.content();
		Map<String, Object> m = new HashMap<>();
		m.put("id", d.id());
		m.put("slug", c.slug().value());
		m.put("code", c.code());
		m.put("title", c.title());
		m.put("description", c.description());
		m.put("type", c.type().name());
		m.put("families", SqlTypes.texts(c.families()));
		m.put("availability", c.availability().name());
		m.put("fileUrl", c.file() == null ? null : c.file().url());
		m.put("mediaType", c.file() == null ? null : c.file().mediaType());
		m.put("sizeBytes", c.file() == null ? null : c.file().sizeBytes());
		m.put("sha256", c.file() == null ? null : c.file().sha256());
		m.put("revision", c.revision());
		m.put("language", c.language());
		m.put("status", d.status().name());
		m.put("publishedAt", SqlTypes.ts(d.publishedAt()));
		m.put("updatedAt", SqlTypes.ts(d.updatedAt()));
		return m;
	}

	private void writeProducts(TechnicalDocument d) {
		for (UUID productId : d.content().productIds()) {
			this.jdbc.sql("INSERT INTO product_documents (product_id, document_id) VALUES (:productId, :documentId)")
				.param("productId", productId)
				.param("documentId", d.id())
				.update();
		}
	}

}
