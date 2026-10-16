package com.fundigsac.audit.application;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fundigsac.shared.application.PageQuery;
import com.fundigsac.shared.application.PageResult;
import com.fundigsac.shared.domain.AuditableEvent;
import com.fundigsac.shared.security.Authz;

/** Escribe y consulta la auditoría. Idempotente: un evento reprocesado no duplica el registro. */
@Service
public class AuditTrail {

	public record AuditEntry(UUID id, Instant occurredAt, @Nullable UUID actorId, String action, String resourceType,
			String resourceId, Map<String, String> details) {
	}

	public record Filter(@Nullable String resourceType, @Nullable String resourceId, @Nullable UUID actorId,
			@Nullable String action, @Nullable Instant from, @Nullable Instant to) {
	}

	/** Puerto de persistencia de la auditoría. */
	public interface Store {

		void append(AuditEntry entry, Instant recordedAt);

		PageResult<AuditEntry> search(Filter filter, PageQuery page);

	}

	private final Store store;

	private final Clock clock;

	AuditTrail(Store store, Clock clock) {
		this.store = store;
		this.clock = clock;
	}

	@ApplicationModuleListener
	public void on(AuditableEvent event) {
		this.store.append(new AuditEntry(com.fundigsac.shared.domain.Ids.newId(), event.occurredAt(), event.actorId(),
				event.auditAction(), event.auditResourceType(), event.auditResourceId(), event.auditDetails()),
				this.clock.instant());
	}

	@PreAuthorize(Authz.AUDIT_READ)
	@Transactional(readOnly = true)
	public PageResult<AuditEntry> search(Filter filter, PageQuery page) {
		return this.store.search(filter, page);
	}

}
