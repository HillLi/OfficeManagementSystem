package com.university.oms.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 邮件详情响应（包含发件人、收件人列表及当前用户的阅读状态）
 */
public class MailDetailResponse {
    private Long id;
    private Long senderId;
    private String senderName;
    private String subject;
    private String content;
    private LocalDateTime createdAt;
    /** 当前用户在该邮件中的收件人类型（to/cc） */
    private String currentUserRecipientType;
    /** 当前用户是否已读 */
    private boolean currentUserRead;
    private List<MailRecipientResponse> recipients = new ArrayList<MailRecipientResponse>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getCurrentUserRecipientType() {
        return currentUserRecipientType;
    }

    public void setCurrentUserRecipientType(String currentUserRecipientType) {
        this.currentUserRecipientType = currentUserRecipientType;
    }

    public boolean isCurrentUserRead() {
        return currentUserRead;
    }

    public void setCurrentUserRead(boolean currentUserRead) {
        this.currentUserRead = currentUserRead;
    }

    public List<MailRecipientResponse> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<MailRecipientResponse> recipients) {
        this.recipients = recipients;
    }
}
