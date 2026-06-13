package com.university.oms.model;

/**
 * 系统通知实体
 */
public class Notification extends BaseEntity {
    /** 接收人ID */
    private Long receiverId;
    private String title;
    private String content;
    /** 是否已读 */
    private boolean readStatus;
    /** 关联的业务类型 */
    private String bizType;
    /** 关联的业务ID */
    private Long bizId;

    public Long getReceiverId() { return receiverId; }
    public void setReceiverId(Long receiverId) { this.receiverId = receiverId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public boolean isReadStatus() { return readStatus; }
    public void setReadStatus(boolean readStatus) { this.readStatus = readStatus; }
    public String getBizType() { return bizType; }
    public void setBizType(String bizType) { this.bizType = bizType; }
    public Long getBizId() { return bizId; }
    public void setBizId(Long bizId) { this.bizId = bizId; }
}
