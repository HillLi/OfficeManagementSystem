package com.university.oms.model;

public class FlowInstance extends BaseEntity {
    private String bizType;
    private Long bizId;
    private String currentNodeKey;
    private String status;
    private Long starterId;

    public String getBizType() { return bizType; }
    public void setBizType(String bizType) { this.bizType = bizType; }
    public Long getBizId() { return bizId; }
    public void setBizId(Long bizId) { this.bizId = bizId; }
    public String getCurrentNodeKey() { return currentNodeKey; }
    public void setCurrentNodeKey(String currentNodeKey) { this.currentNodeKey = currentNodeKey; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getStarterId() { return starterId; }
    public void setStarterId(Long starterId) { this.starterId = starterId; }
}
