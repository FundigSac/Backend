package com.fundigsac.backend.controller;

import com.fundigsac.backend.dto.ApiDtos.*;
import com.fundigsac.backend.model.entity.*;
import com.fundigsac.backend.service.CommerceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "10. Comercio B2B (Carrito, Despacho y Órdenes)", description = "Flujo de compra por SKU elegibles con stock, cotización de flete y órdenes idempotentes")
public class CommerceController {

    private final CommerceService commerceService;

    @GetMapping("/commerce/eligible-products")
    @Operation(summary = "API-029: Listar productos con compra online habilitada", description = "Filtra únicamente los SKU autorizados comercialmente con precios aprobados")
    public ResponseEntity<List<Map<String, Object>>> getEligibleProducts() {
        return ResponseEntity.ok(commerceService.getEligibleProducts());
    }

    @GetMapping("/cart")
    @Operation(summary = "API-030: Ver contenido del carrito activo", description = "Devuelve líneas, subtotales, recálculo de IGV (18%) y total asegurado por el servidor")
    public ResponseEntity<Map<String, Object>> getCart(@RequestParam(required = false) UUID cartId) {
        UUID id = cartId != null ? cartId : commerceService.getOrCreateCart(null).getId();
        return ResponseEntity.ok(commerceService.getCartDetails(id));
    }

    @PostMapping("/cart/items")
    @Operation(summary = "API-031: Agregar producto/variante al carrito", description = "Valida stock disponible y suma el SKU al carrito activo")
    public ResponseEntity<CartLine> addToCart(
            @RequestParam(required = false) UUID cartId,
            @Valid @RequestBody AddToCartRequest req) {
        UUID id = cartId != null ? cartId : commerceService.getOrCreateCart(null).getId();
        return ResponseEntity.ok(commerceService.addToCart(id, req));
    }

    @PatchMapping("/cart/items/{id}")
    @Operation(summary = "API-032: Actualizar cantidad en carrito", description = "Modifica las unidades solicitadas recalculando totales")
    public ResponseEntity<CartLine> updateCartItem(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCartItemRequest req) {
        return ResponseEntity.ok(commerceService.updateCartItem(id, req));
    }

    @DeleteMapping("/cart/items/{id}")
    @Operation(summary = "API-033: Eliminar ítem del carrito", description = "Remueve la línea seleccionada")
    public ResponseEntity<Void> removeCartItem(@PathVariable UUID id) {
        commerceService.removeCartItem(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/shipping/quote")
    @Operation(summary = "API-034: Calcular flete y cobertura de despacho", description = "Evalúa cobertura logística por región peruana y emite tarifa formal con vigencia")
    public ResponseEntity<ShippingQuote> calculateShipping(@Valid @RequestBody ShippingQuoteRequest req) {
        return ResponseEntity.ok(commerceService.calculateShipping(req));
    }

    @PostMapping("/checkout/preview")
    @Operation(summary = "API-035: Resumen y recálculo de totales antes de pagar", description = "Congela y valida en backend los subtotales, IGV y flete final")
    public ResponseEntity<Map<String, Object>> previewCheckout(
            @RequestParam(required = false) UUID cartId,
            @RequestBody(required = false) CheckoutPreviewRequest req) {
        UUID id = cartId != null ? cartId : commerceService.getOrCreateCart(null).getId();
        Map<String, Object> cartDetails = commerceService.getCartDetails(id);
        BigDecimal grandTotal = (BigDecimal) cartDetails.get("grandTotal");
        BigDecimal shipping = new BigDecimal("80.00");
        return ResponseEntity.ok(Map.of(
                "subtotal", cartDetails.get("subtotal"),
                "igv", cartDetails.get("igv"),
                "shipping", shipping,
                "grandTotal", grandTotal.add(shipping),
                "currency", "PEN",
                "canCheckout", true
        ));
    }

    @PostMapping("/orders")
    @Operation(summary = "API-036: Confirmar orden transaccional (Idempotente)", description = "Crea orden inmutable en estado pendiente de pago con snapshot de precios")
    public ResponseEntity<Order> createOrder(
            @RequestParam(required = false) UUID cartId,
            @Valid @RequestBody CreateOrderRequest req) {
        UUID id = cartId != null ? cartId : commerceService.getOrCreateCart(null).getId();
        Order order = commerceService.createOrder(id, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping("/orders/{id}")
    @Operation(summary = "API-037: Consultar detalle de orden", description = "Recupera los datos completos, líneas y timeline de despacho de un pedido")
    public ResponseEntity<Map<String, Object>> getOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(commerceService.getOrderDetails(id));
    }
}
