package com.fundigsac.quotation.infrastructure;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import com.fundigsac.quotation.domain.ProjectDetails;
import com.fundigsac.quotation.domain.QuoteItem;
import com.fundigsac.quotation.domain.QuoteOrigin;
import com.fundigsac.quotation.domain.QuoteRepository;
import com.fundigsac.quotation.domain.QuoteRequest;
import com.fundigsac.quotation.domain.QuoteStatus;
import com.fundigsac.quotation.domain.QuoteUnit;
import com.fundigsac.quotation.domain.Requester;
import com.fundigsac.quotation.domain.StatusChange;
import com.fundigsac.shared.domain.Email;
import com.fundigsac.shared.domain.Folio;
import com.fundigsac.shared.domain.PhoneNumber;
import com.fundigsac.shared.domain.Ruc;
import com.fundigsac.shared.domain.StaleVersionException;
import com.fundigsac.shared.infrastructure.SqlTypes;

/** Adaptador JDBC de solicitudes de cotización (cabecera, líneas inmutables e historial de solo inserción). */
@Repository
class JdbcQuoteRepository implements QuoteRepository {

	private final JdbcClient jdbc;

	JdbcQuoteRepository(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	@Override
	public void insert(QuoteRequest q) {
		var r = q.requester();
		var p = q.project();
		this.jdbc.sql("""
				INSERT INTO quote_requests (id, folio, origin, status, company_name, ruc, contact_name, contact_position,
				    email, phone, project_type, delivery_department, delivery_place, requested_delivery_date, message,
				    privacy_notice_version, customer_id, assignee_id, status_reason, submitted_at, updated_at, version)
				VALUES (:id, :folio, :origin, :status, :company, :ruc, :contact, :position, :email, :phone, :projectType,
				    :department, :place, :deliveryDate, :message, :privacy, :customerId, NULL, NULL, :submittedAt,
				    :updatedAt, 0)""")
			.param("id", q.id())
			.param("folio", q.folio().value())
			.param("origin", q.origin().name())
			.param("status", q.status().name())
			.param("company", r.companyName())
			.param("ruc", r.ruc().value())
			.param("contact", r.contactName())
			.param("position", r.position())
			.param("email", r.email().value())
			.param("phone", r.phone().value())
			.param("projectType", p.projectType())
			.param("department", p.deliveryDepartment())
			.param("place", p.deliveryPlace())
			.param("deliveryDate", p.requestedDeliveryDate())
			.param("message", p.message())
			.param("privacy", q.privacyNoticeVersion())
			.param("customerId", q.customerId())
			.param("submittedAt", SqlTypes.ts(q.submittedAt()))
			.param("updatedAt", SqlTypes.ts(q.updatedAt()))
			.update();
		for (QuoteItem i : q.items()) {
			this.jdbc.sql("""
					INSERT INTO quote_items (quote_id, line_no, product_id, product_slug, product_name, sku, variant_label,
					    quantity, unit, addons, notes)
					VALUES (:quoteId, :lineNo, :productId, :slug, :name, :sku, :variant, :quantity, :unit,
					    CAST(:addons AS text[]), :notes)""")
				.param("quoteId", q.id())
				.param("lineNo", i.lineNo())
				.param("productId", i.productId())
				.param("slug", i.productSlug())
				.param("name", i.productName())
				.param("sku", i.sku())
				.param("variant", i.variantLabel())
				.param("quantity", i.quantity())
				.param("unit", i.unit().name())
				.param("addons", SqlTypes.texts(i.addons()))
				.param("notes", i.notes())
				.update();
		}
		insertHistory(q);
	}

	@Override
	public void update(QuoteRequest q) {
		int rows = this.jdbc.sql("""
				UPDATE quote_requests SET status = :status, assignee_id = :assignee, status_reason = :reason,
				    updated_at = :updatedAt, version = version + 1
				 WHERE id = :id AND version = :version""")
			.param("status", q.status().name())
			.param("assignee", q.assigneeId())
			.param("reason", q.statusReason())
			.param("updatedAt", SqlTypes.ts(q.updatedAt()))
			.param("id", q.id())
			.param("version", q.version())
			.update();
		if (rows != 1) {
			throw new StaleVersionException();
		}
		insertHistory(q);
	}

	@Override
	public Optional<QuoteRequest> findById(UUID id) {
		return find("id = :key", id);
	}

	@Override
	public Optional<QuoteRequest> findByFolio(String folio) {
		return find("folio = :key", folio);
	}

	@Override
	public List<StatusChange> history(UUID quoteId) {
		return this.jdbc.sql("SELECT * FROM quote_status_events WHERE quote_id = :id ORDER BY occurred_at, id")
			.param("id", quoteId)
			.query((rs, n) -> {
				String from = rs.getString("from_status");
				return new StatusChange(SqlTypes.uuid(rs, "id"), from == null ? null : QuoteStatus.valueOf(from),
						QuoteStatus.valueOf(rs.getString("to_status")), rs.getString("note"), SqlTypes.uuid(rs, "actor_id"),
						SqlTypes.instant(rs, "occurred_at"));
			})
			.list();
	}

	private void insertHistory(QuoteRequest q) {
		for (StatusChange c : q.drainNewHistory()) {
			this.jdbc.sql("""
					INSERT INTO quote_status_events (id, quote_id, from_status, to_status, note, actor_id, occurred_at)
					VALUES (:id, :quoteId, :from, :to, :note, :actor, :at)""")
				.param("id", c.id())
				.param("quoteId", q.id())
				.param("from", c.from() == null ? null : c.from().name())
				.param("to", c.to().name())
				.param("note", c.note() == null ? null : c.note().substring(0, Math.min(1000, c.note().length())))
				.param("actor", c.actorId())
				.param("at", SqlTypes.ts(c.occurredAt()))
				.update();
		}
	}

	private Optional<QuoteRequest> find(String condition, Object key) {
		Optional<QuoteRequest> header = this.jdbc.sql("SELECT * FROM quote_requests WHERE " + condition)
			.param("key", key)
			.query(this::map)
			.optional();
		return header;
	}

	private QuoteRequest map(ResultSet rs, int row) throws SQLException {
		UUID id = SqlTypes.uuid(rs, "id");
		List<QuoteItem> items = this.jdbc.sql("SELECT * FROM quote_items WHERE quote_id = :id ORDER BY line_no")
			.param("id", id)
			.query((irs, n) -> new QuoteItem(irs.getInt("line_no"), SqlTypes.uuid(irs, "product_id"),
					irs.getString("product_slug"), irs.getString("product_name"), irs.getString("sku"),
					irs.getString("variant_label"), irs.getBigDecimal("quantity"), QuoteUnit.valueOf(irs.getString("unit")),
					SqlTypes.textArray(irs, "addons"), irs.getString("notes")))
			.list();
		Requester requester = new Requester(rs.getString("company_name"), new Ruc(rs.getString("ruc")),
				rs.getString("contact_name"), rs.getString("contact_position"), new Email(rs.getString("email")),
				new PhoneNumber(rs.getString("phone")));
		ProjectDetails project = new ProjectDetails(rs.getString("project_type"), rs.getString("delivery_department"),
				rs.getString("delivery_place"), SqlTypes.date(rs, "requested_delivery_date"), rs.getString("message"));
		return QuoteRequest.rehydrate(id, Folio.parse(rs.getString("folio")), QuoteOrigin.valueOf(rs.getString("origin")),
				QuoteStatus.valueOf(rs.getString("status")), requester, project, items, rs.getString("privacy_notice_version"),
				SqlTypes.uuid(rs, "customer_id"), SqlTypes.uuid(rs, "assignee_id"), rs.getString("status_reason"),
				SqlTypes.instant(rs, "submitted_at"), SqlTypes.instant(rs, "updated_at"), rs.getLong("version"));
	}

}
