package com.fundigsac.complaint.infrastructure;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import com.fundigsac.complaint.domain.Complaint;
import com.fundigsac.complaint.domain.ComplaintRepository;
import com.fundigsac.shared.domain.Email;
import com.fundigsac.shared.domain.Folio;
import com.fundigsac.shared.domain.IdentityDocument;
import com.fundigsac.shared.domain.Money;
import com.fundigsac.shared.domain.PhoneNumber;
import com.fundigsac.shared.domain.StaleVersionException;
import com.fundigsac.shared.infrastructure.SqlTypes;

/**
 * Adaptador JDBC del Libro de Reclamaciones. El UPDATE solo toca columnas de gestión; además un trigger de la base de
 * datos rechaza cualquier cambio en lo presentado por el consumidor.
 */
@Repository
class JdbcComplaintRepository implements ComplaintRepository {

	private final JdbcClient jdbc;

	JdbcComplaintRepository(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	@Override
	public void insert(Complaint c) {
		var con = c.consumer();
		var g = c.guardian();
		var d = c.details();
		this.jdbc.sql("""
				INSERT INTO complaints (id, folio, kind, consumer_name, document_type, document_number, email, phone, address,
				    is_minor, guardian_name, guardian_document_type, guardian_document_number, good_type, good_description,
				    claimed_amount, claimed_currency, service_location, incident_date, related_reference, facts,
				    consumer_request, response_channel, privacy_notice_version, status, filed_at, response_due_on,
				    updated_at, version)
				VALUES (:id, :folio, :kind, :name, :docType, :docNumber, :email, :phone, :address, :minor, :guardianName,
				    :guardianDocType, :guardianDocNumber, :goodType, :goodDescription, :amount, :currency, :serviceLocation,
				    :incidentDate, :reference, :facts, :request, :channel, :privacy, :status, :filedAt, :dueOn, :updatedAt, 0)""")
			.param("id", c.id())
			.param("folio", c.folio().value())
			.param("kind", c.kind().name())
			.param("name", con.fullName())
			.param("docType", con.document().type().name())
			.param("docNumber", con.document().number())
			.param("email", con.email().value())
			.param("phone", con.phone().value())
			.param("address", con.address())
			.param("minor", g != null)
			.param("guardianName", g == null ? null : g.fullName())
			.param("guardianDocType", g == null ? null : g.document().type().name())
			.param("guardianDocNumber", g == null ? null : g.document().number())
			.param("goodType", d.goodType() == null ? null : d.goodType().name())
			.param("goodDescription", d.goodDescription())
			.param("amount", d.claimedAmount() == null ? null : d.claimedAmount().amount())
			.param("currency", d.claimedAmount() == null ? null : d.claimedAmount().currency().name())
			.param("serviceLocation", d.serviceLocation())
			.param("incidentDate", d.incidentDate())
			.param("reference", d.relatedReference())
			.param("facts", d.facts())
			.param("request", d.consumerRequest())
			.param("channel", c.responseChannel().name())
			.param("privacy", c.privacyNoticeVersion())
			.param("status", c.status().name())
			.param("filedAt", SqlTypes.ts(c.filedAt()))
			.param("dueOn", c.responseDueOn())
			.param("updatedAt", SqlTypes.ts(c.updatedAt()))
			.update();
		insertEvents(c);
	}

	@Override
	public void update(Complaint c) {
		int rows = this.jdbc.sql("""
				UPDATE complaints SET status = :status, assignee_id = :assignee, response_text = :response,
				    responded_at = :respondedAt, responded_by = :respondedBy, closed_at = :closedAt, updated_at = :updatedAt,
				    version = version + 1
				 WHERE id = :id AND version = :version""")
			.param("status", c.status().name())
			.param("assignee", c.assigneeId())
			.param("response", c.responseText())
			.param("respondedAt", SqlTypes.ts(c.respondedAt()))
			.param("respondedBy", c.respondedBy())
			.param("closedAt", SqlTypes.ts(c.closedAt()))
			.param("updatedAt", SqlTypes.ts(c.updatedAt()))
			.param("id", c.id())
			.param("version", c.version())
			.update();
		if (rows != 1) {
			throw new StaleVersionException();
		}
		insertEvents(c);
	}

	@Override
	public Optional<Complaint> findById(UUID id) {
		return this.jdbc.sql("SELECT * FROM complaints WHERE id = :id").param("id", id).query(JdbcComplaintRepository::map).optional();
	}

	@Override
	public Optional<Complaint> findByFolio(String folio) {
		return this.jdbc.sql("SELECT * FROM complaints WHERE folio = :folio")
			.param("folio", folio)
			.query(JdbcComplaintRepository::map)
			.optional();
	}

	@Override
	public List<Complaint.Event> history(UUID complaintId) {
		return this.jdbc.sql("SELECT * FROM complaint_events WHERE complaint_id = :id ORDER BY occurred_at, id")
			.param("id", complaintId)
			.query((rs, n) -> new Complaint.Event(SqlTypes.uuid(rs, "id"), Complaint.EventType.valueOf(rs.getString("event_type")),
					rs.getString("note"), SqlTypes.uuid(rs, "actor_id"), SqlTypes.instant(rs, "occurred_at")))
			.list();
	}

	@Override
	public List<Complaint> search(@Nullable String status, @Nullable LocalDate overdueAsOf, @Nullable String query,
			int limit, long offset) {
		Map<String, Object> params = new HashMap<>();
		String where = where(status, overdueAsOf, query, params);
		params.put("limit", limit);
		params.put("offset", offset);
		return this.jdbc
			.sql("SELECT * FROM complaints" + where + " ORDER BY response_due_on, filed_at, id LIMIT :limit OFFSET :offset")
			.params(params)
			.query(JdbcComplaintRepository::map)
			.list();
	}

	@Override
	public long count(@Nullable String status, @Nullable LocalDate overdueAsOf, @Nullable String query) {
		Map<String, Object> params = new HashMap<>();
		return this.jdbc.sql("SELECT count(*) FROM complaints" + where(status, overdueAsOf, query, params))
			.params(params)
			.query(Long.class)
			.single();
	}

	private static String where(@Nullable String status, @Nullable LocalDate overdueAsOf, @Nullable String query,
			Map<String, Object> params) {
		StringBuilder where = new StringBuilder(" WHERE true");
		if (status != null) {
			where.append(" AND status = :status");
			params.put("status", status);
		}
		if (overdueAsOf != null) {
			where.append(" AND status IN ('FILED', 'IN_REVIEW') AND response_due_on < :today");
			params.put("today", overdueAsOf);
		}
		if (query != null) {
			where.append(" AND (folio ILIKE :q OR consumer_name ILIKE :q OR email ILIKE :q)");
			params.put("q", "%" + query.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%");
		}
		return where.toString();
	}

	private void insertEvents(Complaint c) {
		for (Complaint.Event e : c.drainNewEvents()) {
			this.jdbc.sql("""
					INSERT INTO complaint_events (id, complaint_id, event_type, note, actor_id, occurred_at)
					VALUES (:id, :complaintId, :type, :note, :actor, :at)""")
				.param("id", e.id())
				.param("complaintId", c.id())
				.param("type", e.type().name())
				.param("note", e.note())
				.param("actor", e.actorId())
				.param("at", SqlTypes.ts(e.occurredAt()))
				.update();
		}
	}

	private static Complaint map(ResultSet rs, int row) throws SQLException {
		Complaint.Consumer consumer = new Complaint.Consumer(rs.getString("consumer_name"),
				new IdentityDocument(IdentityDocument.Type.valueOf(rs.getString("document_type")), rs.getString("document_number")),
				new Email(rs.getString("email")), new PhoneNumber(rs.getString("phone")), rs.getString("address"));
		String guardianName = rs.getString("guardian_name");
		Complaint.Guardian guardian = rs.getBoolean("is_minor") && guardianName != null
				? new Complaint.Guardian(guardianName, new IdentityDocument(
						IdentityDocument.Type.valueOf(rs.getString("guardian_document_type")), rs.getString("guardian_document_number")))
				: null;
		var amount = rs.getBigDecimal("claimed_amount");
		String goodType = rs.getString("good_type");
		Complaint.Details details = new Complaint.Details(goodType == null ? null : Complaint.GoodType.valueOf(goodType),
				rs.getString("good_description"),
				amount == null ? null : new Money(amount, Money.Currency.valueOf(rs.getString("claimed_currency"))),
				rs.getString("service_location"), SqlTypes.date(rs, "incident_date"), rs.getString("related_reference"),
				rs.getString("facts"), rs.getString("consumer_request"));
		return Complaint.rehydrate(SqlTypes.uuid(rs, "id"), Folio.parse(rs.getString("folio")),
				Complaint.Kind.valueOf(rs.getString("kind")), consumer, guardian, details,
				Complaint.ResponseChannel.valueOf(rs.getString("response_channel")), rs.getString("privacy_notice_version"),
				Complaint.Status.valueOf(rs.getString("status")), SqlTypes.instant(rs, "filed_at"),
				SqlTypes.date(rs, "response_due_on"), SqlTypes.uuid(rs, "assignee_id"), rs.getString("response_text"),
				SqlTypes.instant(rs, "responded_at"), SqlTypes.uuid(rs, "responded_by"), SqlTypes.instant(rs, "closed_at"),
				SqlTypes.instant(rs, "updated_at"), rs.getLong("version"));
	}

}
