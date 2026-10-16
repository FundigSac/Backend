package com.fundigsac.privacy.web;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
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
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fundigsac.privacy.application.PrivacyService;
import com.fundigsac.shared.application.PageQuery;
import com.fundigsac.shared.application.PageResult;
import com.fundigsac.shared.web.ApiDocs;
import com.fundigsac.shared.web.ETags;
import com.fundigsac.shared.web.RateLimited;

/** Textos legales vigentes, consentimiento de cookies y derechos del titular (público y gestión interna). */
@RestController
class PrivacyController {

	private static final String TYPES = "privacy-policy, cookie-policy, terms-of-use, b2b-terms o complaints-book";

	record PublishNoticeRequest(@NotBlank @Size(max = 20) String version, @NotBlank @Size(min = 3, max = 200) String title,
			@Size(max = 1000) String summary, @NotBlank @Size(max = 300) String contentUrl, @Size(max = 200_000) String body,
			@NotNull Instant effectiveFrom) {
	}

	record ConsentRequest(UUID consentId, @NotEmpty Map<String, Boolean> choices,
			@NotBlank @Size(max = 20) String policyVersion,
			@NotBlank @Pattern(regexp = "BANNER|PREFERENCES|POLICY|EMBED") String source) {
	}

	record RightsRequest(
			@NotBlank @Pattern(regexp = "ACCESS|RECTIFICATION|CANCELLATION|OPPOSITION|ACCOUNT_DELETION") String type,
			@NotBlank @Size(min = 2, max = 160) String fullName, @NotBlank @Email @Size(max = 254) String email,
			@Pattern(regexp = "^$|DNI|CE|PASSPORT|RUC") String documentType, @Size(max = 12) String documentNumber,
			@NotBlank @Size(min = 10, max = 4000) String details,
			@AssertTrue(message = "Debe aceptar el tratamiento de datos para atender su solicitud.") boolean acceptPrivacyNotice,
			@Parameter(hidden = true) @Size(max = 0, message = "Solicitud no válida.") String website) {
	}

	record ConcludeRequest(boolean accepted, @NotBlank @Size(min = 10, max = 4000) String resolutionNote) {
	}

	private final PrivacyService privacy;

	PrivacyController(PrivacyService privacy) {
		this.privacy = privacy;
	}

	@GetMapping("/api/v1/legal/{type}")
	@Tag(name = "Legal", description = "Textos legales vigentes y su historial de versiones")
	@Operation(summary = "Versión vigente de un texto legal", description = "type: " + TYPES + ".")
	ResponseEntity<PrivacyService.NoticeView> current(@PathVariable @Size(max = 30) String type) {
		return ResponseEntity.ok()
			.cacheControl(CacheControl.maxAge(Duration.ofMinutes(10)).cachePublic())
			.body(this.privacy.currentNotice(type));
	}

	@GetMapping("/api/v1/legal/{type}/versions")
	@Tag(name = "Legal")
	@Operation(summary = "Historial de versiones publicadas")
	List<PrivacyService.NoticeView> versions(@PathVariable @Size(max = 30) String type) {
		return this.privacy.noticeVersions(type);
	}

	@PostMapping("/api/v1/staff/legal/{type}/versions")
	@SecurityRequirement(name = ApiDocs.BEARER)
	@Tag(name = "Personal: legal y privacidad", description = "Publicación de textos legales y solicitudes de derechos")
	@Operation(summary = "Publicar nueva versión de un texto legal", description = "Inmutable: no se edita, se publica otra versión.")
	ResponseEntity<PrivacyService.NoticeView> publish(@PathVariable @Size(max = 30) String type,
			@Valid @RequestBody PublishNoticeRequest r) {
		var view = this.privacy.publishNotice(type, new PrivacyService.PublishNotice(r.version(), r.title(), r.summary(),
				r.contentUrl(), r.body(), r.effectiveFrom()));
		return ResponseEntity.status(HttpStatus.CREATED).body(view);
	}

	@PostMapping("/api/v1/privacy/consents")
	@RateLimited("consent")
	@Tag(name = "Privacidad", description = "Consentimiento de cookies y derechos del titular")
	@Operation(summary = "Registrar decisión de cookies", description = "choices debe tener exactamente necessary (true) y thirdParty. No guarda IP ni identificadores de seguimiento.")
	ResponseEntity<PrivacyService.ConsentReceipt> consent(@Valid @RequestBody ConsentRequest r) {
		var receipt = this.privacy.recordConsent(r.consentId(), r.choices(), r.policyVersion(), r.source());
		return ResponseEntity.status(HttpStatus.CREATED).cacheControl(CacheControl.noStore()).body(receipt);
	}

	@PostMapping("/api/v1/privacy/requests")
	@RateLimited("public-form")
	@Tag(name = "Privacidad")
	@Operation(summary = "Solicitar un derecho del titular (ARCO) o la eliminación de datos", description = "Devuelve el folio ARC-AAAA-NNNNNN. Acepta Idempotency-Key.")
	ResponseEntity<PrivacyService.RequestReceipt> request(
			@RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
			@Valid @RequestBody RightsRequest r) {
		var receipt = this.privacy.submitRequest(new PrivacyService.SubmitRequest(r.type(), r.fullName(), r.email(),
				r.documentType(), r.documentNumber(), r.details()), idempotencyKey);
		return ResponseEntity.created(URI.create("/api/v1/privacy/requests/" + receipt.folio()))
			.cacheControl(CacheControl.noStore())
			.body(receipt);
	}

	@GetMapping("/api/v1/staff/privacy/requests")
	@SecurityRequirement(name = ApiDocs.BEARER)
	@Tag(name = "Personal: legal y privacidad")
	@Operation(summary = "Bandeja de solicitudes de derechos", description = "status: RECEIVED, IN_PROGRESS, RESOLVED o REJECTED.")
	PageResult<PrivacyService.RequestView> requests(@RequestParam(required = false) @Size(max = 12) String status,
			@RequestParam(required = false) @Min(0) Integer page, @RequestParam(required = false) @Min(1) @Max(100) Integer size) {
		return this.privacy.requests(status, PageQuery.of(page, size));
	}

	@GetMapping("/api/v1/staff/privacy/requests/{id}")
	@SecurityRequirement(name = ApiDocs.BEARER)
	@Tag(name = "Personal: legal y privacidad")
	@Operation(summary = "Detalle de una solicitud")
	ResponseEntity<PrivacyService.RequestView> requestDetail(@PathVariable UUID id) {
		var view = this.privacy.request(id);
		return ResponseEntity.ok().eTag(ETags.of(view.version())).cacheControl(CacheControl.noStore()).body(view);
	}

	@PostMapping("/api/v1/staff/privacy/requests/{id}/start")
	@SecurityRequirement(name = ApiDocs.BEARER)
	@Tag(name = "Personal: legal y privacidad")
	@Operation(summary = "Marcar en trámite", description = "Requiere If-Match.")
	ResponseEntity<PrivacyService.RequestView> start(@PathVariable UUID id,
			@RequestHeader(name = "If-Match", required = false) String ifMatch) {
		var view = this.privacy.startRequest(id, ETags.requireVersion(ifMatch));
		return ResponseEntity.ok().eTag(ETags.of(view.version())).body(view);
	}

	@PostMapping("/api/v1/staff/privacy/requests/{id}/conclude")
	@SecurityRequirement(name = ApiDocs.BEARER)
	@Tag(name = "Personal: legal y privacidad")
	@Operation(summary = "Resolver o rechazar con fundamento", description = "Se informa al titular por correo. Requiere If-Match.")
	ResponseEntity<PrivacyService.RequestView> conclude(@PathVariable UUID id,
			@RequestHeader(name = "If-Match", required = false) String ifMatch, @Valid @RequestBody ConcludeRequest r) {
		var view = this.privacy.concludeRequest(id, r.accepted(), r.resolutionNote(), ETags.requireVersion(ifMatch));
		return ResponseEntity.ok().eTag(ETags.of(view.version())).body(view);
	}

}
