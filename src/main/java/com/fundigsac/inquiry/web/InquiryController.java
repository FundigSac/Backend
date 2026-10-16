package com.fundigsac.inquiry.web;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fundigsac.inquiry.application.InquiryService;
import com.fundigsac.shared.application.PageQuery;
import com.fundigsac.shared.application.PageResult;
import com.fundigsac.shared.web.ApiDocs;
import com.fundigsac.shared.web.ETags;
import com.fundigsac.shared.web.RateLimited;

/** Formulario público de contacto y bandeja de consultas del personal. */
@RestController
class InquiryController {

	record InquiryRequest(
			@Pattern(regexp = "GENERAL|TECHNICAL|COMMERCIAL|DOCUMENTATION|MEETING|AFTER_SALES|OTHER", message = "Motivo no válido.") String reason,
			@NotBlank @Size(min = 2, max = 120) String fullName, @NotBlank @Size(min = 2, max = 160) String companyName,
			@Pattern(regexp = "^$|^\\d{11}$", message = "El RUC debe tener 11 dígitos.") String ruc,
			@Size(max = 120) String position, @NotBlank @Email @Size(max = 254) String email,
			@NotBlank @Size(max = 24) String phone, @NotBlank @Size(min = 10, max = 4000) String message,
			@Size(max = 200) String requestedDocument, @NotBlank @Size(max = 20) String privacyNoticeVersion,
			@AssertTrue(message = "Debe aceptar el aviso de privacidad.") boolean acceptPrivacyNotice,
			@Parameter(hidden = true) @Size(max = 0, message = "Solicitud no válida.") String website) {
	}

	record AssignRequest(@NotNull UUID assigneeId) {
	}

	record StatusRequest(@NotBlank @Size(max = 12) String status, @Size(max = 1000) String note) {
	}

	private final InquiryService inquiries;

	InquiryController(InquiryService inquiries) {
		this.inquiries = inquiries;
	}

	@PostMapping("/api/v1/inquiries")
	@RateLimited("public-form")
	@Tag(name = "Contacto", description = "Consultas generales del formulario de contacto")
	@Operation(summary = "Enviar consulta", description = "Devuelve el folio CON-AAAA-NNNNNN. Acepta Idempotency-Key.")
	ResponseEntity<Map<String, Object>> submit(
			@RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
			@Valid @RequestBody InquiryRequest r) {
		var receipt = this.inquiries.submit(new InquiryService.SubmitInquiry(r.reason() == null ? "GENERAL" : r.reason(),
				r.fullName(), r.companyName(), r.ruc(), r.position(), r.email(), r.phone(), r.message(), r.requestedDocument(),
				r.privacyNoticeVersion()), idempotencyKey);
		return ResponseEntity.created(URI.create("/api/v1/inquiries/" + receipt.folio()))
			.cacheControl(CacheControl.noStore())
			.body(Map.of("folio", receipt.folio(), "status", receipt.status(), "receivedAt", receipt.receivedAt(), "message",
					"Consulta registrada. Le responderemos por correo o teléfono."));
	}

	@GetMapping("/api/v1/staff/inquiries")
	@SecurityRequirement(name = ApiDocs.BEARER)
	@Tag(name = "Personal: consultas", description = "Bandeja de consultas de contacto")
	@Operation(summary = "Bandeja de consultas", description = "status: RECEIVED, IN_PROGRESS, ANSWERED o CLOSED.")
	PageResult<InquiryService.InquirySummary> inbox(@RequestParam(required = false) @Size(max = 12) String status,
			@RequestParam(required = false) @Size(max = 100) String q, @RequestParam(required = false) @Min(0) Integer page,
			@RequestParam(required = false) @Min(1) @Max(100) Integer size) {
		return this.inquiries.inbox(status, q, PageQuery.of(page, size));
	}

	@GetMapping("/api/v1/staff/inquiries/{id}")
	@SecurityRequirement(name = ApiDocs.BEARER)
	@Tag(name = "Personal: consultas")
	@Operation(summary = "Detalle de una consulta con historial")
	ResponseEntity<InquiryService.InquiryDetail> detail(@PathVariable UUID id) {
		var view = this.inquiries.detail(id);
		return ResponseEntity.ok().eTag(ETags.of(view.version())).body(view);
	}

	@PostMapping("/api/v1/staff/inquiries/{id}/assign")
	@SecurityRequirement(name = ApiDocs.BEARER)
	@Tag(name = "Personal: consultas")
	@Operation(summary = "Asignar responsable", description = "Requiere If-Match.")
	ResponseEntity<InquiryService.InquiryDetail> assign(@PathVariable UUID id,
			@RequestHeader(name = "If-Match", required = false) String ifMatch, @Valid @RequestBody AssignRequest request) {
		var view = this.inquiries.assign(id, request.assigneeId(), ETags.requireVersion(ifMatch));
		return ResponseEntity.ok().eTag(ETags.of(view.version())).body(view);
	}

	@PostMapping("/api/v1/staff/inquiries/{id}/status")
	@SecurityRequirement(name = ApiDocs.BEARER)
	@Tag(name = "Personal: consultas")
	@Operation(summary = "Cambiar estado", description = "Requiere If-Match.")
	ResponseEntity<InquiryService.InquiryDetail> status(@PathVariable UUID id,
			@RequestHeader(name = "If-Match", required = false) String ifMatch, @Valid @RequestBody StatusRequest request) {
		var view = this.inquiries.changeStatus(id, request.status(), request.note(), ETags.requireVersion(ifMatch));
		return ResponseEntity.ok().eTag(ETags.of(view.version())).body(view);
	}

}
