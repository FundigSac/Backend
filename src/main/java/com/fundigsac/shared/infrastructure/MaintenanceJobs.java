package com.fundigsac.shared.infrastructure;

import java.time.Clock;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.IncompleteEventPublications;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fundigsac.shared.application.IdempotencyStore;

/**
 * Tareas de mantenimiento: reintenta los eventos entre módulos que no se completaron (p. ej. por un reinicio) y
 * borra claves de idempotencia vencidas.
 */
@Component
class MaintenanceJobs {

	private static final Logger log = LoggerFactory.getLogger(MaintenanceJobs.class);

	private final IdempotencyStore idempotency;

	private final IncompleteEventPublications incompletePublications;

	private final Clock clock;

	MaintenanceJobs(IdempotencyStore idempotency, IncompleteEventPublications incompletePublications, Clock clock) {
		this.idempotency = idempotency;
		this.incompletePublications = incompletePublications;
		this.clock = clock;
	}

	@Scheduled(initialDelayString = "PT1M", fixedDelayString = "PT1H")
	void purgeExpiredIdempotencyKeys() {
		int removed = this.idempotency.purgeExpired(this.clock.instant());
		if (removed > 0) {
			log.info("Claves de idempotencia vencidas eliminadas: {}", removed);
		}
	}

	@Scheduled(initialDelayString = "PT30S", fixedDelayString = "${fundigsac.events.resubmit-interval:PT1M}")
	void resubmitIncompleteEvents() {
		this.incompletePublications.resubmitIncompletePublicationsOlderThan(Duration.ofMinutes(2));
	}

}
