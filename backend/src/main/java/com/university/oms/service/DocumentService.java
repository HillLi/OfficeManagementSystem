package com.university.oms.service;

import com.university.oms.common.BusinessException;
import com.university.oms.design.*;
import com.university.oms.dto.AiDraftRequest;
import com.university.oms.dto.DocumentDistributionRequest;
import com.university.oms.dto.DocumentRequest;
import com.university.oms.model.AiReviewResult;
import com.university.oms.model.Document;
import com.university.oms.model.DocumentDistribution;
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
    private final DictionaryService dictionaryService;

    public DocumentService(InMemoryDatabase db, ApprovalService approvalService,
                           AiProviderAdapter aiProvider, List<DocumentReviewStrategy> strategies,
                           DataPersistence persistence, DocumentProcessor processor, WorkflowService workflowService,
                           BusinessAccessService accessService, DictionaryService dictionaryService) {
        this.db = db;
        this.approvalService = approvalService;
        this.aiProvider = aiProvider;
        this.strategies = strategies;
        this.persistence = persistence;
        this.processor = processor;
        this.workflowService = workflowService;
        this.accessService = accessService;
        this.dictionaryService = dictionaryService;
    }

    public List<Document> list() {
        User user = AuthContext.currentUser();
        List<Document> documents = new ArrayList<Document>(db.documents().values());
        if (user == null || canViewAll(user)) {
            return documents;
        }
        List<Document> scoped = new ArrayList<Document>();
        for (Document document : documents) {
            if (accessService.canReadBusiness("document", document.getId())) {
                scoped.add(document);
            }
        }
        return scoped;
    }

    public Document create(DocumentRequest request) {
        dictionaryService.requireEnabled("document_type", request.getDocType(), "公文文种");
        dictionaryService.requireEnabled("secrecy_level", request.getSecrecyLevel(), "密级");
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
        if ("rejected".equals(document.getStatus())) {
            document.setVersion(document.getVersion() == null ? 2 : document.getVersion() + 1);
        }
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
        workflowService.audit("document", "ai_review", "document", id,
                "密级：" + document.getSecrecyLevel() + "，结果：" + (result.isPassed() ? "通过" : "阻断或待修正"));
        return result;
    }

    public DocumentDistribution distribute(Long id, DocumentDistributionRequest request) {
        Document document = find(id);
        accessService.requireDocumentDistribute(document);
        if (!"approved".equals(document.getStatus()) && !"archived".equals(document.getStatus())) {
            throw new BusinessException("只有审批通过或已归档的公文可以分发");
        }
        User receiver = db.users().get(request.getReceiverId());
        if (receiver == null || !request.getReceiverDeptId().equals(receiver.getDeptId())) {
            throw new BusinessException("接收人或接收部门无效");
        }
        DocumentDistribution distribution = new DocumentDistribution();
        db.fill(distribution, db.nextId());
        distribution.setDocumentId(id);
        distribution.setReceiverId(request.getReceiverId());
        distribution.setReceiverDeptId(request.getReceiverDeptId());
        distribution.setStatus("distributed");
        distribution.setDistributedAt(LocalDateTime.now());
        db.documentDistributions().put(distribution.getId(), distribution);
        document.setDistributionStatus("distributed");
        document.setUpdatedAt(LocalDateTime.now());
        persistence.saveDocumentDistribution(distribution);
        persistence.saveDocument(document);
        workflowService.notifyUser(distribution.getReceiverId(), "公文待签收",
                document.getTitle() + " 已分发，请及时签收", "document", id);
        workflowService.audit("document", "distribute", "document", id, "分发至用户#" + distribution.getReceiverId());
        return distribution;
    }

    public List<DocumentDistribution> distributions(Long id) {
        find(id);
        accessService.requireBusinessRead("document", id);
        List<DocumentDistribution> rows = new ArrayList<DocumentDistribution>();
        for (DocumentDistribution distribution : db.documentDistributions().values()) {
            if (id.equals(distribution.getDocumentId())) {
                rows.add(distribution);
            }
        }
        rows.sort(Comparator.comparing(DocumentDistribution::getCreatedAt));
        return rows;
    }

    public DocumentDistribution receipt(Long id, Long distributionId) {
        Document document = find(id);
        DocumentDistribution distribution = findDistribution(id, distributionId);
        accessService.requireDocumentReceipt(distribution);
        distribution.setStatus("received");
        distribution.setReceivedAt(LocalDateTime.now());
        distribution.setUpdatedAt(LocalDateTime.now());
        document.setDistributionStatus(allReceived(id) ? "received" : "partially_received");
        document.setUpdatedAt(LocalDateTime.now());
        persistence.saveDocumentDistribution(distribution);
        persistence.saveDocument(document);
        workflowService.audit("document", "receipt", "document", id, "签收记录#" + distributionId);
        return distribution;
    }

    public DocumentDistribution remind(Long id, Long distributionId) {
        Document document = find(id);
        accessService.requireDocumentRemind(document);
        DocumentDistribution distribution = findDistribution(id, distributionId);
        if ("received".equals(distribution.getStatus())) {
            throw new BusinessException("已签收公文无需催办");
        }
        distribution.setRemindedAt(LocalDateTime.now());
        distribution.setUpdatedAt(LocalDateTime.now());
        persistence.saveDocumentDistribution(distribution);
        workflowService.notifyUser(distribution.getReceiverId(), "公文签收催办",
                document.getTitle() + " 尚未签收，请及时办理", "document", id);
        workflowService.audit("document", "remind_receipt", "document", id, "催办记录#" + distributionId);
        return distribution;
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

    private DocumentDistribution findDistribution(Long documentId, Long distributionId) {
        DocumentDistribution distribution = db.documentDistributions().get(distributionId);
        if (distribution == null || !documentId.equals(distribution.getDocumentId())) {
            throw new BusinessException("公文分发记录不存在");
        }
        return distribution;
    }

    private boolean allReceived(Long documentId) {
        boolean found = false;
        for (DocumentDistribution distribution : db.documentDistributions().values()) {
            if (documentId.equals(distribution.getDocumentId())) {
                found = true;
                if (!"received".equals(distribution.getStatus())) {
                    return false;
                }
            }
        }
        return found;
    }

    private boolean canViewAll(User user) {
        return user.getRoleKeys().contains("admin")
                || user.getRoleKeys().contains("office_admin")
                || user.getRoleKeys().contains("school_leader");
    }
}
