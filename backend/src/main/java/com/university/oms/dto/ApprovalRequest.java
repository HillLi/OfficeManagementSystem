package com.university.oms.dto;

import javax.validation.constraints.NotNull;

public class ApprovalRequest {
    @NotNull(message = "操作人不能为空")
    private Long operatorId;
    private String action = "approve";
    private String opinion = "同意";

    public Long getOperatorId() { return operatorId; }
    public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getOpinion() { return opinion; }
    public void setOpinion(String opinion) { this.opinion = opinion; }
}
