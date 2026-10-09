package com.fundigsac.backend.controller;

import com.fundigsac.backend.dto.ApiDtos.CreateComplaintRequest;
import com.fundigsac.backend.model.entity.Complaint;
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
@RequestMapping("/api/v1/complaints")
@RequiredArgsConstructor
@Tag(name = "05. Libro de Reclamaciones (Normativa Perú)", description = "Registro obligatorio conforme a Indecopi y Ley N° 29733 con emisión de correlativo y constancia")
public class ComplaintsController {

    private final OperationService operationService;

    @PostMapping
    @Operation(summary = "API-013: Registrar reclamo o queja formal", description = "Ingresa un reclamo/queja en el Libro de Reclamaciones sin exigir login, calculando SLA de respuesta")
    public ResponseEntity<Complaint> createComplaint(@Valid @RequestBody CreateComplaintRequest req) {
        Complaint created = operationService.createComplaint(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{reference}/receipt")
    @Operation(summary = "API-014: Consultar constancia de reclamo", description = "Recupera la hoja de reclamación y estado del proceso mediante el código de folio generado")
    public ResponseEntity<Map<String, Object>> getComplaintReceipt(@PathVariable String reference) {
        return ResponseEntity.ok(operationService.getComplaintReceipt(reference));
    }
}
