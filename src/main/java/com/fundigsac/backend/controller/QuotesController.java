package com.fundigsac.backend.controller;

import com.fundigsac.backend.dto.ApiDtos.CreateQuoteRequest;
import com.fundigsac.backend.model.entity.QuoteRequest;
import com.fundigsac.backend.service.OperationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/quotes")
@RequiredArgsConstructor
@Tag(name = "04. Cotizaciones B2B", description = "Captura de oportunidades técnicas B2B con folio de seguimiento e idempotencia")
public class QuotesController {

    private final OperationService operationService;

    @PostMapping
    @Operation(summary = "API-009: Crear solicitud de cotización", description = "Genera una cotización formal B2B con items o consulta abierta, persistiendo folio único antes de confirmar")
    public ResponseEntity<QuoteRequest> createQuote(@Valid @RequestBody CreateQuoteRequest req) {
        QuoteRequest created = operationService.createQuote(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{reference}/receipt")
    @Operation(summary = "API-010: Constancia pública de cotización", description = "Consulta el estado público de una cotización mediante su código de folio sin exponer datos de terceros")
    public ResponseEntity<Map<String, Object>> getQuoteReceipt(@PathVariable String reference) {
        return ResponseEntity.ok(operationService.getQuoteReceipt(reference));
    }
}
