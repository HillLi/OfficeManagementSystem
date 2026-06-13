package com.university.oms.model;

import java.time.LocalDateTime;

/**
 * 站内信收件人实体，记录每封邮件的接收人及阅读状态
 */
public class MailRecipient extends BaseEntity {
    /** 关联的邮件ID */
    private Long mailId;
    /** 收件人用户ID */
    private Long userId;
    /** 收件人类型（to / cc） */
    private String recipientType;
    /** 是否已读 */
    private boolean readStatus;
    private LocalDateTime readAt;
    /** 邮件发送状态（pending / sent / failed） */
    private String emailStatus = "pending";
    /** 发送失败时的错误信息 */
    private String emailError;
    private LocalDateTime emailSentAt;

    public Long getMailId() { return mailId; }
    public void setMailId(Long mailId) { this.mailId = mailId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getRecipientType() { return recipientType; }
    public void setRecipientType(String recipientType) { this.recipientType = recipientType; }
    public boolean isReadStatus() { return readStatus; }
    public void setReadStatus(boolean readStatus) { this.readStatus = readStatus; }
    public LocalDateTime getReadAt() { return readAt; }
    public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }
    public String getEmailStatus() { return emailStatus; }
    public void setEmailStatus(String emailStatus) { this.emailStatus = emailStatus; }
    public String getEmailError() { return emailError; }
    public void setEmailError(String emailError) { this.emailError = emailError; }
    public LocalDateTime getEmailSentAt() { return emailSentAt; }
    public void setEmailSentAt(LocalDateTime emailSentAt) { this.emailSentAt = emailSentAt; }
}
