package com.university.oms.service;

import com.university.oms.common.BusinessException;
import com.university.oms.design.ApprovalFlowConfig;
import com.university.oms.dto.AttachmentDeleteRequest;
import com.university.oms.dto.AttachmentRequest;
import com.university.oms.dto.AttachmentUpdateRequest;
import com.university.oms.model.*;
import com.university.oms.repository.OmsRepository;
import com.university.oms.security.AuthContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WorkflowService {
    private final OmsRepository repo;
    private final ApprovalFlowConfig flowConfig;
    private final BusinessAccessService accessService;
    private final AttachmentStorageService storageService;
    private final DictionaryService dictionaryService;

    public WorkflowService(OmsRepository repo, ApprovalFlowConfig flowConfig,
                           BusinessAccessService accessService, AttachmentStorageService storageService,
                           DictionaryService dictionaryService) {
        this.repo = repo;
        this.flowConfig = flowConfig;
        this.accessService = accessService;
        this.storageService = storageService;
        this.dictionaryService = dictionaryService;
    }

    public Attachment addAttachment(AttachmentRequest request) {
        if ("seal".equals(request.getBizType())) {
            throw new BusinessException("用印材料请通过文件上传接口提交");
        }
        User user = AuthContext.requireUser();
        accessService.requireBusinessRead(request.getBizType(), request.getBizId());
        dictionaryService.requireEnabled("secrecy_level", request.getSecrecyLevel(), "材料密级");
        Attachment attachment = new Attachment();
        OmsRepository.fillEntity(attachment, repo.nextId());
        attachment.setBizType(request.getBizType());
        attachment.setBizId(request.getBizId());
        attachment.setFileName(request.getFileName());
        attachment.setFileUrl(request.getFileUrl());
        attachment.setSecrecyLevel(request.getSecrecyLevel());
        attachment.setUploaderId(user.getId());
        repo.saveAttachment(attachment);
        audit(request.getBizType(), "upload_attachment", request.getBizType(), request.getBizId(), request.getFileName());
        return attachment;
    }

    public List<Attachment> attachments(String bizType, Long bizId) {
        return attachments(bizType, bizId, false);
    }

    public List<Attachment> attachments(String bizType, Long bizId, boolean includeDeleted) {
        if (bizType != null && bizId != null) {
            accessService.requireBusinessRead(bizType, bizId);
        }
        if (includeDeleted) {
            accessService.requireViewDeletedAttachments();
        }
        return repo.findAllAttachments().stream()
                .filter(a -> bizType == null || a.getBizType().equals(bizType))
                .filter(a -> bizId == null || a.getBizId().equals(bizId))
                .filter(a -> includeDeleted || !a.isDeleted())
                .filter(a -> bizType != null && bizId != null || accessService.canReadBusiness(a.getBizType(), a.getBizId()))
                .collect(Collectors.toList());
    }

    public Attachment uploadAttachment(String bizType, Long bizId, String secrecyLevel, MultipartFile file) {
        User user = AuthContext.requireUser();
        accessService.requireAttachmentUpload(bizType, bizId);
        String resolvedSecrecyLevel = secrecyLevel == null || secrecyLevel.trim().isEmpty() ? "内部" : secrecyLevel;
        dictionaryService.requireEnabled("secrecy_level", resolvedSecrecyLevel, "材料密级");
        Attachment attachment = new Attachment();
        OmsRepository.fillEntity(attachment, repo.nextId());
        attachment.setBizType(bizType);
        attachment.setBizId(bizId);
        attachment.setOriginalName(file.getOriginalFilename());
        attachment.setFileName(file.getOriginalFilename());
        attachment.setFileSize(file.getSize());
        attachment.setContentType(file.getContentType());
        attachment.setSecrecyLevel(resolvedSecrecyLevel);
        attachment.setUploaderId(user.getId());
        attachment.setStoragePath(storageService.store(attachment.getId(), file));
        attachment.setFileUrl("/api/workflow/attachments/" + attachment.getId() + "/download");
        repo.saveAttachment(attachment);
        audit(bizType, "upload_attachment", bizType, bizId, attachment.getFileName());
        return attachment;
    }

    public Attachment attachment(Long id) {
        Attachment attachment = repo.findAttachmentById(id);
        if (attachment == null) {
            throw new BusinessException("材料不存在");
        }
        return attachment;
    }

    public Resource downloadAttachment(Long id) {
        Attachment attachment = attachment(id);
        if (attachment.isDeleted()) {
            throw new BusinessException("材料已删除，不可下载");
        }
        accessService.requireBusinessRead(attachment.getBizType(), attachment.getBizId());
        audit(attachment.getBizType(), "download_attachment", attachment.getBizType(), attachment.getBizId(),
                attachment.getFileName());
        return storageService.load(attachment.getStoragePath());
    }

    public Attachment updateAttachment(Long id, AttachmentUpdateRequest request) {
        Attachment attachment = activeAttachment(id);
        accessService.requireAttachmentEdit(attachment.getBizType(), attachment.getBizId());
        dictionaryService.requireEnabled("secrecy_level", request.getSecrecyLevel(), "材料密级");
        attachment.setFileName(request.getFileName());
        attachment.setSecrecyLevel(request.getSecrecyLevel());
        attachment.setUpdatedAt(LocalDateTime.now());
        repo.saveAttachment(attachment);
        audit(attachment.getBizType(), "update_attachment", attachment.getBizType(), attachment.getBizId(),
                attachment.getFileName());
        return attachment;
    }

    public Attachment deleteAttachment(Long id, AttachmentDeleteRequest request) {
        Attachment attachment = activeAttachment(id);
        accessService.requireAttachmentDelete(attachment.getBizType(), attachment.getBizId());
        attachment.setDeleted(true);
        attachment.setDeletedBy(AuthContext.requireUser().getId());
        attachment.setDeletedAt(LocalDateTime.now());
        attachment.setDeleteReason(request.getReason());
        attachment.setUpdatedAt(LocalDateTime.now());
        repo.saveAttachment(attachment);
        audit(attachment.getBizType(), "delete_attachment", attachment.getBizType(), attachment.getBizId(),
                request.getReason());
        return attachment;
    }

    private Attachment activeAttachment(Long id) {
        Attachment attachment = attachment(id);
        if (attachment.isDeleted()) {
            throw new BusinessException("材料已删除");
        }
        return attachment;
    }

    public AuditLog audit(String module, String action, String bizType, Long bizId, String detail) {
        Long operatorId = AuthContext.currentUserIdOr(0L);
        AuditLog log = new AuditLog();
        OmsRepository.fillEntity(log, repo.nextId());
        log.setOperatorId(operatorId);
        log.setModule(module);
        log.setAction(action);
        log.setBizType(bizType);
        log.setBizId(bizId);
        log.setDetail(detail);
        repo.saveAuditLog(log);
        return log;
    }

    public List<AuditLog> auditLogs(String bizType, Long bizId) {
        return repo.findAllAuditLogs().stream()
                .filter(l -> bizType == null || bizType.equals(l.getBizType()))
                .filter(l -> bizId == null || bizId.equals(l.getBizId()))
                .collect(Collectors.toList());
    }

    public Notification notifyUser(Long receiverId, String title, String content, String bizType, Long bizId) {
        if (receiverId == null || repo.findUserById(receiverId) == null) {
            return null;
        }
        Notification notification = new Notification();
        OmsRepository.fillEntity(notification, repo.nextId());
        notification.setReceiverId(receiverId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setBizType(bizType);
        notification.setBizId(bizId);
        repo.saveNotification(notification);
        return notification;
    }

    public List<Notification> notifications(boolean unreadOnly) {
        User user = AuthContext.requireUser();
        return repo.findNotificationsByReceiverId(user.getId()).stream()
                .filter(n -> !unreadOnly || !n.isReadStatus())
                .collect(Collectors.toList());
    }

    public Notification markRead(Long id) {
        User user = AuthContext.requireUser();
        for (Notification notification : repo.findAllNotifications()) {
            if (notification.getId().equals(id) && notification.getReceiverId().equals(user.getId())) {
                notification.setReadStatus(true);
                notification.setUpdatedAt(LocalDateTime.now());
                repo.saveNotification(notification);
                return notification;
            }
        }
        throw new BusinessException("通知不存在");
    }

    public FlowInstance startFlow(String bizType, Long bizId, String initialStatus, Long starterId) {
        FlowInstance instance = repo.findFlowInstanceByBizTypeAndBizId(bizType, bizId);
        if (instance == null) {
            instance = new FlowInstance();
            OmsRepository.fillEntity(instance, repo.nextId());
            instance.setBizType(bizType);
            instance.setBizId(bizId);
            instance.setStarterId(starterId);
        }
        instance.setCurrentNodeKey(initialStatus);
        instance.setStatus("running");
        instance.setUpdatedAt(LocalDateTime.now());
        repo.saveFlowInstance(instance);
        createPendingTask(instance, initialStatus);
        audit(bizType, "start_flow", bizType, bizId, "流程进入" + initialStatus);
        notifyNextApprovers(bizType, bizId, initialStatus);
        return instance;
    }

    public void advanceFlow(String bizType, Long bizId, String oldStatus, String newStatus, Long applicantId) {
        FlowInstance instance = repo.findFlowInstanceByBizTypeAndBizId(bizType, bizId);
        if (instance == null) {
            instance = startFlow(bizType, bizId, oldStatus, applicantId);
        }
        closeOpenTasks(instance.getId(), "reject".equals(newStatus) || "rejected".equals(newStatus) ? "rejected" : "completed");
        instance.setCurrentNodeKey(newStatus);
        instance.setStatus(terminal(newStatus) ? newStatus : "running");
        instance.setUpdatedAt(LocalDateTime.now());
        repo.saveFlowInstance(instance);
        if (newStatus != null && newStatus.startsWith("pending_")) {
            createPendingTask(instance, newStatus);
            notifyNextApprovers(bizType, bizId, newStatus);
        } else {
            notifyUser(applicantId, "流程状态变更", bizType + "#" + bizId + " 已流转为 " + newStatus, bizType, bizId);
        }
        audit(bizType, "advance_flow", bizType, bizId, oldStatus + " -> " + newStatus);
    }

    public List<FlowInstance> flowInstances() {
        User user = AuthContext.requireUser();
        return repo.findAllFlowInstances().stream()
                .filter(i -> canViewInstance(user, i))
                .collect(Collectors.toList());
    }

    public List<FlowTask> tasks(boolean onlyMine) {
        User user = AuthContext.requireUser();
        return repo.findAllFlowTasks().stream()
                .filter(t -> !"pending".equals(t.getStatus()) || canHandle(user, t))
                .filter(t -> !onlyMine || "pending".equals(t.getStatus()))
                .collect(Collectors.toList());
    }

    private void createPendingTask(FlowInstance instance, String nodeKey) {
        for (FlowTask existing : repo.findAllFlowTasks()) {
            if (existing.getInstanceId().equals(instance.getId()) && nodeKey.equals(existing.getNodeKey())
                    && "pending".equals(existing.getStatus())) {
                return;
            }
        }
        String role = flowConfig.getRequiredRole(nodeKey);
        FlowTask task = new FlowTask();
        OmsRepository.fillEntity(task, repo.nextId());
        task.setInstanceId(instance.getId());
        task.setBizType(instance.getBizType());
        task.setBizId(instance.getBizId());
        task.setNodeKey(nodeKey);
        task.setApproverRole(role);
        task.setStatus("pending");
        task.setDueTime(LocalDateTime.now().plusDays(3));
        repo.saveFlowTask(task);
    }

    private void closeOpenTasks(Long instanceId, String status) {
        for (FlowTask task : repo.findAllFlowTasks()) {
            if (task.getInstanceId().equals(instanceId) && "pending".equals(task.getStatus())) {
                task.setStatus(status);
                task.setApproverId(AuthContext.currentUserIdOr(null));
                task.setUpdatedAt(LocalDateTime.now());
                repo.saveFlowTask(task);
            }
        }
    }

    private void notifyNextApprovers(String bizType, Long bizId, String nodeKey) {
        String role = flowConfig.getRequiredRole(nodeKey);
        if (role == null) {
            return;
        }
        for (User user : repo.findAllUsers()) {
            if (accessService.canHandleApproval(user, bizType, bizId, nodeKey, role)) {
                notifyUser(user.getId(), "新的待办审批", bizType + "#" + bizId + " 等待您处理：" + nodeKey, bizType, bizId);
            }
        }
    }

    private boolean canHandle(User user, FlowTask task) {
        return user.getRoleKeys().contains("admin")
                || (task.getApproverId() != null && task.getApproverId().equals(user.getId()))
                || accessService.canHandleApproval(user, task.getBizType(), task.getBizId(), task.getNodeKey(),
                        task.getApproverRole());
    }

    private boolean canViewInstance(User user, FlowInstance instance) {
        if (user.getRoleKeys().contains("admin") || user.getRoleKeys().contains("office_admin")
                || user.getRoleKeys().contains("school_leader")) {
            return true;
        }
        if (user.getId().equals(instance.getStarterId())) {
            return true;
        }
        for (FlowTask task : repo.findAllFlowTasks()) {
            if (task.getInstanceId().equals(instance.getId()) && canHandle(user, task)) {
                return true;
            }
        }
        return false;
    }

    private boolean terminal(String status) {
        return "approved".equals(status) || "rejected".equals(status) || "archived".equals(status)
                || "returned".equals(status) || "completed".equals(status);
    }

}
