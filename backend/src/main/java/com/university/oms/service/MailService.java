package com.university.oms.service;

import com.university.oms.common.BusinessException;
import com.university.oms.common.ForbiddenException;
import com.university.oms.dto.MailDetailResponse;
import com.university.oms.dto.MailRecipientResponse;
import com.university.oms.dto.MailSendRequest;
import com.university.oms.model.MailMessage;
import com.university.oms.model.MailRecipient;
import com.university.oms.model.User;
import com.university.oms.repository.OmsRepository;
import com.university.oms.security.AuthContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 邮件服务，处理站内邮件的发送、收件箱、已发送和外部邮件投递
 */
@Service
public class MailService {
    private static final Logger log = LoggerFactory.getLogger(MailService.class);
    private static final int NOTIFICATION_TITLE_LIMIT = 255;
    private static final int NOTIFICATION_CONTENT_LIMIT = 1000;
    private static final int EMAIL_ERROR_LIMIT = 1000;

    private final OmsRepository repo;
    private final WorkflowService workflowService;
    private final EmailSenderService emailSenderService;

    public MailService(OmsRepository repo, WorkflowService workflowService,
                       EmailSenderService emailSenderService) {
        this.repo = repo;
        this.workflowService = workflowService;
        this.emailSenderService = emailSenderService;
    }

    /** 发送邮件，包含收件人和抄送人 */
    public MailDetailResponse send(MailSendRequest request) {
        User sender = AuthContext.requireUser();
        String subject = trimRequired(request.getSubject(), "邮件主题不能为空");
        String content = trimRequired(request.getContent(), "邮件内容不能为空");
        Set<Long> toUserIds = deduplicate(request.getToUserIds());
        if (toUserIds.isEmpty()) {
            throw new BusinessException("收件人不能为空");
        }
        Set<Long> ccUserIds = deduplicate(request.getCcUserIds());
        ccUserIds.removeAll(toUserIds);
        requireUsersExist(toUserIds);
        requireUsersExist(ccUserIds);

        MailMessage message = new MailMessage();
        OmsRepository.fillEntity(message, repo.nextId());
        message.setSenderId(sender.getId());
        message.setSubject(subject);
        message.setContent(content);
        repo.saveMailMessage(message);

        saveRecipients(message, toUserIds, "to");
        saveRecipients(message, ccUserIds, "cc");
        return response(message, sender.getId(), false);
    }

    /** 获取当前用户的收件箱列表 */
    public List<MailDetailResponse> inbox() {
        Long userId = AuthContext.requireUser().getId();
        return repo.findMailRecipientsByUserId(userId).stream()
                .map(recipient -> repo.findMailMessageById(recipient.getMailId()))
                .filter(message -> message != null)
                .sorted(messageOrder())
                .map(message -> response(message, userId, true))
                .collect(Collectors.toList());
    }

    /** 获取当前用户的已发送邮件列表 */
    public List<MailDetailResponse> sent() {
        Long userId = AuthContext.requireUser().getId();
        return repo.findAllMailMessages().stream()
                .filter(message -> userId.equals(message.getSenderId()))
                .sorted(messageOrder())
                .map(message -> response(message, userId, false))
                .collect(Collectors.toList());
    }

    /** 获取邮件详情 */
    public MailDetailResponse detail(Long id) {
        User user = AuthContext.requireUser();
        MailMessage message = requireMessage(id);
        if (!user.getId().equals(message.getSenderId()) && findRecipient(id, user.getId()) == null) {
            throw new ForbiddenException("无权查看该邮件");
        }
        return response(message, user.getId(), false);
    }

    /** 标记邮件为已读 */
    public MailDetailResponse markRead(Long id) {
        User user = AuthContext.requireUser();
        MailMessage message = requireMessage(id);
        MailRecipient recipient = findRecipient(id, user.getId());
        if (recipient == null) {
            throw new ForbiddenException("只有收件人可以标记邮件已读");
        }
        if (!recipient.isReadStatus()) {
            LocalDateTime now = LocalDateTime.now();
            recipient.setReadStatus(true);
            recipient.setReadAt(now);
            recipient.setUpdatedAt(now);
            repo.saveMailRecipient(recipient);
        }
        return response(message, user.getId(), true);
    }

    /** 重试发送失败的外部邮件 */
    public MailDetailResponse retryEmail(Long id) {
        User user = AuthContext.requireUser();
        MailMessage message = requireMessage(id);
        boolean sender = user.getId().equals(message.getSenderId());
        boolean admin = user.getRoleKeys().contains("admin");
        if (!sender && !admin) {
            throw new ForbiddenException("只有发件人或管理员可以重试邮件发送");
        }
        User mailSender = repo.findUserById(message.getSenderId());
        for (MailRecipient recipient : recipients(message.getId())) {
            if ("failed".equals(recipient.getEmailStatus()) || "skipped".equals(recipient.getEmailStatus())) {
                deliverExternalEmail(message, recipient, mailSender, repo.findUserById(recipient.getUserId()));
            }
        }
        return response(message, user.getId(), false, sender, sender);
    }

    /** 保存收件人记录，发送站内通知和外部邮件 */
    private void saveRecipients(MailMessage message, Set<Long> userIds, String recipientType) {
        for (Long userId : userIds) {
            MailRecipient recipient = new MailRecipient();
            OmsRepository.fillEntity(recipient, repo.nextId());
            recipient.setMailId(message.getId());
            recipient.setUserId(userId);
            recipient.setRecipientType(recipientType);
            repo.saveMailRecipient(recipient);
            try {
                workflowService.notifyUser(userId, notificationTitle(message), notificationContent(), "mail",
                        message.getId());
            } catch (RuntimeException ex) {
                log.warn("Failed to create mail notification for mailId={}, userId={}", message.getId(), userId, ex);
            }
            deliverExternalEmail(message, recipient, repo.findUserById(message.getSenderId()), repo.findUserById(userId));
        }
    }

    private MailDetailResponse response(MailMessage message, Long currentUserId, boolean recipientContext) {
        return response(message, currentUserId, recipientContext, false, true);
    }

    /** 构建邮件详情响应对象 */
    private MailDetailResponse response(MailMessage message, Long currentUserId, boolean recipientContext,
                                        boolean includeAllRecipients, boolean includeContent) {
        MailDetailResponse response = new MailDetailResponse();
        response.setId(message.getId());
        response.setSenderId(message.getSenderId());
        User sender = repo.findUserById(message.getSenderId());
        response.setSenderName(sender == null ? null : sender.getRealName());
        response.setSubject(message.getSubject());
        response.setContent(includeContent ? message.getContent() : null);
        response.setCreatedAt(message.getCreatedAt());
        response.setCurrentUserRecipientType(message.getSenderId().equals(currentUserId) ? "sender" : "admin");
        response.setCurrentUserRead(true);

        List<MailRecipient> recipients = recipients(message.getId());
        MailRecipient currentRecipient = findRecipient(recipients, currentUserId);
        boolean currentUserIsSender = message.getSenderId().equals(currentUserId);
        if (currentRecipient != null && (recipientContext || !currentUserIsSender)) {
            response.setCurrentUserRecipientType(currentRecipient.getRecipientType());
            response.setCurrentUserRead(currentRecipient.isReadStatus());
        }
        for (MailRecipient recipient : recipients) {
            if (includeAllRecipients || currentUserIsSender || recipient.getUserId().equals(currentUserId)) {
                response.getRecipients().add(recipientResponse(recipient));
            }
        }
        return response;
    }

    /** 投递外部邮件，失败时记录错误状态 */
    private void deliverExternalEmail(MailMessage message, MailRecipient recipient, User sender, User receiver) {
        try {
            if (!emailSenderService.isEnabled()) {
                markEmailSkipped(recipient, emailSenderService.disabledReason());
                return;
            }
            if (receiver == null || receiver.getEmail() == null || receiver.getEmail().trim().isEmpty()) {
                markEmailSkipped(recipient, "recipient email missing");
                return;
            }
            emailSenderService.sendMail(receiver.getEmail(), message.getSubject(), externalMailContent(message, sender));
            LocalDateTime now = LocalDateTime.now();
            recipient.setEmailStatus("sent");
            recipient.setEmailError(null);
            recipient.setEmailSentAt(now);
            recipient.setUpdatedAt(now);
            repo.saveMailRecipient(recipient);
        } catch (RuntimeException ex) {
            LocalDateTime now = LocalDateTime.now();
            recipient.setEmailStatus("failed");
            recipient.setEmailError(truncate(ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage(),
                    EMAIL_ERROR_LIMIT));
            recipient.setEmailSentAt(null);
            recipient.setUpdatedAt(now);
            repo.saveMailRecipient(recipient);
            log.warn("Failed to send external mail for mailId={}, userId={}", message.getId(), recipient.getUserId(), ex);
        }
    }

    /** 标记外部邮件为跳过状态 */
    private void markEmailSkipped(MailRecipient recipient, String reason) {
        LocalDateTime now = LocalDateTime.now();
        recipient.setEmailStatus("skipped");
        recipient.setEmailError(truncate(reason, EMAIL_ERROR_LIMIT));
        recipient.setEmailSentAt(null);
        recipient.setUpdatedAt(now);
        repo.saveMailRecipient(recipient);
    }

    private String externalMailContent(MailMessage message, User sender) {
        String senderName = sender == null ? "系统用户" : sender.getRealName();
        return "发件人：" + senderName + "\n\n" + message.getContent();
    }

    private MailRecipientResponse recipientResponse(MailRecipient recipient) {
        MailRecipientResponse response = new MailRecipientResponse();
        response.setUserId(recipient.getUserId());
        User user = repo.findUserById(recipient.getUserId());
        response.setRealName(user == null ? null : user.getRealName());
        response.setDeptName(user == null ? null : user.getDeptName());
        response.setRecipientType(recipient.getRecipientType());
        response.setReadStatus(recipient.isReadStatus());
        response.setEmailStatus(recipient.getEmailStatus());
        response.setEmailError(recipient.getEmailError());
        response.setEmailSentAt(recipient.getEmailSentAt());
        return response;
    }

    private List<MailRecipient> recipients(Long mailId) {
        return repo.findMailRecipientsByMailId(mailId).stream()
                .sorted(Comparator.comparing(MailRecipient::getId))
                .collect(Collectors.toList());
    }

    private MailRecipient findRecipient(Long mailId, Long userId) {
        return repo.findMailRecipientByMailIdAndUserId(mailId, userId);
    }

    private MailRecipient findRecipient(List<MailRecipient> recipients, Long userId) {
        for (MailRecipient recipient : recipients) {
            if (userId.equals(recipient.getUserId())) {
                return recipient;
            }
        }
        return null;
    }

    private MailMessage requireMessage(Long id) {
        MailMessage message = repo.findMailMessageById(id);
        if (message == null) {
            throw new BusinessException("邮件不存在");
        }
        return message;
    }

    private Set<Long> deduplicate(List<Long> userIds) {
        return userIds == null ? new LinkedHashSet<Long>() : new LinkedHashSet<Long>(userIds);
    }

    /** 校验所有指定的用户ID是否存在 */
    private void requireUsersExist(Set<Long> userIds) {
        List<Long> unknownIds = new ArrayList<Long>();
        for (Long userId : userIds) {
            if (userId == null || repo.findUserById(userId) == null) {
                unknownIds.add(userId);
            }
        }
        if (!unknownIds.isEmpty()) {
            throw new BusinessException("用户不存在：" + unknownIds);
        }
    }

    private Comparator<MailMessage> messageOrder() {
        return Comparator.comparing(MailMessage::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(MailMessage::getId, Comparator.reverseOrder());
    }

    private String trimRequired(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new BusinessException(message);
        }
        return value.trim();
    }

    private String notificationTitle(MailMessage message) {
        return truncate("新邮件：" + message.getSubject(), NOTIFICATION_TITLE_LIMIT);
    }

    private String notificationContent() {
        return truncate("您收到一封新的站内邮件，请在邮件中心查看。", NOTIFICATION_CONTENT_LIMIT);
    }

    private String truncate(String value, int maxLength) {
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
