package com.fundigsac.backend.controller;

import com.fundigsac.backend.dto.ApiDtos.AnalyticsEventRequest;
import com.fundigsac.backend.model.entity.FaqEntry;
import com.fundigsac.backend.service.StaffAndMiscService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "13. Analítica, Internacionalización y Asistencia", description = "Métricas éticas sin PII, diccionario ES/EN y base de preguntas frecuentes técnicas")
public class MiscController {

    private final StaffAndMiscService staffAndMiscService;

    @PostMapping("/analytics/events")
    @Operation(summary = "API-051: Registrar evento de navegación / conversión", description = "Métricas agregadas sin datos personales respetando el consentimiento del visitante")
    public ResponseEntity<Void> recordAnalytics(@Valid @RequestBody AnalyticsEventRequest req) {
        staffAndMiscService.recordAnalytics(req);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/i18n/{locale}/{resource}")
    @Operation(summary = "API-053: Obtener traducciones aprobadas (ES/EN)", description = "Devuelve contenido técnico homologado en español o inglés con fallback seguro")
    public ResponseEntity<Map<String, Object>> getLocalization(
            @PathVariable String locale,
            @PathVariable String resource) {
        return ResponseEntity.ok(staffAndMiscService.getLocalization(locale, resource));
    }

    @GetMapping("/faq")
    @Operation(summary = "API-054: Base de conocimiento y preguntas frecuentes", description = "Lista respuestas verificadas sobre normas, materiales, envíos y compras industriales")
    public ResponseEntity<List<FaqEntry>> getFaqs() {
        return ResponseEntity.ok(staffAndMiscService.getFaqs());
    }
}
