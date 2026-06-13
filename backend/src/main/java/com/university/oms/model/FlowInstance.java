package com.university.oms.model;

/**
 * 审批流程实例实体
 */
public class FlowInstance extends BaseEntity {
    /** 关联的业务类型 */
    private String bizType;
    /** 关联的业务ID */
    private Long bizId;
    /** 当前所在流程节点标识 */
    private String currentNodeKey;
    private String status;
    /** 流程发起人ID */
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
