package com.university.oms.dto;

import java.time.LocalDateTime;

/**
 * 邮件收件人信息响应
 */
public class MailRecipientResponse {
    private Long userId;
    private String realName;
    private String deptName;
    /** 收件人类型（to/cc） */
    private String recipientType;
    /** 是否已读 */
    private boolean readStatus;
    /** 邮件发送状态 */
    private String emailStatus;
    /** 邮件发送失败时的错误信息 */
    private String emailError;
    /** 邮件发送时间 */
    private LocalDateTime emailSentAt;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public String getRecipientType() {
        return recipientType;
    }

    public void setRecipientType(String recipientType) {
        this.recipientType = recipientType;
    }

    public boolean isReadStatus() {
        return readStatus;
    }

    public void setReadStatus(boolean readStatus) {
        this.readStatus = readStatus;
    }

    public String getEmailStatus() {
        return emailStatus;
    }

    public void setEmailStatus(String emailStatus) {
        this.emailStatus = emailStatus;
    }

    public String getEmailError() {
        return emailError;
    }

    public void setEmailError(String emailError) {
        this.emailError = emailError;
    }

    public LocalDateTime getEmailSentAt() {
        return emailSentAt;
    }

    public void setEmailSentAt(LocalDateTime emailSentAt) {
        this.emailSentAt = emailSentAt;
    }
}
