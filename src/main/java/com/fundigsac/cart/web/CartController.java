package com.fundigsac.cart.web;

import java.net.URI;
import java.time.LocalDate;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fundigsac.cart.application.CartService;
import com.fundigsac.quotation.api.QuoteSubmission;
import com.fundigsac.shared.web.ApiDocs;
import com.fundigsac.shared.web.RateLimited;

/** Carrito de cotización del cliente autenticado. */
@RestController
@RequestMapping("/api/v1/cart")
@SecurityRequirement(name = ApiDocs.BEARER)
@Tag(name = "Carrito de cotización", description = "Productos a cotizar del cliente (sin precios)")
class CartController {

	record AddItemRequest(@NotBlank @Size(max = 120) String productSlug, @Size(max = 120) String variantLabel,
			@Min(1) @Max(999) int quantity, @Size(max = 500) String note) {
	}

	record UpdateItemRequest(@Min(1) @Max(999) int quantity, @Size(max = 500) String note) {
	}

	record CheckoutRequest(@Size(max = 160) String companyName,
			@Pattern(regexp = "^$|^\\d{11}$", message = "El RUC debe tener 11 dígitos.") String ruc,
			@Size(max = 120) String contactName, @Size(max = 120) String position, @Size(max = 24) String phone,
			@Size(max = 120) String projectType, @Size(max = 60) String deliveryDepartment,
			@Size(max = 200) String deliveryPlace, LocalDate requestedDeliveryDate, @Size(max = 4000) String message,
			@NotBlank @Size(max = 20) String privacyNoticeVersion,
			@AssertTrue(message = "Debe aceptar el aviso de privacidad.") boolean acceptPrivacyNotice) {
	}

	private final CartService cart;

	CartController(CartService cart) {
		this.cart = cart;
	}

	@GetMapping
	@Operation(summary = "Ver mi carrito")
	CartService.CartView get() {
		return this.cart.get();
	}

	@PostMapping("/items")
	@Operation(summary = "Agregar producto", description = "Si la pieza y medida ya están, suma la cantidad (máximo 999).")
	CartService.CartView add(@Valid @RequestBody AddItemRequest request) {
		return this.cart.add(request.productSlug(), request.variantLabel(), request.quantity(), request.note());
	}

	@PatchMapping("/items/{itemId}")
	@Operation(summary = "Cambiar cantidad o nota")
	CartService.CartView update(@PathVariable UUID itemId, @Valid @RequestBody UpdateItemRequest request) {
		return this.cart.update(itemId, request.quantity(), request.note());
	}

	@DeleteMapping("/items/{itemId}")
	@Operation(summary = "Quitar producto")
	CartService.CartView remove(@PathVariable UUID itemId) {
		return this.cart.remove(itemId);
	}

	@DeleteMapping
	@Operation(summary = "Vaciar carrito")
	CartService.CartView clear() {
		return this.cart.clear();
	}

	@PostMapping("/submit")
	@RateLimited("public-form")
	@Operation(summary = "Enviar a cotizar", description = "Crea la solicitud COT con los productos del carrito y lo vacía. Acepta Idempotency-Key.")
	ResponseEntity<QuoteSubmission.Receipt> submit(@RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
			@Valid @RequestBody CheckoutRequest r) {
		QuoteSubmission.Receipt receipt = this.cart.submit(new CartService.Checkout(r.companyName(), r.ruc(), r.contactName(),
				r.position(), r.phone(), r.projectType(), r.deliveryDepartment(), r.deliveryPlace(), r.requestedDeliveryDate(),
				r.message(), r.privacyNoticeVersion()), idempotencyKey);
		return ResponseEntity.created(URI.create("/api/v1/account/quotes/" + receipt.folio())).body(receipt);
	}

}
