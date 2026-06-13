package com.university.oms.model;

/**
 * 审批记录实体，记录各类业务单据的审批操作历史
 */
public class ApprovalRecord extends BaseEntity {
    /** 业务类型（如：document、seal_application） */
    private String bizType;
    /** 业务ID */
    private Long bizId;
    /** 操作人ID */
    private Long operatorId;
    /** 审批动作（如：approve、reject） */
    private String action;
    /** 审批意见 */
    private String opinion;

    public String getBizType() {
        return bizType;
    }

    public void setBizType(String bizType) {
        this.bizType = bizType;
    }

    public Long getBizId() {
        return bizId;
    }

    public void setBizId(Long bizId) {
        this.bizId = bizId;
    }

    public Long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getOpinion() {
        return opinion;
    }

    public void setOpinion(String opinion) {
        this.opinion = opinion;
    }
}
