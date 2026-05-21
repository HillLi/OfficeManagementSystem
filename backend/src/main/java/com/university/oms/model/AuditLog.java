package com.university.oms.model;

public class AuditLog extends BaseEntity {
    private Long operatorId;
    private String module;
    private String action;
    private String bizType;
    private Long bizId;
    private String detail;

    public Long getOperatorId() { return operatorId; }
    public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }
    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getBizType() { return bizType; }
    public void setBizType(String bizType) { this.bizType = bizType; }
    public Long getBizId() { return bizId; }
    public void setBizId(Long bizId) { this.bizId = bizId; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
}
