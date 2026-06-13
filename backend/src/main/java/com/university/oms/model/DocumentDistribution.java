package com.university.oms.model;

import java.time.LocalDateTime;

/**
 * 公文分发记录实体
 */
public class DocumentDistribution extends BaseEntity {
    private Long documentId;
    /** 接收人ID */
    private Long receiverId;
    /** 接收人部门ID */
    private Long receiverDeptId;
    private String status;
    /** 分发时间 */
    private LocalDateTime distributedAt;
    /** 接收时间 */
    private LocalDateTime receivedAt;
    /** 催办时间 */
    private LocalDateTime remindedAt;

    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }
    public Long getReceiverId() { return receiverId; }
    public void setReceiverId(Long receiverId) { this.receiverId = receiverId; }
    public Long getReceiverDeptId() { return receiverDeptId; }
    public void setReceiverDeptId(Long receiverDeptId) { this.receiverDeptId = receiverDeptId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getDistributedAt() { return distributedAt; }
    public void setDistributedAt(LocalDateTime distributedAt) { this.distributedAt = distributedAt; }
    public LocalDateTime getReceivedAt() { return receivedAt; }
    public void setReceivedAt(LocalDateTime receivedAt) { this.receivedAt = receivedAt; }
    public LocalDateTime getRemindedAt() { return remindedAt; }
    public void setRemindedAt(LocalDateTime remindedAt) { this.remindedAt = remindedAt; }
}
