package com.university.oms.dto;

import javax.validation.constraints.NotNull;

public class DocumentDistributionRequest {
    @NotNull(message = "接收人不能为空")
    private Long receiverId;
    @NotNull(message = "接收部门不能为空")
    private Long receiverDeptId;

    public Long getReceiverId() { return receiverId; }
    public void setReceiverId(Long receiverId) { this.receiverId = receiverId; }
    public Long getReceiverDeptId() { return receiverDeptId; }
    public void setReceiverDeptId(Long receiverDeptId) { this.receiverDeptId = receiverDeptId; }
}
