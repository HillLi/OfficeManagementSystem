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

/**
 * 工作流服务，管理审批流实例、待办任务、通知、附件和审计日志
 */
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

    /** 添加附件记录（非用印业务） */
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

    /** 查询指定业务的附件列表 */
    public List<Attachment> attachments(String bizType, Long bizId) {
        return attachments(bizType, bizId, false);
    }

    /** 查询附件列表，支持包含已删除的附件 */
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

    /** 通过文件上传方式添加附件（用印业务） */
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

    /** 根据ID获取附件 */
    public Attachment attachment(Long id) {
        Attachment attachment = repo.findAttachmentById(id);
        if (attachment == null) {
            throw new BusinessException("材料不存在");
        }
        return attachment;
    }

    /** 下载附件文件 */
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

    /** 更新附件信息（文件名和密级） */
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

    /** 逻辑删除附件，记录删除原因 */
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

    /** 获取未删除的附件，不存在或已删除则抛异常 */
    private Attachment activeAttachment(Long id) {
        Attachment attachment = attachment(id);
        if (attachment.isDeleted()) {
            throw new BusinessException("材料已删除");
        }
        return attachment;
    }

    /** 记录审计日志 */
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

    /** 查询审计日志列表 */
    public List<AuditLog> auditLogs(String bizType, Long bizId) {
        return repo.findAllAuditLogs().stream()
                .filter(l -> bizType == null || bizType.equals(l.getBizType()))
                .filter(l -> bizId == null || bizId.equals(l.getBizId()))
                .collect(Collectors.toList());
    }

    /** 向指定用户发送站内通知 */
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

    /** 获取当前用户的通知列表，支持仅显示未读 */
    public List<Notification> notifications(boolean unreadOnly) {
        User user = AuthContext.requireUser();
        return repo.findNotificationsByReceiverId(user.getId()).stream()
                .filter(n -> !unreadOnly || !n.isReadStatus())
                .collect(Collectors.toList());
    }

    /** 标记通知为已读 */
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

    /** 启动审批流程，创建流程实例和待办任务 */
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

    /** 推进审批流程到下一个节点 */
    public void advanceFlow(String bizType, Long bizId, String oldStatus, String newStatus, Long applicantId) {
        FlowInstance instance = repo.findFlowInstanceByBizTypeAndBizId(bizType, bizId);
        if (instance == null) {
            instance = startFlow(bizType, bizId, oldStatus, applicantId);
        }
        // 关闭当前所有待办任务
        closeOpenTasks(instance.getId(), "reject".equals(newStatus) || "rejected".equals(newStatus) ? "rejected" : "completed");
        instance.setCurrentNodeKey(newStatus);
        instance.setStatus(terminal(newStatus) ? newStatus : "running");
        instance.setUpdatedAt(LocalDateTime.now());
        repo.saveFlowInstance(instance);
        // 新节点为待审批状态时创建待办任务并通知审批人
        if (newStatus != null && newStatus.startsWith("pending_")) {
            createPendingTask(instance, newStatus);
            notifyNextApprovers(bizType, bizId, newStatus);
        } else {
            notifyUser(applicantId, "流程状态变更", bizType + "#" + bizId + " 已流转为 " + newStatus, bizType, bizId);
        }
        audit(bizType, "advance_flow", bizType, bizId, oldStatus + " -> " + newStatus);
    }

    /** 获取当前用户可见的流程实例列表 */
    public List<FlowInstance> flowInstances() {
        User user = AuthContext.requireUser();
        return repo.findAllFlowInstances().stream()
                .filter(i -> canViewInstance(user, i))
                .collect(Collectors.toList());
    }

    /** 获取待办任务列表，支持仅显示当前用户的任务 */
    public List<FlowTask> tasks(boolean onlyMine) {
        User user = AuthContext.requireUser();
        return repo.findAllFlowTasks().stream()
                .filter(t -> !"pending".equals(t.getStatus()) || canHandle(user, t))
                .filter(t -> !onlyMine || "pending".equals(t.getStatus()))
                .collect(Collectors.toList());
    }

    /** 为流程实例创建待办任务（同一节点不重复创建） */
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

    /** 关闭指定流程实例的所有待办任务 */
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

    /** 通知下一个审批节点的有权限审批人 */
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

    /** 判断用户是否可以处理指定待办任务 */
    private boolean canHandle(User user, FlowTask task) {
        return user.getRoleKeys().contains("admin")
                || (task.getApproverId() != null && task.getApproverId().equals(user.getId()))
                || accessService.canHandleApproval(user, task.getBizType(), task.getBizId(), task.getNodeKey(),
                        task.getApproverRole());
    }

    /** 判断用户是否可以查看指定流程实例 */
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

    /** 判断状态是否为终态（已审批/已驳回/已归档/已归还/已完成） */
    private boolean terminal(String status) {
        return "approved".equals(status) || "rejected".equals(status) || "archived".equals(status)
                || "returned".equals(status) || "completed".equals(status);
    }

}
