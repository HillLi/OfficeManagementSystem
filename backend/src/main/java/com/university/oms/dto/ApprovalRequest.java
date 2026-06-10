package com.university.oms.dto;

import javax.validation.constraints.*;

public class ApprovalRequest {
    private Long operatorId;
    @NotBlank(message = "审批动作不能为空")
    @Pattern(regexp = "approve|reject", message = "审批动作必须是approve或reject")
    private String action = "approve";
    @Size(max = 500, message = "审批意见不能超过500字")
    private String opinion = "同意";

    public Long getOperatorId() { return operatorId; }
    public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getOpinion() { return opinion; }
    public void setOpinion(String opinion) { this.opinion = opinion; }
}
