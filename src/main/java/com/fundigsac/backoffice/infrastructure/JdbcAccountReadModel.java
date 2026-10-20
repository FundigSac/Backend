package com.fundigsac.backoffice.infrastructure;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import com.fundigsac.backoffice.application.AccountReadModel;
import com.fundigsac.backoffice.application.AccountViews.AccountPage;
import com.fundigsac.backoffice.application.AccountViews.AccountRow;
import com.fundigsac.backoffice.application.AccountViews.AccountStats;
import com.fundigsac.backoffice.application.AccountViews.ActivityRow;
import com.fundigsac.backoffice.application.AccountViews.DayCount;
import com.fundigsac.backoffice.application.AccountViews.Funnel;
import com.fundigsac.backoffice.application.AccountViews.SourceCount;
import com.fundigsac.shared.application.PageQuery;
import com.fundigsac.shared.application.PageResult;
import com.fundigsac.shared.infrastructure.SqlTypes;

/**
 * Consultas de solo lectura del directorio. Las cuentas eliminadas (anonimizadas) nunca se listan. Los filtros llegan
 * validados; el orden sale de una lista cerrada, nunca del texto de la petición.
 */
@Repository
class JdbcAccountReadModel implements AccountReadModel {

	private static final String COLUMNS = """
			u.id, u.kind, u.full_name, u.email, u.company_name, u.ruc, u.phone, u.signup_method, u.status,
			COALESCE((SELECT array_agg(r.role ORDER BY r.role) FROM user_roles r WHERE r.user_id = u.id), '{}') AS roles,
			u.email_verified_at IS NOT NULL AS email_verified, u.newsletter_opt_in_at IS NOT NULL AS newsletter,
			u.privacy_notice_version, u.signup_source, u.created_at, u.last_login_at,
			(SELECT count(*) FROM quote_requests q WHERE q.customer_id = u.id) AS quotes,
			(SELECT count(*) FROM quote_cart_items c WHERE c.owner_id = u.id) AS cart_items,
			(SELECT count(*) FROM refresh_tokens t
			  WHERE t.user_id = u.id AND t.revoked_at IS NULL AND t.expires_at > now()) AS active_sessions,
			u.version""";

	private static final String CUSTOMERS = " FROM user_accounts u WHERE u.kind = 'CUSTOMER' AND u.status <> 'DELETED'";

	private final JdbcClient jdbc;

	JdbcAccountReadModel(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	@Override
	public AccountPage accounts(Filter filter, PageQuery page) {
		Map<String, Object> params = new HashMap<>();
		String base = where(filter, params);
		String withStatus = filter.status() == null ? base : base + " AND u.status = :status";
		if (filter.status() != null) {
			params.put("status", filter.status());
		}
		long total = this.jdbc.sql("SELECT count(*) FROM user_accounts u" + withStatus).params(params).query(Long.class).single();
		Map<String, Long> counts = new LinkedHashMap<>();
		counts.put("all", 0L);
		counts.put("ACTIVE", 0L);
		counts.put("PENDING_VERIFICATION", 0L);
		counts.put("DISABLED", 0L);
		this.jdbc.sql("SELECT u.status, count(*) AS n FROM user_accounts u" + base + " GROUP BY u.status")
			.params(params)
			.query((rs, n) -> Map.entry(rs.getString("status"), rs.getLong("n")))
			.list()
			.forEach((e) -> {
				counts.merge(e.getKey(), e.getValue(), Long::sum);
				counts.merge("all", e.getValue(), Long::sum);
			});
		params.put("limit", page.size());
		params.put("offset", page.offset());
		List<AccountRow> items = this.jdbc
			.sql("SELECT " + COLUMNS + " FROM user_accounts u" + withStatus + " ORDER BY " + order(filter)
					+ " LIMIT :limit OFFSET :offset")
			.params(params)
			.query(JdbcAccountReadModel::row)
			.list();
		int totalPages = (int) ((total + page.size() - 1) / page.size());
		return new AccountPage(items, page.page(), page.size(), total, totalPages, counts);
	}

	@Override
	public Optional<AccountRow> account(UUID id) {
		return this.jdbc.sql("SELECT " + COLUMNS + " FROM user_accounts u WHERE u.id = :id AND u.status <> 'DELETED'")
			.param("id", id)
			.query(JdbcAccountReadModel::row)
			.optional();
	}

	@Override
	public AccountStats stats(Instant from, Instant previousFrom, int days, Instant now) {
		Map<String, Object> p = Map.of("from", SqlTypes.ts(from), "prev", SqlTypes.ts(previousFrom), "now", SqlTypes.ts(now),
				"days", days);
		Map<String, Object> totals = this.jdbc.sql("""
				SELECT count(*) AS total,
				       count(*) FILTER (WHERE u.created_at >= :from) AS new_in_range,
				       count(*) FILTER (WHERE u.created_at >= :prev AND u.created_at < :from) AS new_prev_range,
				       count(*) FILTER (WHERE u.status = 'PENDING_VERIFICATION') AS pending,
				       count(*) FILTER (WHERE u.last_login_at >= CAST(:now AS timestamptz) - interval '7 days') AS active_week,
				       count(*) FILTER (WHERE u.status = 'DISABLED') AS disabled,
				       count(*) FILTER (WHERE u.email_verified_at IS NOT NULL) AS verified,
				       count(*) FILTER (WHERE u.newsletter_opt_in_at IS NOT NULL) AS newsletter,
				       count(*) FILTER (WHERE u.status = 'PENDING_VERIFICATION'
				                          AND u.created_at < CAST(:now AS timestamptz) - interval '48 hours') AS pending_over_48h
				""" + CUSTOMERS)
			.params(p)
			.query()
			.singleRow();
		long total = ((Number) totals.get("total")).longValue();

		// Una fila por día (hora de Lima) de los últimos `days` días, con ceros donde no hubo registros.
		List<DayCount> series = this.jdbc.sql("""
				SELECT CAST(d.day AS date) AS day, count(u.id) AS n
				  FROM generate_series((CAST(:now AS timestamptz) AT TIME ZONE 'America/Lima')::date - (:days - 1),
				                       (CAST(:now AS timestamptz) AT TIME ZONE 'America/Lima')::date, interval '1 day') AS d(day)
				  LEFT JOIN user_accounts u ON u.kind = 'CUSTOMER' AND u.status <> 'DELETED'
				       AND (u.created_at AT TIME ZONE 'America/Lima')::date = d.day::date
				 GROUP BY d.day ORDER BY d.day""")
			.params(p)
			.query((rs, n) -> new DayCount(rs.getObject("day", LocalDate.class), rs.getLong("n")))
			.list();

		Map<String, Long> byMethod = new LinkedHashMap<>(Map.of("EMAIL", 0L, "GOOGLE", 0L, "FACEBOOK", 0L));
		this.jdbc.sql("SELECT u.signup_method AS k, count(*) AS n" + CUSTOMERS + " AND u.created_at >= :from GROUP BY 1")
			.params(p)
			.query((rs, n) -> Map.entry(rs.getString("k"), rs.getLong("n")))
			.list()
			.forEach((e) -> byMethod.put(e.getKey(), e.getValue()));
		Map<String, Long> byStatus = new LinkedHashMap<>(Map.of("ACTIVE", 0L, "PENDING_VERIFICATION", 0L, "DISABLED", 0L));
		this.jdbc.sql("SELECT u.status AS k, count(*) AS n" + CUSTOMERS + " GROUP BY 1")
			.query((rs, n) -> Map.entry(rs.getString("k"), rs.getLong("n")))
			.list()
			.forEach((e) -> byStatus.put(e.getKey(), e.getValue()));
		List<SourceCount> bySource = this.jdbc
			.sql("SELECT COALESCE(u.signup_source, 'registro') AS k, count(*) AS n" + CUSTOMERS
					+ " AND u.created_at >= :from GROUP BY 1 ORDER BY n DESC, k")
			.params(p)
			.query((rs, n) -> new SourceCount(rs.getString("k"), rs.getLong("n")))
			.list();
		Map<String, Object> funnel = this.jdbc.sql("""
				SELECT count(*) AS registered, count(*) FILTER (WHERE u.email_verified_at IS NOT NULL) AS verified,
				       count(*) FILTER (WHERE EXISTS (SELECT 1 FROM quote_requests q WHERE q.customer_id = u.id)) AS quoted
				""" + CUSTOMERS + " AND u.created_at >= :from")
			.params(p)
			.query()
			.singleRow();
		List<AccountRow> latest = this.jdbc.sql("SELECT " + COLUMNS + CUSTOMERS + " ORDER BY u.created_at DESC, u.id LIMIT 6")
			.query(JdbcAccountReadModel::row)
			.list();
		List<AccountRow> pendingOldest = this.jdbc
			.sql("SELECT " + COLUMNS + CUSTOMERS + " AND u.status = 'PENDING_VERIFICATION' ORDER BY u.created_at, u.id LIMIT 4")
			.query(JdbcAccountReadModel::row)
			.list();

		return new AccountStats(total, longOf(totals, "new_in_range"), longOf(totals, "new_prev_range"),
				longOf(totals, "pending"), longOf(totals, "active_week"), longOf(totals, "disabled"),
				rate(longOf(totals, "verified"), total), rate(longOf(totals, "newsletter"), total), series, byMethod, byStatus,
				bySource, latest,
				new Funnel(longOf(funnel, "registered"), longOf(funnel, "verified"), longOf(funnel, "quoted")),
				pendingOldest, longOf(totals, "pending_over_48h"), now);
	}

	/**
	 * Actividad: auditoría de cuentas (ingresos, verificaciones, contraseñas, estado, roles, invitaciones y
	 * eliminaciones) más las altas y las cotizaciones de clientes con cuenta, que salen de sus propias tablas.
	 */
	@Override
	public PageResult<ActivityRow> activity(ActivityFilter filter, PageQuery page) {
		String events = """
				SELECT 'aud_' || a.id AS id, a.occurred_at AS at,
				       CASE a.action
				           WHEN 'SIGNED_IN' THEN 'LOGIN'
				           WHEN 'EMAIL_VERIFIED' THEN 'VERIFY'
				           WHEN 'PASSWORD_RESET' THEN 'PASSWORD'
				           WHEN 'PASSWORD_CHANGED' THEN 'PASSWORD'
				           WHEN 'USER_ROLES_CHANGED' THEN 'ROLES'
				           WHEN 'STAFF_INVITED' THEN 'INVITE'
				           WHEN 'ACCOUNT_DELETED' THEN 'DELETE'
				           WHEN 'USER_STATUS_CHANGED' THEN CASE WHEN a.details ->> 'status' = 'DISABLED' THEN 'DISABLE' ELSE 'ENABLE' END
				       END AS type,
				       CAST(a.resource_id AS uuid) AS user_id,
				       CASE WHEN a.actor_id IS NOT NULL AND a.actor_id::text <> a.resource_id THEN a.actor_id END AS actor_id,
				       COALESCE(a.details ->> 'method', a.details ->> 'roles') AS detail
				  FROM audit_events a
				 WHERE a.resource_type = 'USER'
				   AND a.action IN ('SIGNED_IN', 'EMAIL_VERIFIED', 'PASSWORD_RESET', 'PASSWORD_CHANGED', 'USER_ROLES_CHANGED',
				                    'STAFF_INVITED', 'ACCOUNT_DELETED', 'USER_STATUS_CHANGED')
				UNION ALL
				SELECT 'usr_' || u.id, u.created_at, 'SIGNUP', u.id, NULL, u.signup_method
				  FROM user_accounts u WHERE u.kind = 'CUSTOMER'
				UNION ALL
				SELECT 'cot_' || q.id, q.submitted_at, 'QUOTE', q.customer_id, NULL, q.folio
				  FROM quote_requests q WHERE q.customer_id IS NOT NULL""";
		StringBuilder where = new StringBuilder(" WHERE true");
		Map<String, Object> params = new HashMap<>();
		if (filter.type() != null) {
			where.append(" AND e.type = :type");
			params.put("type", filter.type());
		}
		if (filter.userId() != null) {
			where.append(" AND e.user_id = :userId");
			params.put("userId", filter.userId());
		}
		if (filter.query() != null) {
			where.append(" AND (u.full_name ILIKE :q OR u.email ILIKE :q OR s.full_name ILIKE :q OR e.detail ILIKE :q)");
			params.put("q", like(filter.query()));
		}
		String from = " FROM (" + events + ") e JOIN user_accounts u ON u.id = e.user_id"
				+ " LEFT JOIN user_accounts s ON s.id = e.actor_id" + where;
		long total = this.jdbc.sql("SELECT count(*)" + from).params(params).query(Long.class).single();
		params.put("limit", page.size());
		params.put("offset", page.offset());
		List<ActivityRow> items = this.jdbc
			.sql("SELECT e.id, e.at, e.type, e.user_id, u.full_name AS user_name, s.full_name AS actor_name, e.detail" + from
					+ " ORDER BY e.at DESC, e.id LIMIT :limit OFFSET :offset")
			.params(params)
			.query((rs, n) -> new ActivityRow(rs.getString("id"), SqlTypes.instant(rs, "at"), rs.getString("type"),
					SqlTypes.uuid(rs, "user_id"), rs.getString("user_name"), rs.getString("actor_name"), rs.getString("detail")))
			.list();
		return PageResult.of(items, page, total);
	}

	private static String where(Filter filter, Map<String, Object> params) {
		StringBuilder where = new StringBuilder(" WHERE u.status <> 'DELETED'");
		if (filter.query() != null) {
			// Todas las palabras deben aparecer en alguno de los campos (sin distinguir tildes ni mayúsculas).
			String[] words = filter.query().split("\\s+");
			for (int i = 0; i < words.length && i < 6; i++) {
				where.append(" AND unaccent_text(concat_ws(' ', u.full_name, u.email, u.company_name, u.ruc, u.phone))")
					.append(" ILIKE unaccent_text(:q").append(i).append(")");
				params.put("q" + i, like(words[i]));
			}
		}
		if (filter.kind() != null) {
			where.append(" AND u.kind = :kind");
			params.put("kind", filter.kind());
		}
		if (filter.method() != null) {
			where.append(" AND u.signup_method = :method");
			params.put("method", filter.method());
		}
		if (filter.role() != null) {
			where.append(" AND EXISTS (SELECT 1 FROM user_roles r WHERE r.user_id = u.id AND r.role = :role)");
			params.put("role", filter.role());
		}
		if (filter.registeredFrom() != null) {
			where.append(" AND u.created_at >= :registeredFrom");
			params.put("registeredFrom", SqlTypes.ts(filter.registeredFrom()));
		}
		return where.toString();
	}

	private static String order(Filter filter) {
		String direction = filter.descending() ? " DESC" : " ASC";
		String column = switch (filter.sort()) {
			case NAME -> "lower(u.full_name)" + direction;
			case REGISTERED_AT -> "u.created_at" + direction;
			case LAST_ACTIVE_AT -> "u.last_login_at" + direction + " NULLS LAST";
			case QUOTES -> "quotes" + direction;
			case STATUS -> "CASE u.status WHEN 'ACTIVE' THEN 0 WHEN 'PENDING_VERIFICATION' THEN 1 ELSE 2 END" + direction;
		};
		return column + ", u.id";
	}

	private static String like(String text) {
		return "%" + text.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
	}

	private static long longOf(Map<String, Object> row, String key) {
		Object value = row.get(key);
		return value == null ? 0 : ((Number) value).longValue();
	}

	private static double rate(long part, long total) {
		return total == 0 ? 0 : (double) part / total;
	}

	private static AccountRow row(ResultSet rs, int n) throws SQLException {
		return new AccountRow(SqlTypes.uuid(rs, "id"), rs.getString("kind"), rs.getString("full_name"), rs.getString("email"),
				rs.getString("company_name"), rs.getString("ruc"), rs.getString("phone"), rs.getString("signup_method"),
				rs.getString("status"), SqlTypes.textArray(rs, "roles"), rs.getBoolean("email_verified"),
				rs.getBoolean("newsletter"), rs.getString("privacy_notice_version"), rs.getString("signup_source"),
				SqlTypes.instant(rs, "created_at"), SqlTypes.instant(rs, "last_login_at"), rs.getLong("quotes"),
				rs.getLong("cart_items"), rs.getLong("active_sessions"), rs.getLong("version"));
	}

}
