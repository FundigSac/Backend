package com.fundigsac.quotation.web;

import java.util.Locale;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fundigsac.quotation.application.QuoteQueryService;
import com.fundigsac.quotation.application.QuoteReadModel;
import com.fundigsac.quotation.application.QuoteViews.QuoteDetailView;
import com.fundigsac.quotation.application.QuoteViews.QuoteSummaryView;
import com.fundigsac.quotation.application.StaffQuoteService;
import com.fundigsac.shared.application.PageQuery;
import com.fundigsac.shared.application.PageResult;
import com.fundigsac.shared.web.ApiDocs;
import com.fundigsac.shared.web.ETags;

/** Bandeja de cotizaciones de Ventas. */
@RestController
@RequestMapping("/api/v1/staff/quotes")
@SecurityRequirement(name = ApiDocs.BEARER)
@Tag(name = "Personal: cotizaciones", description = "Bandeja, asignación y estados de atención")
class StaffQuoteController {

	record AssignRequest(@NotNull UUID assigneeId) {
	}

	record StatusRequest(@NotBlank @Size(max = 12) String status, @Size(max = 1000) String note) {
	}

	private final QuoteQueryService queries;

	private final StaffQuoteService quotes;

	StaffQuoteController(QuoteQueryService queries, StaffQuoteService quotes) {
		this.queries = queries;
		this.quotes = quotes;
	}

	@GetMapping
	@Operation(summary = "Bandeja de solicitudes", description = "status: RECEIVED, ASSIGNED, IN_REVIEW, RESPONDED, CLOSED o CANCELLED.")
	PageResult<QuoteSummaryView> inbox(@RequestParam(required = false) @Size(max = 12) String status,
			@RequestParam(required = false) UUID assigneeId, @RequestParam(defaultValue = "false") boolean unassigned,
			@RequestParam(required = false) @Size(max = 100) String q, @RequestParam(required = false) @Min(0) Integer page,
			@RequestParam(required = false) @Min(1) @Max(100) Integer size) {
		String normalized = status == null || status.isBlank() ? null : status.strip().toUpperCase(Locale.ROOT);
		String query = q == null || q.isBlank() ? null : q.strip();
		return this.queries.inbox(new QuoteReadModel.StaffFilter(normalized, assigneeId, unassigned, query),
				PageQuery.of(page, size));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Detalle con historial", description = "Devuelve ETag para modificarla.")
	ResponseEntity<QuoteDetailView> detail(@PathVariable UUID id) {
		QuoteDetailView view = this.queries.detail(id);
		return ResponseEntity.ok().eTag(ETags.of(view.version())).body(view);
	}

	@PostMapping("/{id}/assign")
	@Operation(summary = "Asignar responsable", description = "Requiere If-Match. La persona debe tener permiso quote:manage.")
	ResponseEntity<QuoteDetailView> assign(@PathVariable UUID id, @RequestHeader(name = "If-Match", required = false) String ifMatch,
			@Valid @RequestBody AssignRequest request) {
		QuoteDetailView view = this.quotes.assign(id, request.assigneeId(), ETags.requireVersion(ifMatch));
		return ResponseEntity.ok().eTag(ETags.of(view.version())).body(view);
	}

	@PostMapping("/{id}/status")
	@Operation(summary = "Cambiar estado", description = "Requiere If-Match. Cancelar exige motivo.")
	ResponseEntity<QuoteDetailView> status(@PathVariable UUID id, @RequestHeader(name = "If-Match", required = false) String ifMatch,
			@Valid @RequestBody StatusRequest request) {
		QuoteDetailView view = this.quotes.changeStatus(id, request.status(), request.note(), ETags.requireVersion(ifMatch));
		return ResponseEntity.ok().eTag(ETags.of(view.version())).body(view);
	}

}
