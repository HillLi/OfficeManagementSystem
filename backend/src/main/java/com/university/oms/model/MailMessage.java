package com.university.oms.model;

/**
 * 站内信消息实体
 */
public class MailMessage extends BaseEntity {
    /** 发件人ID */
    private Long senderId;
    private String subject;
    private String content;

    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
