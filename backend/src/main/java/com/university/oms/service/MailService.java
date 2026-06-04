package com.university.oms.service;

import com.university.oms.common.BusinessException;
import com.university.oms.common.ForbiddenException;
import com.university.oms.dto.MailDetailResponse;
import com.university.oms.dto.MailRecipientResponse;
import com.university.oms.dto.MailSendRequest;
import com.university.oms.model.MailMessage;
import com.university.oms.model.MailRecipient;
import com.university.oms.model.User;
import com.university.oms.repository.DataPersistence;
import com.university.oms.repository.InMemoryDatabase;
import com.university.oms.security.AuthContext;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MailService {
    private final InMemoryDatabase db;
    private final DataPersistence persistence;
    private final WorkflowService workflowService;

    public MailService(InMemoryDatabase db, DataPersistence persistence, WorkflowService workflowService) {
        this.db = db;
        this.persistence = persistence;
        this.workflowService = workflowService;
    }

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
        db.fill(message, db.nextId());
        message.setSenderId(sender.getId());
        message.setSubject(subject);
        message.setContent(content);
        db.mailMessages().put(message.getId(), message);
        persistence.saveMailMessage(message);

        saveRecipients(message, toUserIds, "to");
        saveRecipients(message, ccUserIds, "cc");
        return response(message, sender.getId());
    }

    public List<MailDetailResponse> inbox() {
        Long userId = AuthContext.requireUser().getId();
        return db.mailRecipients().stream()
                .filter(recipient -> userId.equals(recipient.getUserId()))
                .map(recipient -> db.mailMessages().get(recipient.getMailId()))
                .filter(message -> message != null)
                .sorted(messageOrder())
                .map(message -> response(message, userId))
                .collect(Collectors.toList());
    }

    public List<MailDetailResponse> sent() {
        Long userId = AuthContext.requireUser().getId();
        return db.mailMessages().values().stream()
                .filter(message -> userId.equals(message.getSenderId()))
                .sorted(messageOrder())
                .map(message -> response(message, userId))
                .collect(Collectors.toList());
    }

    public MailDetailResponse detail(Long id) {
        User user = AuthContext.requireUser();
        MailMessage message = requireMessage(id);
        if (!user.getId().equals(message.getSenderId()) && findRecipient(id, user.getId()) == null) {
            throw new ForbiddenException("无权查看该邮件");
        }
        return response(message, user.getId());
    }

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
            persistence.saveMailRecipient(recipient);
        }
        return response(message, user.getId());
    }

    private void saveRecipients(MailMessage message, Set<Long> userIds, String recipientType) {
        for (Long userId : userIds) {
            MailRecipient recipient = new MailRecipient();
            db.fill(recipient, db.nextId());
            recipient.setMailId(message.getId());
            recipient.setUserId(userId);
            recipient.setRecipientType(recipientType);
            db.mailRecipients().add(recipient);
            persistence.saveMailRecipient(recipient);
            workflowService.notifyUser(userId, "新邮件：" + message.getSubject(), message.getContent(), "mail",
                    message.getId());
        }
    }

    private MailDetailResponse response(MailMessage message, Long currentUserId) {
        MailDetailResponse response = new MailDetailResponse();
        response.setId(message.getId());
        response.setSenderId(message.getSenderId());
        User sender = db.users().get(message.getSenderId());
        response.setSenderName(sender == null ? null : sender.getRealName());
        response.setSubject(message.getSubject());
        response.setContent(message.getContent());
        response.setCreatedAt(message.getCreatedAt());
        response.setCurrentUserRecipientType("sender");
        response.setCurrentUserRead(true);

        List<MailRecipient> recipients = recipients(message.getId());
        for (MailRecipient recipient : recipients) {
            response.getRecipients().add(recipientResponse(recipient));
            if (recipient.getUserId().equals(currentUserId)) {
                response.setCurrentUserRecipientType(recipient.getRecipientType());
                response.setCurrentUserRead(recipient.isReadStatus());
            }
        }
        return response;
    }

    private MailRecipientResponse recipientResponse(MailRecipient recipient) {
        MailRecipientResponse response = new MailRecipientResponse();
        response.setUserId(recipient.getUserId());
        User user = db.users().get(recipient.getUserId());
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
        return db.mailRecipients().stream()
                .filter(recipient -> mailId.equals(recipient.getMailId()))
                .sorted(Comparator.comparing(MailRecipient::getId))
                .collect(Collectors.toList());
    }

    private MailRecipient findRecipient(Long mailId, Long userId) {
        for (MailRecipient recipient : db.mailRecipients()) {
            if (mailId.equals(recipient.getMailId()) && userId.equals(recipient.getUserId())) {
                return recipient;
            }
        }
        return null;
    }

    private MailMessage requireMessage(Long id) {
        MailMessage message = db.mailMessages().get(id);
        if (message == null) {
            throw new BusinessException("邮件不存在");
        }
        return message;
    }

    private Set<Long> deduplicate(List<Long> userIds) {
        return userIds == null ? new LinkedHashSet<Long>() : new LinkedHashSet<Long>(userIds);
    }

    private void requireUsersExist(Set<Long> userIds) {
        List<Long> unknownIds = new ArrayList<Long>();
        for (Long userId : userIds) {
            if (userId == null || !db.users().containsKey(userId)) {
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
}
