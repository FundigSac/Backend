package com.fundigsac.backend.controller;

import com.fundigsac.backend.dto.ApiDtos.PrivacyChoiceRequest;
import com.fundigsac.backend.model.entity.ConsentEvidence;
import com.fundigsac.backend.model.entity.LegalNotice;
import com.fundigsac.backend.model.enums.LegalNoticeType;
import com.fundigsac.backend.service.OperationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "06. Cumplimiento Legal y Consentimiento", description = "Textos jurídicos homologados y registro de consentimientos para cookies y datos personales")
public class LegalController {

    private final OperationService operationService;

    @GetMapping("/legal/{type}")
    @Operation(summary = "API-017: Obtener texto legal vigente", description = "Devuelve el contenido legal aprobado según tipo: privacy, cookies, terms, claims")
    public ResponseEntity<LegalNotice> getLegalNotice(@PathVariable LegalNoticeType type) {
        return ResponseEntity.ok(operationService.getLegalNotice(type));
    }

    @PostMapping("/privacy/choices")
    @Operation(summary = "API-018: Registrar preferencia de privacidad / cookies", description = "Almacena la prueba de consentimiento o rechazo de cookies no esenciales de forma auditable")
    public ResponseEntity<ConsentEvidence> recordPrivacyChoice(@Valid @RequestBody PrivacyChoiceRequest req) {
        ConsentEvidence evidence = operationService.recordPrivacyChoice(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(evidence);
    }
}
