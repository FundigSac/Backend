package com.fundigsac.backoffice.application;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import com.fundigsac.backoffice.application.AccountViews.AccountPage;
import com.fundigsac.backoffice.application.AccountViews.AccountRow;
import com.fundigsac.backoffice.application.AccountViews.AccountStats;
import com.fundigsac.backoffice.application.AccountViews.ActivityRow;
import com.fundigsac.shared.application.PageQuery;
import com.fundigsac.shared.application.PageResult;

/** Puerto de lectura del directorio de cuentas. */
public interface AccountReadModel {

	enum Sort {
		NAME, REGISTERED_AT, LAST_ACTIVE_AT, QUOTES, STATUS
	}

	/**
	 * Filtros ya validados (valores en mayúsculas como en la base de datos; nulo = todos).
	 *
	 * @param query texto en nombre, correo, empresa, RUC o celular (todas las palabras deben aparecer)
	 * @param registeredFrom solo cuentas creadas desde este instante
	 */
	record Filter(@Nullable String query, @Nullable String status, @Nullable String kind, @Nullable String method,
			@Nullable String role, @Nullable Instant registeredFrom, Sort sort, boolean descending) {
	}

	/** @param type tipo de hecho (SIGNUP, LOGIN, …) o nulo para todos */
	record ActivityFilter(@Nullable String query, @Nullable String type, @Nullable UUID userId) {
	}

	AccountPage accounts(Filter filter, PageQuery page);

	Optional<AccountRow> account(UUID id);

	AccountStats stats(Instant from, Instant previousFrom, int days, Instant now);

	PageResult<ActivityRow> activity(ActivityFilter filter, PageQuery page);

}
