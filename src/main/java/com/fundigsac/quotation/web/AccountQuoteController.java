package com.fundigsac.quotation.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fundigsac.quotation.application.QuoteQueryService;
import com.fundigsac.quotation.application.QuoteViews.QuoteDetailView;
import com.fundigsac.quotation.application.QuoteViews.QuoteSummaryView;
import com.fundigsac.shared.application.PageQuery;
import com.fundigsac.shared.application.PageResult;
import com.fundigsac.shared.web.ApiDocs;

/** Solicitudes de cotización del cliente autenticado (solo las suyas). */
@RestController
@RequestMapping("/api/v1/account/quotes")
@SecurityRequirement(name = ApiDocs.BEARER)
@Tag(name = "Mi cuenta", description = "Perfil, contraseña y eliminación de la cuenta del cliente")
class AccountQuoteController {

	private final QuoteQueryService queries;

	AccountQuoteController(QuoteQueryService queries) {
		this.queries = queries;
	}

	@GetMapping
	@Operation(summary = "Mis solicitudes de cotización")
	PageResult<QuoteSummaryView> mine(@RequestParam(required = false) @Min(0) Integer page,
			@RequestParam(required = false) @Min(1) @Max(100) Integer size) {
		return this.queries.mine(PageQuery.of(page, size));
	}

	@GetMapping("/{folio}")
	@Operation(summary = "Detalle de una solicitud mía", description = "Una solicitud de otra persona responde 404.")
	QuoteDetailView mine(@PathVariable @Size(max = 16) String folio) {
		return this.queries.mineByFolio(folio);
	}

}
