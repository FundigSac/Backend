package com.fundigsac.quotation.infrastructure;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import com.fundigsac.quotation.application.QuoteReadModel;
import com.fundigsac.quotation.application.QuoteViews.QuoteSummaryView;
import com.fundigsac.shared.application.PageQuery;
import com.fundigsac.shared.application.PageResult;
import com.fundigsac.shared.infrastructure.SqlTypes;

/** Bandejas de cotizaciones (más recientes primero, orden determinista). */
@Repository
class JdbcQuoteReadModel implements QuoteReadModel {

	private static final String COLUMNS = """
			q.id, q.folio, q.origin, q.status, q.company_name, q.contact_name, q.email, q.assignee_id, q.submitted_at,
			q.updated_at, (SELECT count(*) FROM quote_items i WHERE i.quote_id = q.id) AS line_count""";

	private final JdbcClient jdbc;

	JdbcQuoteReadModel(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	@Override
	public PageResult<QuoteSummaryView> staffInbox(StaffFilter filter, PageQuery page) {
		Map<String, Object> params = new HashMap<>();
		StringBuilder where = new StringBuilder(" WHERE true");
		if (filter.status() != null) {
			where.append(" AND q.status = :status");
			params.put("status", filter.status());
		}
		if (filter.assigneeId() != null) {
			where.append(" AND q.assignee_id = :assignee");
			params.put("assignee", filter.assigneeId());
		}
		if (filter.unassigned()) {
			where.append(" AND q.assignee_id IS NULL");
		}
		if (filter.query() != null) {
			where.append(" AND (q.folio ILIKE :q OR q.company_name ILIKE :q OR q.contact_name ILIKE :q OR q.email ILIKE :q)");
			params.put("q", "%" + filter.query().replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%");
		}
		return page(where.toString(), params, page);
	}

	@Override
	public PageResult<QuoteSummaryView> ofCustomer(UUID customerId, PageQuery page) {
		Map<String, Object> params = new HashMap<>();
		params.put("customer", customerId);
		return page(" WHERE q.customer_id = :customer", params, page);
	}

	private PageResult<QuoteSummaryView> page(String where, Map<String, Object> params, PageQuery page) {
		long total = this.jdbc.sql("SELECT count(*) FROM quote_requests q" + where).params(params).query(Long.class).single();
		params.put("limit", page.size());
		params.put("offset", page.offset());
		List<QuoteSummaryView> items = this.jdbc
			.sql("SELECT " + COLUMNS + " FROM quote_requests q" + where
					+ " ORDER BY q.submitted_at DESC, q.id DESC LIMIT :limit OFFSET :offset")
			.params(params)
			.query(JdbcQuoteReadModel::summary)
			.list();
		return PageResult.of(items, page, total);
	}

	private static QuoteSummaryView summary(ResultSet rs, int row) throws SQLException {
		return new QuoteSummaryView(SqlTypes.uuid(rs, "id"), rs.getString("folio"), rs.getString("origin"),
				rs.getString("status"), rs.getString("company_name"), rs.getString("contact_name"), rs.getString("email"),
				rs.getInt("line_count"), SqlTypes.uuid(rs, "assignee_id"), SqlTypes.instant(rs, "submitted_at"),
				SqlTypes.instant(rs, "updated_at"));
	}

}
