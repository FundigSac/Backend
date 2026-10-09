package com.fundigsac.backend.service;

import com.fundigsac.backend.exception.GlobalExceptionHandler.ResourceNotFoundException;
import com.fundigsac.backend.model.entity.*;
import com.fundigsac.backend.model.enums.*;
import com.fundigsac.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CatalogService {

    private final ProductCategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final TechnicalAttributeRepository attributeRepository;
    private final TechnicalDocumentRepository documentRepository;
    private final ProductDocumentRepository productDocumentRepository;
    private final ProductMediaRepository productMediaRepository;
    private final SitePageRepository pageRepository;

    public List<ProductCategory> getCategories() {
        return categoryRepository.findByPublishedTrueOrderBySortOrderAsc();
    }

    public Page<Product> getProducts(String categorySlug, String query, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, Math.min(size, 100));
        if (categorySlug != null && !categorySlug.isBlank()) {
            Optional<ProductCategory> category = categoryRepository.findBySlug(categorySlug);
            if (category.isPresent()) {
                return productRepository.findByCategoryIdAndVisibility(category.get().getId(), ProductVisibility.published, pageRequest);
            }
        }
        return productRepository.findByVisibility(ProductVisibility.published, pageRequest);
    }

    public Map<String, Object> getProductDetails(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado o no publicado: " + slug));

        if (product.getVisibility() != ProductVisibility.published) {
            throw new ResourceNotFoundException("El producto no está en estado publicado.");
        }

        List<ProductVariant> variants = variantRepository.findByProductIdAndStatus(product.getId(), VariantStatus.active);
        List<ProductMedia> mediaList = productMediaRepository.findByProductIdOrderBySortOrderAsc(product.getId());
        List<ProductDocument> docsList = productDocumentRepository.findByProductIdOrderBySortOrderAsc(product.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("product", product);
        response.put("variants", variants);
        response.put("media", mediaList);
        response.put("documents", docsList);
        return response;
    }

    public Map<String, Object> getAvailableFilters(String category) {
        List<TechnicalAttribute> filterableAttrs = attributeRepository.findByFilterableTrue();
        return Map.of(
                "attributes", filterableAttrs,
                "materials", List.of("Hierro Dúctil GGG50", "HDPE PE100", "Acero Inoxidable 316", "EPDM"),
                "standards", List.of("ISO 2531", "EN 545", "AWWA C509", "DIN 3352")
        );
    }

    public List<TechnicalDocument> getResources(String query) {
        return documentRepository.findByStatus(DocumentStatus.approved);
    }

    public TechnicalDocument getResourceDownload(UUID id) {
        return documentRepository.findById(id)
                .filter(doc -> doc.getStatus() == DocumentStatus.approved)
                .orElseThrow(() -> new ResourceNotFoundException("Documento no encontrado o no vigente: " + id));
    }

    public SitePage getPage(String slug) {
        return pageRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Página no encontrada: " + slug));
    }
}
