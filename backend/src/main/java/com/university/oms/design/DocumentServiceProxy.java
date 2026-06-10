package com.university.oms.design;

import com.university.oms.common.BusinessException;
import com.university.oms.dto.AiDraftRequest;
import com.university.oms.dto.DocumentRequest;
import com.university.oms.model.AiReviewResult;
import com.university.oms.model.Document;
import com.university.oms.model.User;
import com.university.oms.repository.OmsRepository;
import com.university.oms.service.DocumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Proxy pattern — wraps DocumentService with permission checks and operation logging.
 */
@Service
public class DocumentServiceProxy {
    private static final Logger log = LoggerFactory.getLogger(DocumentServiceProxy.class);

    private final DocumentService delegate;
    private final OmsRepository repo;

    public DocumentServiceProxy(DocumentService delegate, OmsRepository repo) {
        this.delegate = delegate;
        this.repo = repo;
    }

    public Document createWithPermissionCheck(DocumentRequest request, Long currentUserId) {
        checkPermission(currentUserId, "office_user");
        logOperation("create_document", currentUserId);
        return delegate.create(request);
    }

    public Document submitWithPermissionCheck(Long id, Long currentUserId) {
        checkOwnership(id, currentUserId);
        logOperation("submit_document", currentUserId);
        return delegate.submit(id);
    }

    public AiReviewResult review(Long id) {
        return delegate.review(id);
    }

    public String draft(AiDraftRequest request) {
        return delegate.draft(request);
    }

    public List<Document> list() {
        return delegate.list();
    }

    private void checkPermission(Long userId, String requiredRole) {
        User user = repo.findUserById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!user.getRoleKeys().contains(requiredRole) && !user.getRoleKeys().contains("admin")) {
            throw new BusinessException("无权执行此操作，需要角色：" + requiredRole);
        }
    }

    private void checkOwnership(Long docId, Long userId) {
        Document doc = repo.findDocumentById(docId);
        if (doc == null) {
            throw new BusinessException("公文不存在");
        }
        User owner = repo.findUserById(userId);
        if (!doc.getApplicantId().equals(userId) && owner != null && !owner.getRoleKeys().contains("admin")) {
            throw new BusinessException("只能操作自己起草的公文");
        }
    }

    private void logOperation(String action, Long userId) {
        log.info("Document operation: {} by user {}", action, userId);
    }
}
