package com.fundigsac.quotation.web;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import org.jspecify.annotations.Nullable;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fundigsac.quotation.api.QuoteSubmission;
import com.fundigsac.quotation.application.QuoteQueryService;
import com.fundigsac.quotation.application.QuoteViews.QuoteReceiptView;
import com.fundigsac.shared.application.CurrentActor;
import com.fundigsac.shared.web.RateLimited;

/** Solicitud pública de cotización (sin cuenta) y su constancia. */
@RestController
@RequestMapping("/api/v1/quotes")
@Tag(name = "Cotizaciones", description = "Solicitud B2B con folio y constancia")
class QuoteController {

	record ContactRequest(@NotBlank @Size(min = 2, max = 160) String companyName,
			@NotBlank @Pattern(regexp = "^\\d{11}$", message = "El RUC debe tener 11 dígitos.") String ruc,
			@NotBlank @Size(min = 2, max = 120) String contactName, @Size(max = 120) String position,
			@NotBlank @Email @Size(max = 254) String email, @NotBlank @Size(max = 24) String phone) {
	}

	record ProjectRequest(@Size(max = 120) String projectType, @Size(max = 60) String deliveryDepartment,
			@Size(max = 200) String deliveryPlace, LocalDate requestedDeliveryDate, @Size(max = 4000) String message) {
	}

	record ItemRequest(@Size(max = 120) String productSlug, @Size(max = 200) String productName,
			@Size(max = 120) String variantLabel,
			@NotNull @Positive @DecimalMax("1000000") @Digits(integer = 7, fraction = 3) BigDecimal quantity,
			@NotBlank @Size(max = 10) String unit, @Size(max = 10) List<@Size(max = 120) String> addons,
			@Size(max = 1000) String notes) {
	}

	record SubmitQuoteRequest(
			@NotBlank @Pattern(regexp = "GENERAL_FORM|PRODUCT_FORM|PROCUREMENT", message = "Origen no válido.") String origin,
			@NotNull @Valid ContactRequest contact, @Valid ProjectRequest project,
			@Size(max = 50) List<@Valid ItemRequest> items,
			@NotBlank @Size(max = 20) String privacyNoticeVersion,
			@AssertTrue(message = "Debe aceptar el aviso de privacidad.") boolean acceptPrivacyNotice,
			@Parameter(hidden = true) @Size(max = 0, message = "Solicitud no válida.") String website) {

		QuoteSubmission.Request toRequest(@Nullable UUID customerId) {
			var c = this.contact;
			var p = this.project == null ? new ProjectRequest(null, null, null, null, null) : this.project;
			return new QuoteSubmission.Request(this.origin,
					new QuoteSubmission.Contact(c.companyName(), c.ruc(), c.contactName(), c.position(), c.email(), c.phone()),
					new QuoteSubmission.Project(p.projectType(), p.deliveryDepartment(), p.deliveryPlace(),
							p.requestedDeliveryDate(), p.message()),
					this.items == null ? List.of() : this.items.stream()
						.map((i) -> new QuoteSubmission.Line(i.productSlug(), i.productName(), i.variantLabel(), i.quantity(),
								i.unit(), i.addons(), i.notes()))
						.toList(),
					this.privacyNoticeVersion, customerId);
		}

	}

	record ReceiptResponse(String folio, String status, Instant receivedAt, String receiptToken, String receiptUrl,
			String message) {

		static ReceiptResponse of(QuoteSubmission.Receipt r) {
			return new ReceiptResponse(r.folio(), r.status(), r.receivedAt(), r.receiptToken(),
					"/api/v1/quotes/" + r.folio() + "/receipt",
					"Solicitud registrada. Ventas la revisará y le responderá por correo. Guarde su folio.");
		}

	}

	private final QuoteSubmission submission;

	private final QuoteQueryService queries;

	private final CurrentActor actor;

	QuoteController(QuoteSubmission submission, QuoteQueryService queries, CurrentActor actor) {
		this.submission = submission;
		this.queries = queries;
		this.actor = actor;
	}

	@PostMapping
	@RateLimited("public-form")
	@Operation(summary = "Solicitar cotización", description = """
			Registra la solicitud y devuelve folio y token de constancia. Envíe `Idempotency-Key` para que un reintento \
			devuelva el mismo folio. Con sesión de cliente, la solicitud queda vinculada a su cuenta.""")
	ResponseEntity<ReceiptResponse> submit(
			@Parameter(description = "Clave única por intento (8-128 caracteres)") @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
			@Valid @RequestBody SubmitQuoteRequest request) {
		QuoteSubmission.Receipt receipt = this.submission.submit(request.toRequest(customerId()), idempotencyKey);
		return ResponseEntity.created(URI.create("/api/v1/quotes/" + receipt.folio() + "/receipt"))
			.cacheControl(CacheControl.noStore())
			.body(ReceiptResponse.of(receipt));
	}

	@GetMapping("/{folio}/receipt")
	@RateLimited("receipt")
	@Operation(summary = "Ver constancia", description = "Requiere la cabecera X-Receipt-Token entregada al registrar. Sin token válido responde 404.")
	ResponseEntity<QuoteReceiptView> receipt(@PathVariable @Size(max = 16) String folio,
			@RequestHeader(name = "X-Receipt-Token", required = false) String token) {
		return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(this.queries.receipt(folio, token));
	}

	private @Nullable UUID customerId() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		boolean customer = auth != null && !(auth instanceof AnonymousAuthenticationToken)
				&& auth.getAuthorities().stream().anyMatch((a) -> "ROLE_CUSTOMER".equals(a.getAuthority()));
		return customer ? this.actor.userId().orElse(null) : null;
	}

}
