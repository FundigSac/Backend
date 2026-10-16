package com.fundigsac.audit.web;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fundigsac.audit.application.AuditTrail;
import com.fundigsac.shared.application.PageQuery;
import com.fundigsac.shared.application.PageResult;
import com.fundigsac.shared.web.ApiDocs;

/** Consulta de la auditoría (permiso audit:read). */
@RestController
@SecurityRequirement(name = ApiDocs.BEARER)
@Tag(name = "Personal: auditoría", description = "Registro inmutable de acciones relevantes")
class AuditController {

	private final AuditTrail audit;

	AuditController(AuditTrail audit) {
		this.audit = audit;
	}

	@GetMapping("/api/v1/staff/audit")
	@Operation(summary = "Buscar en la auditoría", description = "resourceType: USER, PRODUCT, DOCUMENT, QUOTE, INQUIRY, COMPLAINT, DATA_SUBJECT_REQUEST o LEGAL_NOTICE. Fechas ISO-8601 (UTC).")
	PageResult<AuditTrail.AuditEntry> search(@RequestParam(required = false) @Size(max = 40) String resourceType,
			@RequestParam(required = false) @Size(max = 80) String resourceId, @RequestParam(required = false) UUID actorId,
			@RequestParam(required = false) @Size(max = 60) String action, @RequestParam(required = false) Instant from,
			@RequestParam(required = false) Instant to, @RequestParam(required = false) @Min(0) Integer page,
			@RequestParam(required = false) @Min(1) @Max(100) Integer size) {
		return this.audit.search(new AuditTrail.Filter(upper(resourceType), blank(resourceId), actorId, upper(action), from, to),
				PageQuery.of(page, size));
	}

	private static String upper(String value) {
		return value == null || value.isBlank() ? null : value.strip().toUpperCase(Locale.ROOT);
	}

	private static String blank(String value) {
		return value == null || value.isBlank() ? null : value.strip();
	}

}
