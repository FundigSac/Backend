package com.fundigsac.complaint.web;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
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

import com.fundigsac.complaint.application.ComplaintService;
import com.fundigsac.complaint.application.ComplaintViews.ComplaintSheet;
import com.fundigsac.complaint.application.ComplaintViews.ComplaintSummary;
import com.fundigsac.complaint.application.ComplaintViews.Receipt;
import com.fundigsac.shared.application.PageQuery;
import com.fundigsac.shared.application.PageResult;
import com.fundigsac.shared.web.ApiDocs;
import com.fundigsac.shared.web.ETags;
import com.fundigsac.shared.web.RateLimited;

/** Libro de Reclamaciones virtual: registro público, constancia y bandeja de Atención Legal. */
@RestController
class ComplaintController {

	record ComplaintRequest(
			@NotBlank @Pattern(regexp = "CLAIM|GRIEVANCE", message = "Use CLAIM (reclamo) o GRIEVANCE (queja).") String kind,
			@NotBlank @Size(min = 2, max = 160) String consumerName,
			@NotBlank @Pattern(regexp = "DNI|CE|PASSPORT|RUC") String documentType,
			@NotBlank @Size(max = 12) String documentNumber, @NotBlank @Email @Size(max = 254) String email,
			@NotBlank @Size(max = 24) String phone, @NotBlank @Size(min = 5, max = 300) String address, boolean minor,
			@Size(max = 160) String guardianName, @Pattern(regexp = "^$|DNI|CE|PASSPORT") String guardianDocumentType,
			@Size(max = 12) String guardianDocumentNumber, @Pattern(regexp = "^$|PRODUCT|SERVICE") String goodType,
			@NotBlank @Size(min = 5, max = 1000) String goodDescription,
			@DecimalMin("0") @DecimalMax("99999999.99") @Digits(integer = 8, fraction = 2) BigDecimal claimedAmount,
			@Pattern(regexp = "^$|PEN|USD") String currency, @Size(max = 200) String serviceLocation,
			@NotNull @PastOrPresent LocalDate incidentDate, @Size(max = 60) String relatedReference,
			@NotBlank @Size(min = 20, max = 4000) String facts, @NotBlank @Size(min = 5, max = 2000) String consumerRequest,
			@NotBlank @Pattern(regexp = "EMAIL|POSTAL_ADDRESS") String responseChannel,
			@NotBlank @Size(max = 20) String privacyNoticeVersion,
			@AssertTrue(message = "Debe aceptar el tratamiento de datos (Ley N.° 29733).") boolean acceptPrivacyNotice,
			@Parameter(hidden = true) @Size(max = 0, message = "Solicitud no válida.") String website) {

		ComplaintService.FileComplaint toCommand() {
			return new ComplaintService.FileComplaint(this.kind, this.consumerName, this.documentType, this.documentNumber,
					this.email, this.phone, this.address, this.minor, this.guardianName, this.guardianDocumentType,
					this.guardianDocumentNumber, this.goodType, this.goodDescription, this.claimedAmount, this.currency,
					this.serviceLocation, this.incidentDate, this.relatedReference, this.facts, this.consumerRequest,
					this.responseChannel, this.privacyNoticeVersion);
		}

	}

	record AssignRequest(@NotNull UUID assigneeId) {
	}

	record NoteRequest(@NotBlank @Size(min = 3, max = 1000) String note) {
	}

	record AnswerRequest(@NotBlank @Size(min = 20, max = 8000) String response) {
	}

	private final ComplaintService complaints;

	ComplaintController(ComplaintService complaints) {
		this.complaints = complaints;
	}

	@PostMapping("/api/v1/complaints")
	@RateLimited("public-form")
	@Tag(name = "Libro de Reclamaciones", description = "Hoja de reclamación virtual y constancia")
	@Operation(summary = "Registrar reclamo o queja", description = """
			Asigna el número correlativo REC-AAAA-NNNNNN y la fecha límite de respuesta (días hábiles). Devuelve el token \
			para consultar la constancia. Acepta Idempotency-Key.""")
	ResponseEntity<Receipt> file(@RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
			@Valid @RequestBody ComplaintRequest request) {
		Receipt receipt = this.complaints.file(request.toCommand(), idempotencyKey);
		return ResponseEntity.created(URI.create("/api/v1/complaints/" + receipt.folio() + "/receipt"))
			.cacheControl(CacheControl.noStore())
			.body(receipt);
	}

	@GetMapping("/api/v1/complaints/{folio}/receipt")
	@RateLimited("receipt")
	@Tag(name = "Libro de Reclamaciones")
	@Operation(summary = "Constancia de la hoja", description = "Requiere X-Receipt-Token. Datos personales enmascarados.")
	ResponseEntity<ComplaintSheet> receipt(@PathVariable @Size(max = 16) String folio,
			@RequestHeader(name = "X-Receipt-Token", required = false) String token) {
		return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(this.complaints.receiptSheet(folio, token));
	}

	@GetMapping("/api/v1/staff/complaints")
	@SecurityRequirement(name = ApiDocs.BEARER)
	@Tag(name = "Personal: reclamaciones", description = "Bandeja legal con vencimientos")
	@Operation(summary = "Bandeja de hojas", description = "Ordenada por fecha límite. overdue=true muestra solo las vencidas sin respuesta.")
	PageResult<ComplaintSummary> inbox(@RequestParam(required = false) @Size(max = 12) String status,
			@RequestParam(defaultValue = "false") boolean overdue, @RequestParam(required = false) @Size(max = 100) String q,
			@RequestParam(required = false) @Min(0) Integer page, @RequestParam(required = false) @Min(1) @Max(100) Integer size) {
		return this.complaints.inbox(status, overdue, q, PageQuery.of(page, size));
	}

	@GetMapping("/api/v1/staff/complaints/{id}")
	@SecurityRequirement(name = ApiDocs.BEARER)
	@Tag(name = "Personal: reclamaciones")
	@Operation(summary = "Hoja completa con historial")
	ResponseEntity<ComplaintSheet> detail(@PathVariable UUID id) {
		ComplaintSheet sheet = this.complaints.detail(id);
		return ResponseEntity.ok().eTag(ETags.of(sheet.version())).cacheControl(CacheControl.noStore()).body(sheet);
	}

	@PostMapping("/api/v1/staff/complaints/{id}/assign")
	@SecurityRequirement(name = ApiDocs.BEARER)
	@Tag(name = "Personal: reclamaciones")
	@Operation(summary = "Asignar responsable", description = "Requiere If-Match.")
	ResponseEntity<ComplaintSheet> assign(@PathVariable UUID id, @RequestHeader(name = "If-Match", required = false) String ifMatch,
			@Valid @RequestBody AssignRequest request) {
		return withEtag(this.complaints.assign(id, request.assigneeId(), ETags.requireVersion(ifMatch)));
	}

	@PostMapping("/api/v1/staff/complaints/{id}/notes")
	@SecurityRequirement(name = ApiDocs.BEARER)
	@Tag(name = "Personal: reclamaciones")
	@Operation(summary = "Agregar nota interna", description = "Requiere If-Match.")
	ResponseEntity<ComplaintSheet> note(@PathVariable UUID id, @RequestHeader(name = "If-Match", required = false) String ifMatch,
			@Valid @RequestBody NoteRequest request) {
		return withEtag(this.complaints.addNote(id, request.note(), ETags.requireVersion(ifMatch)));
	}

	@PostMapping("/api/v1/staff/complaints/{id}/answer")
	@SecurityRequirement(name = ApiDocs.BEARER)
	@Tag(name = "Personal: reclamaciones")
	@Operation(summary = "Emitir respuesta formal", description = "Una sola vez; queda registrada si fue fuera de plazo y se envía al consumidor. Requiere If-Match.")
	ResponseEntity<ComplaintSheet> answer(@PathVariable UUID id, @RequestHeader(name = "If-Match", required = false) String ifMatch,
			@Valid @RequestBody AnswerRequest request) {
		return withEtag(this.complaints.answer(id, request.response(), ETags.requireVersion(ifMatch)));
	}

	@PostMapping("/api/v1/staff/complaints/{id}/close")
	@SecurityRequirement(name = ApiDocs.BEARER)
	@Tag(name = "Personal: reclamaciones")
	@Operation(summary = "Cerrar hoja respondida", description = "Requiere If-Match.")
	ResponseEntity<ComplaintSheet> close(@PathVariable UUID id, @RequestHeader(name = "If-Match", required = false) String ifMatch) {
		return withEtag(this.complaints.close(id, ETags.requireVersion(ifMatch)));
	}

	private static ResponseEntity<ComplaintSheet> withEtag(ComplaintSheet sheet) {
		return ResponseEntity.ok().eTag(ETags.of(sheet.version())).cacheControl(CacheControl.noStore()).body(sheet);
	}

}
