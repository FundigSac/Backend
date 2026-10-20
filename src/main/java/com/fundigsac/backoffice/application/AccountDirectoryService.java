package com.fundigsac.backoffice.application;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fundigsac.backoffice.application.AccountReadModel.ActivityFilter;
import com.fundigsac.backoffice.application.AccountReadModel.Filter;
import com.fundigsac.backoffice.application.AccountReadModel.Sort;
import com.fundigsac.backoffice.application.AccountViews.AccountPage;
import com.fundigsac.backoffice.application.AccountViews.AccountRow;
import com.fundigsac.backoffice.application.AccountViews.AccountStats;
import com.fundigsac.backoffice.application.AccountViews.ActivityRow;
import com.fundigsac.shared.application.PageQuery;
import com.fundigsac.shared.application.PageResult;
import com.fundigsac.shared.domain.NotFoundException;
import com.fundigsac.shared.domain.PeruTime;
import com.fundigsac.shared.domain.ValidationException;
import com.fundigsac.shared.security.Authz;

/**
 * Directorio de cuentas del panel interno (permiso users:manage). Valida los filtros contra listas cerradas antes de
 * llegar al SQL: un valor desconocido responde 400 en lugar de devolver una lista vacía que confunda.
 */
@Service
public class AccountDirectoryService {

	private static final Set<String> STATUSES = Set.of("ACTIVE", "PENDING_VERIFICATION", "DISABLED");

	private static final Set<String> KINDS = Set.of("CUSTOMER", "STAFF");

	private static final Set<String> METHODS = Set.of("EMAIL", "GOOGLE", "FACEBOOK");

	private static final Set<String> ROLES = Set.of("CUSTOMER", "ADMINISTRATOR", "SALES", "CATALOG_EDITOR",
			"CATALOG_REVIEWER", "LEGAL_SUPPORT", "READ_ONLY");

	private static final Set<String> ACTIVITY = Set.of("SIGNUP", "LOGIN", "VERIFY", "QUOTE", "PASSWORD", "DISABLE",
			"ENABLE", "ROLES", "DELETE", "INVITE");

	/** "Todo el historial" en el gráfico diario se limita a los últimos 120 días para que siga siendo legible. */
	private static final int ALL_TIME_DAYS = 120;

	private final AccountReadModel accounts;

	private final Clock clock;

	AccountDirectoryService(AccountReadModel accounts, Clock clock) {
		this.accounts = accounts;
		this.clock = clock;
	}

	/** Filtros tal como llegan de la consulta HTTP. */
	public record Query(@Nullable String q, @Nullable String status, @Nullable String kind, @Nullable String method,
			@Nullable String role, @Nullable String range, @Nullable String sort, boolean desc) {
	}

	@PreAuthorize(Authz.USERS_MANAGE)
	@Transactional(readOnly = true)
	public AccountPage list(Query query, PageQuery page) {
		Instant now = this.clock.instant();
		Integer days = days(query.range());
		Filter filter = new Filter(text(query.q()), pick("status", query.status(), STATUSES), pick("kind", query.kind(), KINDS),
				pick("method", query.method(), METHODS), pick("role", query.role(), ROLES),
				days == null ? null : now.minus(Duration.ofDays(days)), sort(query.sort()), query.desc());
		return this.accounts.accounts(filter, page);
	}

	@PreAuthorize(Authz.USERS_MANAGE)
	@Transactional(readOnly = true)
	public AccountRow get(UUID id) {
		return this.accounts.account(id)
			.orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "La cuenta no existe."));
	}

	@PreAuthorize(Authz.USERS_MANAGE)
	@Transactional(readOnly = true)
	public AccountStats stats(@Nullable String range) {
		Instant now = this.clock.instant();
		Integer days = days(range);
		int span = days == null ? ALL_TIME_DAYS : days;
		// Los días del gráfico se cuentan en hora de Lima: el primero empieza a medianoche.
		LocalDate today = LocalDate.ofInstant(now, PeruTime.ZONE);
		Instant from = days == null ? Instant.EPOCH : today.minusDays(span - 1L).atStartOfDay(PeruTime.ZONE).toInstant();
		Instant previousFrom = days == null ? Instant.EPOCH : from.minus(Duration.ofDays(span));
		return this.accounts.stats(from, previousFrom, span, now);
	}

	@PreAuthorize(Authz.USERS_MANAGE)
	@Transactional(readOnly = true)
	public PageResult<ActivityRow> activity(@Nullable String q, @Nullable String type, @Nullable UUID userId, PageQuery page) {
		return this.accounts.activity(new ActivityFilter(text(q), pick("type", type, ACTIVITY), userId), page);
	}

	private static @Nullable String text(@Nullable String raw) {
		return raw == null || raw.isBlank() ? null : raw.strip();
	}

	private static @Nullable String pick(String field, @Nullable String raw, Set<String> allowed) {
		if (raw == null || raw.isBlank() || "all".equalsIgnoreCase(raw.strip())) {
			return null;
		}
		String value = raw.strip().toUpperCase(Locale.ROOT);
		if (!allowed.contains(value)) {
			throw new ValidationException(field, "INVALID_FILTER", "Filtro no válido.");
		}
		return value;
	}

	private static @Nullable Integer days(@Nullable String range) {
		if (range == null || range.isBlank() || "all".equalsIgnoreCase(range)) {
			return null;
		}
		return switch (range.strip()) {
			case "7d" -> 7;
			case "30d" -> 30;
			case "90d" -> 90;
			default -> throw new ValidationException("range", "INVALID_FILTER", "Periodo no válido (7d, 30d, 90d o all).");
		};
	}

	private static Sort sort(@Nullable String raw) {
		if (raw == null || raw.isBlank()) {
			return Sort.REGISTERED_AT;
		}
		return switch (raw.strip()) {
			case "name" -> Sort.NAME;
			case "registeredAt" -> Sort.REGISTERED_AT;
			case "lastActiveAt" -> Sort.LAST_ACTIVE_AT;
			case "quotes" -> Sort.QUOTES;
			case "status" -> Sort.STATUS;
			default -> throw new ValidationException("sort", "INVALID_FILTER", "Orden no válido.");
		};
	}

}
