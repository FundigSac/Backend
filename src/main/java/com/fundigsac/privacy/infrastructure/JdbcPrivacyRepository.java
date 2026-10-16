package com.fundigsac.privacy.infrastructure;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import com.fundigsac.privacy.domain.ConsentRecord;
import com.fundigsac.privacy.domain.DataSubjectRequest;
import com.fundigsac.privacy.domain.LegalNotice;
import com.fundigsac.privacy.domain.PrivacyRepository;
import com.fundigsac.shared.domain.Email;
import com.fundigsac.shared.domain.Folio;
import com.fundigsac.shared.domain.IdentityDocument;
import com.fundigsac.shared.domain.StaleVersionException;
import com.fundigsac.shared.infrastructure.SqlTypes;
import tools.jackson.databind.json.JsonMapper;

/** Adaptador JDBC de privacidad. Textos legales y consentimientos son de solo inserción. */
@Repository
class JdbcPrivacyRepository implements PrivacyRepository {

	private final JdbcClient jdbc;

	private final JsonMapper json;

	JdbcPrivacyRepository(JdbcClient jdbc, JsonMapper json) {
		this.jdbc = jdbc;
		this.json = json;
	}

	@Override
	public Optional<LegalNotice> currentNotice(LegalNotice.Type type, Instant at) {
		return this.jdbc.sql("""
				SELECT * FROM legal_notices WHERE notice_type = :type AND effective_from <= :at
				 ORDER BY effective_from DESC, published_at DESC LIMIT 1""")
			.param("type", type.name())
			.param("at", SqlTypes.ts(at))
			.query(JdbcPrivacyRepository::notice)
			.optional();
	}

	@Override
	public List<LegalNotice> noticeVersions(LegalNotice.Type type) {
		return this.jdbc.sql("SELECT * FROM legal_notices WHERE notice_type = :type ORDER BY effective_from DESC, published_at DESC")
			.param("type", type.name())
			.query(JdbcPrivacyRepository::notice)
			.list();
	}

	@Override
	public boolean noticeVersionExists(LegalNotice.Type type, String version) {
		return this.jdbc.sql("SELECT EXISTS (SELECT 1 FROM legal_notices WHERE notice_type = :type AND version = :version)")
			.param("type", type.name())
			.param("version", version)
			.query(Boolean.class)
			.single();
	}

	@Override
	public void insertNotice(LegalNotice n) {
		this.jdbc.sql("""
				INSERT INTO legal_notices (id, notice_type, version, title, summary, content_url, body, content_sha256,
				    effective_from, published_by, published_at)
				VALUES (:id, :type, :version, :title, :summary, :url, :body, :sha, :effectiveFrom, :publishedBy, :publishedAt)""")
			.param("id", n.id())
			.param("type", n.type().name())
			.param("version", n.version())
			.param("title", n.title())
			.param("summary", n.summary())
			.param("url", n.contentUrl())
			.param("body", n.body())
			.param("sha", n.contentSha256())
			.param("effectiveFrom", SqlTypes.ts(n.effectiveFrom()))
			.param("publishedBy", n.publishedBy())
			.param("publishedAt", SqlTypes.ts(n.publishedAt()))
			.update();
	}

	@Override
	public void insertConsent(ConsentRecord r) {
		this.jdbc.sql("""
				INSERT INTO consent_records (id, consent_id, choices, policy_version, source, recorded_at)
				VALUES (:id, :consentId, CAST(:choices AS jsonb), :policy, :source, :recordedAt)""")
			.param("id", r.id())
			.param("consentId", r.consentId())
			.param("choices", this.json.writeValueAsString(r.choices()))
			.param("policy", r.policyVersion())
			.param("source", r.source().name())
			.param("recordedAt", SqlTypes.ts(r.recordedAt()))
			.update();
	}

	@Override
	public void insertRequest(DataSubjectRequest r) {
		this.jdbc.sql("""
				INSERT INTO data_subject_requests (id, folio, request_type, full_name, email, document_type, document_number,
				    details, status, received_at, updated_at, version)
				VALUES (:id, :folio, :type, :name, :email, :docType, :docNumber, :details, :status, :receivedAt, :updatedAt, 0)""")
			.param("id", r.id())
			.param("folio", r.folio().value())
			.param("type", r.type().name())
			.param("name", r.fullName())
			.param("email", r.email().value())
			.param("docType", r.document() == null ? null : r.document().type().name())
			.param("docNumber", r.document() == null ? null : r.document().number())
			.param("details", r.details())
			.param("status", r.status().name())
			.param("receivedAt", SqlTypes.ts(r.receivedAt()))
			.param("updatedAt", SqlTypes.ts(r.updatedAt()))
			.update();
	}

	@Override
	public void updateRequest(DataSubjectRequest r) {
		int rows = this.jdbc.sql("""
				UPDATE data_subject_requests SET status = :status, resolution_note = :note, resolved_at = :resolvedAt,
				    resolved_by = :resolvedBy, updated_at = :updatedAt, version = version + 1
				 WHERE id = :id AND version = :version""")
			.param("status", r.status().name())
			.param("note", r.resolutionNote())
			.param("resolvedAt", SqlTypes.ts(r.resolvedAt()))
			.param("resolvedBy", r.resolvedBy())
			.param("updatedAt", SqlTypes.ts(r.updatedAt()))
			.param("id", r.id())
			.param("version", r.version())
			.update();
		if (rows != 1) {
			throw new StaleVersionException();
		}
	}

	@Override
	public Optional<DataSubjectRequest> findRequest(UUID id) {
		return this.jdbc.sql("SELECT * FROM data_subject_requests WHERE id = :id")
			.param("id", id)
			.query(JdbcPrivacyRepository::request)
			.optional();
	}

	@Override
	public Optional<DataSubjectRequest> findRequestByFolio(String folio) {
		return this.jdbc.sql("SELECT * FROM data_subject_requests WHERE folio = :folio")
			.param("folio", folio)
			.query(JdbcPrivacyRepository::request)
			.optional();
	}

	@Override
	public List<DataSubjectRequest> searchRequests(@Nullable String status, int limit, long offset) {
		Map<String, Object> params = new HashMap<>();
		params.put("limit", limit);
		params.put("offset", offset);
		String where = status == null ? "" : " WHERE status = :status";
		if (status != null) {
			params.put("status", status);
		}
		return this.jdbc.sql("SELECT * FROM data_subject_requests" + where
				+ " ORDER BY received_at, id LIMIT :limit OFFSET :offset")
			.params(params)
			.query(JdbcPrivacyRepository::request)
			.list();
	}

	@Override
	public long countRequests(@Nullable String status) {
		var sql = this.jdbc.sql("SELECT count(*) FROM data_subject_requests" + (status == null ? "" : " WHERE status = :status"));
		if (status != null) {
			sql = sql.param("status", status);
		}
		return sql.query(Long.class).single();
	}

	private static LegalNotice notice(ResultSet rs, int row) throws SQLException {
		return new LegalNotice(SqlTypes.uuid(rs, "id"), LegalNotice.Type.valueOf(rs.getString("notice_type")),
				rs.getString("version"), rs.getString("title"), rs.getString("summary"), rs.getString("content_url"),
				rs.getString("body"), rs.getString("content_sha256"), SqlTypes.instant(rs, "effective_from"),
				SqlTypes.uuid(rs, "published_by"), SqlTypes.instant(rs, "published_at"));
	}

	private static DataSubjectRequest request(ResultSet rs, int row) throws SQLException {
		String docType = rs.getString("document_type");
		IdentityDocument document = docType == null ? null
				: new IdentityDocument(IdentityDocument.Type.valueOf(docType), rs.getString("document_number"));
		return DataSubjectRequest.rehydrate(SqlTypes.uuid(rs, "id"), Folio.parse(rs.getString("folio")),
				DataSubjectRequest.Type.valueOf(rs.getString("request_type")), rs.getString("full_name"),
				new Email(rs.getString("email")), document, rs.getString("details"),
				DataSubjectRequest.Status.valueOf(rs.getString("status")), rs.getString("resolution_note"),
				SqlTypes.instant(rs, "received_at"), SqlTypes.instant(rs, "resolved_at"), SqlTypes.uuid(rs, "resolved_by"),
				SqlTypes.instant(rs, "updated_at"), rs.getLong("version"));
	}

}
