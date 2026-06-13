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
import com.university.oms.repository.OmsRepository;
import com.university.oms.security.AuthContext;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 公文管理服务，处理公文的起草、提交、审批、AI校验、分发和签收
 */
@Service
public class DocumentService {
    private final OmsRepository repo;
    private final ApprovalService approvalService;
    private final AiProviderAdapter aiProvider;
    private final List<DocumentReviewStrategy> strategies;
    private final DocumentFactory factory = new DocumentFactory();
    private final DocumentProcessor processor;
    private final WorkflowService workflowService;
    private final BusinessAccessService accessService;
    private final DictionaryService dictionaryService;

    public DocumentService(OmsRepository repo, ApprovalService approvalService,
                           AiProviderAdapter aiProvider, List<DocumentReviewStrategy> strategies,
                           DocumentProcessor processor, WorkflowService workflowService,
                           BusinessAccessService accessService, DictionaryService dictionaryService) {
        this.repo = repo;
        this.approvalService = approvalService;
        this.aiProvider = aiProvider;
        this.strategies = strategies;
        this.processor = processor;
        this.workflowService = workflowService;
        this.accessService = accessService;
        this.dictionaryService = dictionaryService;
    }

    /** 获取当前用户可见的公文列表（根据角色和权限过滤） */
    public List<Document> list() {
        User user = AuthContext.currentUser();
        List<Document> documents = repo.findAllDocuments();
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

    /** 创建新公文（草稿状态） */
    public Document create(DocumentRequest request) {
        dictionaryService.requireEnabled("document_type", request.getDocType(), "公文文种");
        dictionaryService.requireEnabled("secrecy_level", request.getSecrecyLevel(), "密级");
        Long applicantId = AuthContext.currentUserIdOr(request.getApplicantId());
        User applicant = repo.findUserById(applicantId);
        if (applicant == null) {
            throw new BusinessException("用户不存在");
        }
        Document document = factory.create(repo.nextId(), request, applicant);
        OmsRepository.fillEntity(document, document.getId());
        repo.saveDocument(document);
        approvalService.record("document", document.getId(), applicant.getId(), "create", "起草公文");
        workflowService.audit("document", "create", "document", document.getId(), document.getTitle());
        return document;
    }

    /** 修改公文（仅草稿或已驳回状态可修改） */
    public Document update(Long id, DocumentRequest request) {
        Document document = find(id);
        if (!"draft".equals(document.getStatus()) && !"rejected".equals(document.getStatus())) {
            throw new BusinessException("只有草稿或已驳回的公文可以修改");
        }
        dictionaryService.requireEnabled("document_type", request.getDocType(), "公文文种");
        dictionaryService.requireEnabled("secrecy_level", request.getSecrecyLevel(), "密级");
        document.setTitle(request.getTitle());
        document.setDocType(request.getDocType());
        document.setUrgency(request.getUrgency());
        document.setSecrecyLevel(request.getSecrecyLevel());
        document.setKnowledgeScope(request.getKnowledgeScope());
        document.setContent(request.getContent());
        document.setUpdatedAt(LocalDateTime.now());
        repo.saveDocument(document);
        approvalService.record("document", id, AuthContext.currentUserIdOr(document.getApplicantId()), "update", "修改公文");
        workflowService.audit("document", "update", "document", id, document.getTitle());
        return document;
    }

    /** 提交公文进入审批流，驳回后重新提交时自动递增版本号 */
    public Document submit(Long id) {
        Document document = find(id);
        accessService.requireDocumentSubmit(document);
        if ("rejected".equals(document.getStatus())) {
            document.setVersion(document.getVersion() == null ? 2 : document.getVersion() + 1);
        }
        document.setStatus("pending_dept");
        document.setUpdatedAt(LocalDateTime.now());
        repo.saveDocument(document);
        approvalService.record("document", id, document.getApplicantId(), "submit", "提交审批");
        workflowService.startFlow("document", id, document.getStatus(), document.getApplicantId());
        return document;
    }

    /** 归档公文（仅已审批通过的公文可归档） */
    public Document archive(Long id) {
        Document document = find(id);
        accessService.requireDocumentArchive(document);
        if (!"approved".equals(document.getStatus())) {
            throw new BusinessException("只有已审批通过的公文可以归档");
        }
        document.setStatus("archived");
        document.setUpdatedAt(LocalDateTime.now());
        repo.saveDocument(document);
        approvalService.record("document", id, AuthContext.currentUserIdOr(document.getApplicantId()), "archive", "公文归档");
        workflowService.advanceFlow("document", id, "approved", "archived", document.getApplicantId());
        return document;
    }

    /** AI格式校验公文，返回校验结果 */
    public AiReviewResult review(Long id) {
        Document document = find(id);
        AiReviewResult result = processor.process(document);
        document.setAiReviewResult(result);
        document.setUpdatedAt(LocalDateTime.now());
        repo.saveDocument(document);
        workflowService.audit("document", "ai_review", "document", id,
                "密级：" + document.getSecrecyLevel() + "，结果：" + (result.isPassed() ? "通过" : "阻断或待修正"));
        return result;
    }

    /** 分发公文给指定接收人 */
    public DocumentDistribution distribute(Long id, DocumentDistributionRequest request) {
        Document document = find(id);
        accessService.requireDocumentDistribute(document);
        if (!"approved".equals(document.getStatus()) && !"archived".equals(document.getStatus())) {
            throw new BusinessException("只有审批通过或已归档的公文可以分发");
        }
        User receiver = repo.findUserById(request.getReceiverId());
        if (receiver == null || !request.getReceiverDeptId().equals(receiver.getDeptId())) {
            throw new BusinessException("接收人或接收部门无效");
        }
        DocumentDistribution distribution = new DocumentDistribution();
        OmsRepository.fillEntity(distribution, repo.nextId());
        distribution.setDocumentId(id);
        distribution.setReceiverId(request.getReceiverId());
        distribution.setReceiverDeptId(request.getReceiverDeptId());
        distribution.setStatus("distributed");
        distribution.setDistributedAt(LocalDateTime.now());
        document.setDistributionStatus("distributed");
        document.setUpdatedAt(LocalDateTime.now());
        repo.saveDocumentDistribution(distribution);
        repo.saveDocument(document);
        workflowService.notifyUser(distribution.getReceiverId(), "公文待签收",
                document.getTitle() + " 已分发，请及时签收", "document", id);
        workflowService.audit("document", "distribute", "document", id, "分发至用户#" + distribution.getReceiverId());
        return distribution;
    }

    /** 获取公文的分发记录列表 */
    public List<DocumentDistribution> distributions(Long id) {
        find(id);
        accessService.requireBusinessRead("document", id);
        List<DocumentDistribution> rows = new ArrayList<DocumentDistribution>(repo.findDocumentDistributionsByDocumentId(id));
        rows.sort(Comparator.comparing(DocumentDistribution::getCreatedAt));
        return rows;
    }

    /** 签收公文分发 */
    public DocumentDistribution receipt(Long id, Long distributionId) {
        Document document = find(id);
        DocumentDistribution distribution = findDistribution(id, distributionId);
        accessService.requireDocumentReceipt(distribution);
        distribution.setStatus("received");
        distribution.setReceivedAt(LocalDateTime.now());
        distribution.setUpdatedAt(LocalDateTime.now());
        // 根据全员签收情况更新公文的分发状态
        document.setDistributionStatus(allReceived(id) ? "received" : "partially_received");
        document.setUpdatedAt(LocalDateTime.now());
        repo.saveDocumentDistribution(distribution);
        repo.saveDocument(document);
        workflowService.audit("document", "receipt", "document", id, "签收记录#" + distributionId);
        return distribution;
    }

    /** 催办公文签收 */
    public DocumentDistribution remind(Long id, Long distributionId) {
        Document document = find(id);
        accessService.requireDocumentRemind(document);
        DocumentDistribution distribution = findDistribution(id, distributionId);
        if ("received".equals(distribution.getStatus())) {
            throw new BusinessException("已签收公文无需催办");
        }
        distribution.setRemindedAt(LocalDateTime.now());
        distribution.setUpdatedAt(LocalDateTime.now());
        repo.saveDocumentDistribution(distribution);
        workflowService.notifyUser(distribution.getReceiverId(), "公文签收催办",
                document.getTitle() + " 尚未签收，请及时办理", "document", id);
        workflowService.audit("document", "remind_receipt", "document", id, "催办记录#" + distributionId);
        return distribution;
    }

    /** AI智能起草公文 */
    public String draft(AiDraftRequest request) {
        return aiProvider.draft(request.getDocType(), request.getTopic(), request.getKeyPoints());
    }

    /** 根据ID查找公文，不存在则抛异常 */
    private Document find(Long id) {
        Document document = repo.findDocumentById(id);
        if (document == null) {
            throw new BusinessException("公文不存在");
        }
        return document;
    }

    /** 查找公文的分发记录，不存在则抛异常 */
    private DocumentDistribution findDistribution(Long documentId, Long distributionId) {
        DocumentDistribution distribution = repo.findDocumentDistributionById(distributionId);
        if (distribution == null || !documentId.equals(distribution.getDocumentId())) {
            throw new BusinessException("公文分发记录不存在");
        }
        return distribution;
    }

    /** 判断公文的所有分发记录是否都已签收 */
    private boolean allReceived(Long documentId) {
        boolean found = false;
        for (DocumentDistribution distribution : repo.findDocumentDistributionsByDocumentId(documentId)) {
            found = true;
            if (!"received".equals(distribution.getStatus())) {
                return false;
            }
        }
        return found;
    }

    /** 判断用户是否可查看所有公文（管理员、党办校办、校级领导） */
    private boolean canViewAll(User user) {
        return user.getRoleKeys().contains("admin")
                || user.getRoleKeys().contains("office_admin")
                || user.getRoleKeys().contains("school_leader");
    }
}
