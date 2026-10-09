package com.fundigsac.backend.controller;

import com.fundigsac.backend.model.entity.SitePage;
import com.fundigsac.backend.model.entity.TechnicalDocument;
import com.fundigsac.backend.service.CatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "02. Documentos y Contenido Institucional", description = "Descargas de PDFs homologados y páginas de información corporativa")
public class ResourcesAndPagesController {

    private final CatalogService catalogService;

    @GetMapping("/resources")
    @Operation(summary = "API-005: Listado de recursos técnicos aprobados", description = "Catálogos maestros, manuales de instalación y fichas técnicas descargables")
    public ResponseEntity<List<TechnicalDocument>> getResources(@RequestParam(required = false) String query) {
        return ResponseEntity.ok(catalogService.getResources(query));
    }

    @GetMapping("/resources/{id}/download")
    @Operation(summary = "API-006: Descarga segura de documento técnico vigente", description = "Obtiene los metadatos y URL firmada de descarga del PDF oficial verificado")
    public ResponseEntity<Map<String, Object>> downloadResource(@PathVariable UUID id) {
        TechnicalDocument doc = catalogService.getResourceDownload(id);
        return ResponseEntity.ok(Map.of(
                "id", doc.getId(),
                "title", doc.getTitle(),
                "version", doc.getVersionLabel(),
                "downloadUrl", "/assets/docs/" + doc.getStorageKey(),
                "checksumSha256", doc.getChecksumSha256() != null ? doc.getChecksumSha256() : "VERIFIED_ORIGINAL"
        ));
    }

    @GetMapping("/pages/{slug}")
    @Operation(summary = "API-007: Contenido de página pública aprobada", description = "Obtiene los bloques de contenido verificados de páginas como nosotros, soluciones o infraestructura")
    public ResponseEntity<SitePage> getPage(@PathVariable String slug) {
        return ResponseEntity.ok(catalogService.getPage(slug));
    }
}
