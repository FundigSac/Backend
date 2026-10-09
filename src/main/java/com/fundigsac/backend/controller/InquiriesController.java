package com.fundigsac.backend.controller;

import com.fundigsac.backend.dto.ApiDtos.CreateInquiryRequest;
import com.fundigsac.backend.model.entity.Inquiry;
import com.fundigsac.backend.service.OperationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inquiries")
@RequiredArgsConstructor
@Tag(name = "03. Atención y Consultas Generales", description = "Contacto comercial público sin necesidad de registro")
public class InquiriesController {

    private final OperationService operationService;

    @PostMapping
    @Operation(summary = "API-008: Registrar consulta general pública", description = "Envía una solicitud de contacto con datos mínimos y emite folio de seguimiento sin crear reclamo legal")
    public ResponseEntity<Inquiry> createInquiry(@Valid @RequestBody CreateInquiryRequest req) {
        Inquiry created = operationService.createInquiry(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
