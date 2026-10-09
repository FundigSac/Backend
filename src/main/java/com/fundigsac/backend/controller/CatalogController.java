package com.fundigsac.backend.controller;

import com.fundigsac.backend.model.entity.Product;
import com.fundigsac.backend.model.entity.ProductCategory;
import com.fundigsac.backend.service.CatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/catalog")
@RequiredArgsConstructor
@Tag(name = "01. Catálogo Técnico B2B", description = "Endpoints públicos para exploración de familias, productos y filtros técnicos de hierro dúctil")
public class CatalogController {

    private final CatalogService catalogService;

    @GetMapping("/categories")
    @Operation(summary = "API-001: Listar familias publicadas", description = "Obtiene la taxonomía jerárquica de categorías activas (Válvulas, Tuberías, Marcos y tapas)")
    public ResponseEntity<List<ProductCategory>> getCategories() {
        return ResponseEntity.ok(catalogService.getCategories());
    }

    @GetMapping("/products")
    @Operation(summary = "API-002: Listar productos por categoría o filtros", description = "Listado paginado de productos publicados con filtros técnicos")
    public ResponseEntity<Page<Product>> getProducts(
            @Parameter(description = "Slug de la categoría (ej: valvulas)") @RequestParam(required = false) String category,
            @Parameter(description = "Búsqueda por texto libre") @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return ResponseEntity.ok(catalogService.getProducts(category, query, page, size));
    }

    @GetMapping("/products/{slug}")
    @Operation(summary = "API-003: Ficha técnica de producto publicada", description = "Detalle exhaustivo de ficha de producto con variantes, planos y fichas PDF")
    public ResponseEntity<Map<String, Object>> getProductDetails(@PathVariable String slug) {
        return ResponseEntity.ok(catalogService.getProductDetails(slug));
    }

    @GetMapping("/filters")
    @Operation(summary = "API-004: Facetas técnicas disponibles", description = "Devuelve los atributos técnicos filtrables reales para la familia seleccionada (DN, PN, Material)")
    public ResponseEntity<Map<String, Object>> getAvailableFilters(
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok(catalogService.getAvailableFilters(category));
    }
}
