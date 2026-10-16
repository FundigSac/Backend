package com.fundigsac.quotation.application;

import java.util.UUID;

import org.jspecify.annotations.Nullable;

import com.fundigsac.quotation.application.QuoteViews.QuoteSummaryView;
import com.fundigsac.shared.application.PageQuery;
import com.fundigsac.shared.application.PageResult;

/** Puerto de lectura de bandejas de cotizaciones. */
public interface QuoteReadModel {

	/**
	 * @param query texto en folio, empresa, contacto o correo
	 * @param unassigned solo solicitudes sin responsable
	 */
	record StaffFilter(@Nullable String status, @Nullable UUID assigneeId, boolean unassigned, @Nullable String query) {
	}

	PageResult<QuoteSummaryView> staffInbox(StaffFilter filter, PageQuery page);

	PageResult<QuoteSummaryView> ofCustomer(UUID customerId, PageQuery page);

}
