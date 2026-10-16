package com.fundigsac.inquiry.infrastructure;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import com.fundigsac.inquiry.domain.Inquiry;
import com.fundigsac.inquiry.domain.InquiryRepository;
import com.fundigsac.shared.domain.Email;
import com.fundigsac.shared.domain.Folio;
import com.fundigsac.shared.domain.PhoneNumber;
import com.fundigsac.shared.domain.Ruc;
import com.fundigsac.shared.domain.StaleVersionException;
import com.fundigsac.shared.infrastructure.SqlTypes;

/** Adaptador JDBC de consultas de contacto. */
@Repository
class JdbcInquiryRepository implements InquiryRepository {

	private final JdbcClient jdbc;

	JdbcInquiryRepository(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	@Override
	public void insert(Inquiry i) {
		var s = i.sender();
		this.jdbc.sql("""
				INSERT INTO inquiries (id, folio, reason, full_name, company_name, ruc, position, email, phone, message,
				    requested_document, privacy_notice_version, status, assignee_id, received_at, updated_at, version)
				VALUES (:id, :folio, :reason, :fullName, :company, :ruc, :position, :email, :phone, :message, :document,
				    :privacy, :status, NULL, :receivedAt, :updatedAt, 0)""")
			.param("id", i.id())
			.param("folio", i.folio().value())
			.param("reason", i.reason().name())
			.param("fullName", s.fullName())
			.param("company", s.companyName())
			.param("ruc", s.ruc() == null ? null : s.ruc().value())
			.param("position", s.position())
			.param("email", s.email().value())
			.param("phone", s.phone().value())
			.param("message", i.message())
			.param("document", i.requestedDocument())
			.param("privacy", i.privacyNoticeVersion())
			.param("status", i.status().name())
			.param("receivedAt", SqlTypes.ts(i.receivedAt()))
			.param("updatedAt", SqlTypes.ts(i.updatedAt()))
			.update();
		insertEvents(i);
	}

	@Override
	public void update(Inquiry i) {
		int rows = this.jdbc.sql("""
				UPDATE inquiries SET status = :status, assignee_id = :assignee, updated_at = :updatedAt, version = version + 1
				 WHERE id = :id AND version = :version""")
			.param("status", i.status().name())
			.param("assignee", i.assigneeId())
			.param("updatedAt", SqlTypes.ts(i.updatedAt()))
			.param("id", i.id())
			.param("version", i.version())
			.update();
		if (rows != 1) {
			throw new StaleVersionException();
		}
		insertEvents(i);
	}

	@Override
	public Optional<Inquiry> findById(UUID id) {
		return this.jdbc.sql("SELECT * FROM inquiries WHERE id = :id").param("id", id).query(JdbcInquiryRepository::map).optional();
	}

	@Override
	public Optional<Inquiry> findByFolio(String folio) {
		return this.jdbc.sql("SELECT * FROM inquiries WHERE folio = :folio")
			.param("folio", folio)
			.query(JdbcInquiryRepository::map)
			.optional();
	}

	@Override
	public List<Inquiry.Event> history(UUID inquiryId) {
		return this.jdbc.sql("SELECT * FROM inquiry_events WHERE inquiry_id = :id ORDER BY occurred_at, id")
			.param("id", inquiryId)
			.query((rs, n) -> {
				String from = rs.getString("from_status");
				return new Inquiry.Event(SqlTypes.uuid(rs, "id"), from == null ? null : Inquiry.Status.valueOf(from),
						Inquiry.Status.valueOf(rs.getString("to_status")), rs.getString("note"), SqlTypes.uuid(rs, "actor_id"),
						SqlTypes.instant(rs, "occurred_at"));
			})
			.list();
	}

	@Override
	public List<Inquiry> search(@Nullable String status, @Nullable String query, int limit, long offset) {
		Map<String, Object> params = new HashMap<>();
		String where = where(status, query, params);
		params.put("limit", limit);
		params.put("offset", offset);
		return this.jdbc.sql("SELECT * FROM inquiries" + where + " ORDER BY received_at DESC, id DESC LIMIT :limit OFFSET :offset")
			.params(params)
			.query(JdbcInquiryRepository::map)
			.list();
	}

	@Override
	public long count(@Nullable String status, @Nullable String query) {
		Map<String, Object> params = new HashMap<>();
		return this.jdbc.sql("SELECT count(*) FROM inquiries" + where(status, query, params))
			.params(params)
			.query(Long.class)
			.single();
	}

	private static String where(@Nullable String status, @Nullable String query, Map<String, Object> params) {
		StringBuilder where = new StringBuilder(" WHERE true");
		if (status != null) {
			where.append(" AND status = :status");
			params.put("status", status);
		}
		if (query != null) {
			where.append(" AND (folio ILIKE :q OR full_name ILIKE :q OR company_name ILIKE :q OR email ILIKE :q)");
			params.put("q", "%" + query.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%");
		}
		return where.toString();
	}

	private void insertEvents(Inquiry i) {
		for (Inquiry.Event e : i.drainNewEvents()) {
			this.jdbc.sql("""
					INSERT INTO inquiry_events (id, inquiry_id, from_status, to_status, note, actor_id, occurred_at)
					VALUES (:id, :inquiryId, :from, :to, :note, :actor, :at)""")
				.param("id", e.id())
				.param("inquiryId", i.id())
				.param("from", e.from() == null ? null : e.from().name())
				.param("to", e.to().name())
				.param("note", e.note())
				.param("actor", e.actorId())
				.param("at", SqlTypes.ts(e.occurredAt()))
				.update();
		}
	}

	private static Inquiry map(ResultSet rs, int row) throws SQLException {
		String ruc = rs.getString("ruc");
		Inquiry.Sender sender = new Inquiry.Sender(rs.getString("full_name"), rs.getString("company_name"),
				ruc == null ? null : new Ruc(ruc), rs.getString("position"), new Email(rs.getString("email")),
				new PhoneNumber(rs.getString("phone")));
		return Inquiry.rehydrate(SqlTypes.uuid(rs, "id"), Folio.parse(rs.getString("folio")),
				Inquiry.Reason.valueOf(rs.getString("reason")), sender, rs.getString("message"),
				rs.getString("requested_document"), rs.getString("privacy_notice_version"),
				Inquiry.Status.valueOf(rs.getString("status")), SqlTypes.uuid(rs, "assignee_id"),
				SqlTypes.instant(rs, "received_at"), SqlTypes.instant(rs, "updated_at"), rs.getLong("version"));
	}

}
