package com.university.oms.service;

import com.university.oms.common.BusinessException;
import com.university.oms.design.*;
import com.university.oms.dto.AiDraftRequest;
import com.university.oms.dto.DocumentRequest;
import com.university.oms.model.AiReviewResult;
import com.university.oms.model.Document;
import com.university.oms.model.User;
import com.university.oms.repository.DataPersistence;
import com.university.oms.repository.InMemoryDatabase;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class DocumentService {
    private final InMemoryDatabase db;
    private final ApprovalService approvalService;
    private final AiProviderAdapter aiProvider;
    private final List<DocumentReviewStrategy> strategies;
    private final DataPersistence persistence;
    private final DocumentFactory factory = new DocumentFactory();
    private final DocumentProcessor processor;

    public DocumentService(InMemoryDatabase db, ApprovalService approvalService,
                           AiProviderAdapter aiProvider, List<DocumentReviewStrategy> strategies,
                           DataPersistence persistence, DocumentProcessor processor) {
        this.db = db;
        this.approvalService = approvalService;
        this.aiProvider = aiProvider;
        this.strategies = strategies;
        this.persistence = persistence;
        this.processor = processor;
    }

    public List<Document> list() {
        return new ArrayList<Document>(db.documents().values());
    }

    public Document create(DocumentRequest request) {
        User applicant = db.users().get(request.getApplicantId());
        if (applicant == null) {
            throw new BusinessException("用户不存在");
        }
        Document document = factory.create(db.nextId(), request, applicant);
        db.fill(document, document.getId());
        db.documents().put(document.getId(), document);
        persistence.saveDocument(document);
        approvalService.record("document", document.getId(), applicant.getId(), "create", "起草公文");
        return document;
    }

    public Document submit(Long id) {
        Document document = find(id);
        document.setStatus("pending_dept");
        document.setUpdatedAt(LocalDateTime.now());
        persistence.saveDocument(document);
        approvalService.record("document", id, document.getApplicantId(), "submit", "提交审批");
        return document;
    }

    public AiReviewResult review(Long id) {
        Document document = find(id);
        AiReviewResult result = processor.process(document);
        document.setAiReviewResult(result);
        document.setUpdatedAt(LocalDateTime.now());
        persistence.saveDocument(document);
        return result;
    }

    public String draft(AiDraftRequest request) {
        return aiProvider.draft(request.getDocType(), request.getTopic(), request.getKeyPoints());
    }

    private Document find(Long id) {
        Document document = db.documents().get(id);
        if (document == null) {
            throw new BusinessException("公文不存在");
        }
        return document;
    }
}
