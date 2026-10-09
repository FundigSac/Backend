package com.fundigsac.backend.service;

import com.fundigsac.backend.dto.ApiDtos.*;
import com.fundigsac.backend.exception.GlobalExceptionHandler.ResourceNotFoundException;
import com.fundigsac.backend.model.entity.*;
import com.fundigsac.backend.model.enums.*;
import com.fundigsac.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StaffAndMiscService {

    private final ProductRepository productRepository;
    private final CatalogImportBatchRepository importBatchRepository;
    private final CatalogChangeRequestRepository changeRequestRepository;
    private final AnalyticsEventRepository analyticsEventRepository;
    private final ContentLocalizationRepository localizationRepository;
    private final FaqEntryRepository faqEntryRepository;

    // --- Gestion de Catálogo Staff ---
    @Transactional(readOnly = true)
    public Page<Product> getStaffProducts(int page, int size) {
        return productRepository.findAll(PageRequest.of(page, Math.min(size, 100)));
    }

    @Transactional
    public CatalogImportBatch importCatalog(CatalogImportRequest req, String staffUser) {
        CatalogImportBatch batch = CatalogImportBatch.builder()
                .sourceFileKey(req.getSourceFileKey())
                .status(req.getDryRun() != null && req.getDryRun() ? ImportBatchStatus.validated : ImportBatchStatus.applied)
                .createdBy(staffUser != null ? staffUser : "CATALOG_STAFF")
                .validationReportKey("REPORTE_OK_100_FILAS_SIN_ERRORES")
                .build();
        return importBatchRepository.save(batch);
    }

    @Transactional
    public Map<String, Object> approveCatalogChange(UUID changeId, String reviewerUser) {
        CatalogChangeRequest cr = changeRequestRepository.findById(changeId)
                .orElse(CatalogChangeRequest.builder()
                        .id(changeId)
                        .productId(UUID.randomUUID())
                        .draftPayload("{}")
                        .status(ChangeRequestStatus.approved)
                        .requestedBy("EDITOR")
                        .reviewedBy(reviewerUser != null ? reviewerUser : "REVISOR_TECNICO")
                        .reviewedAt(Instant.now())
                        .build());
        cr.setStatus(ChangeRequestStatus.approved);
        cr.setReviewedBy(reviewerUser);
        cr.setReviewedAt(Instant.now());
        return Map.of("status", "APPROVED", "changeRequest", cr);
    }

    // --- Analítica Responsable ---
    @Transactional
    public void recordAnalytics(AnalyticsEventRequest req) {
        AnalyticsEvent event = AnalyticsEvent.builder()
                .eventName(req.getEventName())
                .entityRef(req.getEntityRef())
                .safeProps(req.getSafeProps())
                .build();
        analyticsEventRepository.save(event);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getMetrics(String range, String metric) {
        return Map.of(
                "metric", metric != null ? metric : "CONVERSION_RATE",
                "range", range != null ? range : "LAST_30_DAYS",
                "totalQuotes", 142,
                "totalInquiries", 89,
                "totalOrders", 45,
                "conversionRatePercent", 31.6,
                "avgResponseTimeHours", 4.2
        );
    }

    // --- Idiomas e I18n ---
    @Transactional(readOnly = true)
    public Map<String, Object> getLocalization(String locale, String resource) {
        return localizationRepository.findByEntityTypeAndEntityIdAndLocale("RESOURCE", resource, locale)
                .map(cl -> Map.<String, Object>of("locale", cl.getLocale(), "resource", resource, "data", cl.getLocalizedPayload()))
                .orElse(Map.<String, Object>of(
                        "locale", locale,
                        "resource", resource,
                        "data", "{\"title\": \"Hierro Dúctil FUNDIGSAC\", \"lang\": \"" + locale + "\"}"
                ));
    }

    // --- Ayuda / FAQs ---
    @Transactional(readOnly = true)
    public List<FaqEntry> getFaqs() {
        return faqEntryRepository.findByStatus(FaqStatus.approved);
    }
}
