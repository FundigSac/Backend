package com.fundigsac.audit.infrastructure;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import com.fundigsac.audit.application.AuditTrail;
import com.fundigsac.audit.application.AuditTrail.AuditEntry;
import com.fundigsac.shared.application.PageQuery;
import com.fundigsac.shared.application.PageResult;
import com.fundigsac.shared.infrastructure.SqlTypes;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/** Adaptador JDBC de auditoría (solo inserción; la deduplicación la hace la restricción única). */
@Repository
class JdbcAuditStore implements AuditTrail.Store {

	private static final TypeReference<Map<String, String>> DETAILS = new TypeReference<>() {
	};

	private final JdbcClient jdbc;

	private final JsonMapper json;

	JdbcAuditStore(JdbcClient jdbc, JsonMapper json) {
		this.jdbc = jdbc;
		this.json = json;
	}

	@Override
	public void append(AuditEntry e, Instant recordedAt) {
		this.jdbc.sql("""
				INSERT INTO audit_events (id, occurred_at, recorded_at, actor_id, action, resource_type, resource_id, details)
				VALUES (:id, :occurredAt, :recordedAt, :actor, :action, :type, :resourceId, CAST(:details AS jsonb))
				ON CONFLICT (action, resource_type, resource_id, occurred_at) DO NOTHING""")
			.param("id", e.id())
			.param("occurredAt", SqlTypes.ts(e.occurredAt()))
			.param("recordedAt", SqlTypes.ts(recordedAt))
			.param("actor", e.actorId())
			.param("action", truncate(e.action(), 60))
			.param("type", truncate(e.resourceType(), 40))
			.param("resourceId", truncate(e.resourceId(), 80))
			.param("details", this.json.writeValueAsString(e.details()))
			.update();
	}

	@Override
	public PageResult<AuditEntry> search(AuditTrail.Filter f, PageQuery page) {
		Map<String, Object> params = new HashMap<>();
		StringBuilder where = new StringBuilder(" WHERE true");
		if (f.resourceType() != null) {
			where.append(" AND resource_type = :type");
			params.put("type", f.resourceType());
		}
		if (f.resourceId() != null) {
			where.append(" AND resource_id = :resourceId");
			params.put("resourceId", f.resourceId());
		}
		if (f.actorId() != null) {
			where.append(" AND actor_id = :actor");
			params.put("actor", f.actorId());
		}
		if (f.action() != null) {
			where.append(" AND action = :action");
			params.put("action", f.action());
		}
		if (f.from() != null) {
			where.append(" AND occurred_at >= :from");
			params.put("from", SqlTypes.ts(f.from()));
		}
		if (f.to() != null) {
			where.append(" AND occurred_at < :to");
			params.put("to", SqlTypes.ts(f.to()));
		}
		long total = this.jdbc.sql("SELECT count(*) FROM audit_events" + where).params(params).query(Long.class).single();
		params.put("limit", page.size());
		params.put("offset", page.offset());
		List<AuditEntry> items = this.jdbc
			.sql("SELECT * FROM audit_events" + where + " ORDER BY occurred_at DESC, id DESC LIMIT :limit OFFSET :offset")
			.params(params)
			.query((rs, n) -> new AuditEntry(SqlTypes.uuid(rs, "id"), SqlTypes.instant(rs, "occurred_at"),
					SqlTypes.uuid(rs, "actor_id"), rs.getString("action"), rs.getString("resource_type"),
					rs.getString("resource_id"), this.json.readValue(rs.getString("details"), DETAILS)))
			.list();
		return PageResult.of(items, page, total);
	}

	private static String truncate(String value, int max) {
		return value.length() > max ? value.substring(0, max) : value;
	}

}
