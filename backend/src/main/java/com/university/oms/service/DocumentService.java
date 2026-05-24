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
import com.university.oms.security.AuthContext;
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
    private final WorkflowService workflowService;
    private final BusinessAccessService accessService;

    public DocumentService(InMemoryDatabase db, ApprovalService approvalService,
                           AiProviderAdapter aiProvider, List<DocumentReviewStrategy> strategies,
                           DataPersistence persistence, DocumentProcessor processor, WorkflowService workflowService,
                           BusinessAccessService accessService) {
        this.db = db;
        this.approvalService = approvalService;
        this.aiProvider = aiProvider;
        this.strategies = strategies;
        this.persistence = persistence;
        this.processor = processor;
        this.workflowService = workflowService;
        this.accessService = accessService;
    }

    public List<Document> list() {
        User user = AuthContext.currentUser();
        List<Document> documents = new ArrayList<Document>(db.documents().values());
        if (user == null || canViewAll(user)) {
            return documents;
        }
        List<Document> scoped = new ArrayList<Document>();
        for (Document document : documents) {
            if (document.getApplicantId().equals(user.getId()) || document.getDeptId().equals(user.getDeptId())
                    && user.getRoleKeys().contains("dept_head")) {
                scoped.add(document);
            }
        }
        return scoped;
    }

    public Document create(DocumentRequest request) {
        Long applicantId = AuthContext.currentUserIdOr(request.getApplicantId());
        User applicant = db.users().get(applicantId);
        if (applicant == null) {
            throw new BusinessException("用户不存在");
        }
        Document document = factory.create(db.nextId(), request, applicant);
        db.fill(document, document.getId());
        db.documents().put(document.getId(), document);
        persistence.saveDocument(document);
        approvalService.record("document", document.getId(), applicant.getId(), "create", "起草公文");
        workflowService.audit("document", "create", "document", document.getId(), document.getTitle());
        return document;
    }

    public Document submit(Long id) {
        Document document = find(id);
        accessService.requireDocumentSubmit(document);
        document.setStatus("pending_dept");
        document.setUpdatedAt(LocalDateTime.now());
        persistence.saveDocument(document);
        approvalService.record("document", id, document.getApplicantId(), "submit", "提交审批");
        workflowService.startFlow("document", id, document.getStatus(), document.getApplicantId());
        return document;
    }

    public Document archive(Long id) {
        Document document = find(id);
        accessService.requireDocumentArchive(document);
        if (!"approved".equals(document.getStatus())) {
            throw new BusinessException("只有已审批通过的公文可以归档");
        }
        document.setStatus("archived");
        document.setUpdatedAt(LocalDateTime.now());
        persistence.saveDocument(document);
        approvalService.record("document", id, AuthContext.currentUserIdOr(document.getApplicantId()), "archive", "公文归档");
        workflowService.advanceFlow("document", id, "approved", "archived", document.getApplicantId());
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

    private boolean canViewAll(User user) {
        return user.getRoleKeys().contains("admin")
                || user.getRoleKeys().contains("office_admin")
                || user.getRoleKeys().contains("school_leader");
    }
}
