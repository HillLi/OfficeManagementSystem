package com.university.oms.dto;

import javax.validation.constraints.*;

/**
 * 通用审批请求参数
 */
public class ApprovalRequest {
    /** 操作人 ID（可选，后端可自动填充） */
    private Long operatorId;
    /** 审批动作：approve（通过）或 reject（驳回） */
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
