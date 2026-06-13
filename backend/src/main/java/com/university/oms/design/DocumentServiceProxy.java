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

// 代理模式：公文服务代理，在调用真实服务前后增加权限校验和操作日志
@Service
public class DocumentServiceProxy {
    private static final Logger log = LoggerFactory.getLogger(DocumentServiceProxy.class);

    private final DocumentService delegate; // 被代理的真实公文服务
    private final OmsRepository repo;

    public DocumentServiceProxy(DocumentService delegate, OmsRepository repo) {
        this.delegate = delegate;
        this.repo = repo;
    }

    // 带权限校验的公文创建操作
    public Document createWithPermissionCheck(DocumentRequest request, Long currentUserId) {
        checkPermission(currentUserId, "office_user");
        logOperation("create_document", currentUserId);
        return delegate.create(request);
    }

    // 带所有权校验的公文提交操作
    public Document submitWithPermissionCheck(Long id, Long currentUserId) {
        checkOwnership(id, currentUserId);
        logOperation("submit_document", currentUserId);
        return delegate.submit(id);
    }

    // 直接委托审核操作（无额外校验）
    public AiReviewResult review(Long id) {
        return delegate.review(id);
    }

    // 直接委托AI草稿生成操作
    public String draft(AiDraftRequest request) {
        return delegate.draft(request);
    }

    // 直接委托公文列表查询操作
    public List<Document> list() {
        return delegate.list();
    }

    // 校验用户是否拥有指定角色权限
    private void checkPermission(Long userId, String requiredRole) {
        User user = repo.findUserById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!user.getRoleKeys().contains(requiredRole) && !user.getRoleKeys().contains("admin")) {
            throw new BusinessException("无权执行此操作，需要角色：" + requiredRole);
        }
    }

    // 校验用户是否为公文的所有者或管理员
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

    // 记录操作日志
    private void logOperation(String action, Long userId) {
        log.info("Document operation: {} by user {}", action, userId);
    }
}
