package com.fundigsac.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> checkHealth() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "fundigsac-backend",
                "javaVersion", System.getProperty("java.version"),
                "jvmVendor", System.getProperty("java.vendor"),
                "isVirtualThread", Thread.currentThread().isVirtual(),
                "timestamp", Instant.now().toString(),
                "message", "Backend FUNDIGSAC ejecutándose con Java 21 LTS y Spring Boot a máximo rendimiento"
        ));
    }
}
