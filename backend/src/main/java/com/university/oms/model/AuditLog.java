package com.university.oms.model;

/**
 * 审计日志实体，记录系统操作行为
 */
public class AuditLog extends BaseEntity {
    /** 操作人ID */
    private Long operatorId;
    /** 所属模块 */
    private String module;
    /** 操作动作 */
    private String action;
    /** 关联的业务类型 */
    private String bizType;
    /** 关联的业务ID */
    private Long bizId;
    /** 操作详情 */
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
