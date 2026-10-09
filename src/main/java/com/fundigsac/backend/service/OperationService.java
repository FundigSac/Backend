package com.fundigsac.backend.service;

import com.fundigsac.backend.dto.ApiDtos.*;
import com.fundigsac.backend.exception.GlobalExceptionHandler.ConflictException;
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
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OperationService {

    private final InquiryRepository inquiryRepository;
    private final QuoteRequestRepository quoteRepository;
    private final QuoteItemRepository quoteItemRepository;
    private final QuoteStatusEventRepository quoteStatusEventRepository;
    private final ComplaintRepository complaintRepository;
    private final ComplaintEventRepository complaintEventRepository;
    private final LegalNoticeRepository legalNoticeRepository;
    private final ConsentEvidenceRepository consentEvidenceRepository;
    private final NotificationOutboxRepository outboxRepository;

    // --- Atencion / Inquiries ---
    @Transactional
    public Inquiry createInquiry(CreateInquiryRequest req) {
        String ref = "INQ-" + System.currentTimeMillis() % 10000000;
        Inquiry inquiry = Inquiry.builder()
                .reference(ref)
                .contactName(req.getName())
                .contactEmail(req.getEmail())
                .contactPhone(req.getPhone())
                .subject(req.getSubject())
                .message(req.getMessage())
                .status(InquiryStatus.new_inquiry)
                .build();
        inquiry = inquiryRepository.save(inquiry);

        // Notificación en outbox
        queueNotification("INQUIRY_CREATED_" + ref, "INQUIRY_RECEIVED", req.getEmail(), ref);
        return inquiry;
    }

    // --- Cotizaciones / Quotes ---
    @Transactional
    public QuoteRequest createQuote(CreateQuoteRequest req) {
        String ref = "COT-" + (System.currentTimeMillis() % 10000000);
        QuoteRequest quote = QuoteRequest.builder()
                .reference(ref)
                .contactName(req.getContactName())
                .contactEmail(req.getContactEmail())
                .companyName(req.getCompanyName())
                .contactPhone(req.getContactPhone())
                .message(req.getMessage())
                .status(QuoteStatus.received)
                .source(req.getSource() != null ? req.getSource() : QuoteSource.general)
                .build();
        quote = quoteRepository.save(quote);

        if (req.getItems() != null && !req.getItems().isEmpty()) {
            for (QuoteItemDto itemDto : req.getItems()) {
                QuoteItem item = QuoteItem.builder()
                        .quoteId(quote.getId())
                        .productId(itemDto.getProductId())
                        .variantId(itemDto.getVariantId())
                        .productNameSnapshot(itemDto.getProductName())
                        .referenceSnapshot(itemDto.getReferenceCode())
                        .quantity(itemDto.getQuantity())
                        .uomSnapshot(itemDto.getUom() != null ? itemDto.getUom() : "UND")
                        .notes(itemDto.getNotes())
                        .build();
                quoteItemRepository.save(item);
            }
        }

        quoteStatusEventRepository.save(QuoteStatusEvent.builder()
                .quoteId(quote.getId())
                .fromStatus(null)
                .toStatus(QuoteStatus.received.name())
                .actorRef("CLIENTE_PUBLICO")
                .note("Solicitud de cotización generada")
                .build());

        queueNotification("QUOTE_CREATED_" + ref, "QUOTE_CONFIRMATION", req.getContactEmail(), ref);
        return quote;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getQuoteReceipt(String reference) {
        QuoteRequest quote = quoteRepository.findByReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException("Cotización no encontrada: " + reference));
        List<QuoteItem> items = quoteItemRepository.findByQuoteId(quote.getId());

        return Map.of(
                "reference", quote.getReference(),
                "contactName", quote.getContactName(),
                "status", quote.getStatus(),
                "createdAt", quote.getCreatedAt(),
                "items", items
        );
    }

    @Transactional(readOnly = true)
    public Page<QuoteRequest> getStaffQuotes(QuoteStatus status, int page, int size) {
        PageRequest pr = PageRequest.of(page, Math.min(size, 100));
        return status != null ? quoteRepository.findByStatus(status, pr) : quoteRepository.findAll(pr);
    }

    @Transactional
    public QuoteRequest updateQuoteStatus(UUID id, UpdateQuoteStatusRequest req, String staffUser) {
        QuoteRequest quote = quoteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cotización no encontrada"));
        QuoteStatus oldStatus = quote.getStatus();
        quote.setStatus(req.getStatus());
        if (req.getAssignee() != null) {
            quote.setAssignedStaffId(req.getAssignee());
        }
        quote = quoteRepository.save(quote);

        quoteStatusEventRepository.save(QuoteStatusEvent.builder()
                .quoteId(quote.getId())
                .fromStatus(oldStatus.name())
                .toStatus(req.getStatus().name())
                .actorRef(staffUser != null ? staffUser : "STAFF")
                .note(req.getNote())
                .build());
        return quote;
    }

    // --- Libro de Reclamaciones ---
    @Transactional
    public Complaint createComplaint(CreateComplaintRequest req) {
        String ref = "REC-" + (System.currentTimeMillis() % 10000000);
        // Según normativa peruana: plazo de respuesta legal estándar (15 días hábiles aprox ~ 20 días calendario)
        Instant due = Instant.now().plus(20, ChronoUnit.DAYS);

        Complaint complaint = Complaint.builder()
                .reference(ref)
                .complaintType(req.getType())
                .consumerName(req.getConsumerName())
                .consumerContact(req.getConsumerContact())
                .consumerDocType(req.getConsumerDocType())
                .consumerDocNumber(req.getConsumerDocNumber())
                .productService(req.getProductService())
                .incidentDescription(req.getIncidentDescription())
                .requestDescription(req.getRequestDescription())
                .dueAt(due)
                .status(ComplaintStatus.received)
                .privacyNoticeVersion(req.getPrivacyNoticeVersion() != null ? req.getPrivacyNoticeVersion() : "v1.0-PE")
                .build();
        complaint = complaintRepository.save(complaint);

        complaintEventRepository.save(ComplaintEvent.builder()
                .complaintId(complaint.getId())
                .eventType(ComplaintEventType.created)
                .actorRef("CONSUMIDOR")
                .detail("Reclamo/queja registrado conforme a Ley")
                .build());

        queueNotification("COMPLAINT_CREATED_" + ref, "COMPLAINT_RECEIPT", req.getConsumerContact(), ref);
        return complaint;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getComplaintReceipt(String reference) {
        Complaint complaint = complaintRepository.findByReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException("Constancia de reclamo no encontrada: " + reference));

        return Map.of(
                "reference", complaint.getReference(),
                "type", complaint.getComplaintType(),
                "consumerName", complaint.getConsumerName(),
                "submittedAt", complaint.getSubmittedAt(),
                "dueAt", complaint.getDueAt(),
                "status", complaint.getStatus()
        );
    }

    @Transactional(readOnly = true)
    public Page<Complaint> getStaffComplaints(ComplaintStatus status, int page, int size) {
        PageRequest pr = PageRequest.of(page, Math.min(size, 100));
        return status != null ? complaintRepository.findByStatus(status, pr) : complaintRepository.findAll(pr);
    }

    @Transactional
    public Complaint updateComplaintStatus(UUID id, UpdateComplaintStatusRequest req, String staffUser) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reclamo no encontrado"));
        complaint.setStatus(req.getStatus());
        complaint = complaintRepository.save(complaint);

        complaintEventRepository.save(ComplaintEvent.builder()
                .complaintId(complaint.getId())
                .eventType(ComplaintEventType.assigned)
                .actorRef(staffUser != null ? staffUser : "STAFF_LEGAL")
                .detail(req.getNotes())
                .build());
        return complaint;
    }

    @Transactional
    public ComplaintEvent respondComplaint(UUID id, ComplaintResponseRequest req, String staffUser) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reclamo no encontrado"));
        complaint.setStatus(ComplaintStatus.answered);
        complaintRepository.save(complaint);

        ComplaintEvent event = ComplaintEvent.builder()
                .complaintId(complaint.getId())
                .eventType(ComplaintEventType.responded)
                .actorRef(staffUser != null ? staffUser : "STAFF_LEGAL")
                .detail(req.getResponse())
                .responseDocumentKey(req.getAttachmentKey())
                .build();
        event = complaintEventRepository.save(event);

        queueNotification("COMPLAINT_ANSWERED_" + complaint.getReference(), "COMPLAINT_RESOLUTION", complaint.getConsumerContact(), complaint.getReference());
        return event;
    }

    // --- Legal & Privacidad ---
    @Transactional(readOnly = true)
    public LegalNotice getLegalNotice(LegalNoticeType type) {
        return legalNoticeRepository.findFirstByNoticeTypeOrderByPublishedAtDesc(type)
                .orElseThrow(() -> new ResourceNotFoundException("Aviso legal no publicado para tipo: " + type));
    }

    @Transactional
    public ConsentEvidence recordPrivacyChoice(PrivacyChoiceRequest req) {
        ConsentEvidence evidence = ConsentEvidence.builder()
                .noticeVersion(req.getNoticeVersion())
                .purpose(req.getPurpose())
                .choice(req.getChoice())
                .subjectRef(req.getSubjectRef() != null ? req.getSubjectRef() : "ANONYMOUS")
                .evidenceMethod(req.getEvidenceMethod() != null ? req.getEvidenceMethod() : "WEB_BANNER")
                .build();
        return consentEvidenceRepository.save(evidence);
    }

    private void queueNotification(String key, String template, String recipient, String payloadRef) {
        if (outboxRepository.findByEventKey(key).isEmpty()) {
            outboxRepository.save(NotificationOutbox.builder()
                    .eventKey(key)
                    .templateKey(template)
                    .recipient(recipient)
                    .payloadRef(payloadRef)
                    .status(OutboxStatus.pending)
                    .build());
        }
    }
}
