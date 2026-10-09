package com.fundigsac.backend.controller;

import com.fundigsac.backend.dto.ApiDtos.InitializePaymentRequest;
import com.fundigsac.backend.dto.ApiDtos.IzipayWebhookRequest;
import com.fundigsac.backend.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "11. Pasarela de Pagos Izipay", description = "Inicialización de token seguro, consulta y conciliación mediante webhook firmado")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/initialize")
    @Operation(summary = "API-040: Inicializar intento de pago con Izipay", description = "Genera el intento y token de formulario para procesar en sandbox/producción de Izipay sin almacenar datos de tarjeta")
    public ResponseEntity<Map<String, Object>> initializePayment(@Valid @RequestBody InitializePaymentRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.initializePayment(req));
    }

    @PostMapping("/izipay/webhook")
    @Operation(summary = "API-041: Webhook firmado de notificación de Izipay", description = "Recepción server-to-server con verificación criptográfica de firma e idempotencia para marcar la orden como pagada")
    public ResponseEntity<Map<String, Object>> handleWebhook(@RequestBody IzipayWebhookRequest req) {
        return ResponseEntity.ok(paymentService.handleIzipayWebhook(req));
    }

    @GetMapping("/{id}/status")
    @Operation(summary = "API-042: Consultar estado oficial del pago", description = "Verifica el estado del pago desde la fuente fiable del servidor, no de la URL del navegador")
    public ResponseEntity<Map<String, Object>> getPaymentStatus(@PathVariable UUID id) {
        return ResponseEntity.ok(paymentService.getPaymentStatus(id));
    }
}
