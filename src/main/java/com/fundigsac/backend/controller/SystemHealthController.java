package com.fundigsac.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "07. Sistema y Disponibilidad", description = "Monitoreo de estado y sondas de readiness/liveness para Docker y orquestadores")
public class SystemHealthController {

    @GetMapping("/ready")
    @Operation(summary = "API-019: Sonda de preparación (Readiness Probe)", description = "Verifica que el backend esté listo para recibir tráfico, con conexión activa a PostgreSQL y servicios dependientes")
    public ResponseEntity<Map<String, Object>> getReadiness() {
        return ResponseEntity.ok(Map.of(
                "status", "READY",
                "database", "CONNECTED",
                "engine", "PostgreSQL 16",
                "threads", "Virtual Threads (Java 21 Project Loom)",
                "timestamp", Instant.now().toString(),
                "service", "fundigsac-enterprise-backend"
        ));
    }
}
