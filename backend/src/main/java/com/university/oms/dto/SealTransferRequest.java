package com.university.oms.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class SealTransferRequest {
    @NotNull(message = "印章不能为空")
    private Long sealId;
    @NotNull(message = "接收人不能为空")
    private Long receiverId;
    @NotNull(message = "监交人不能为空")
    private Long supervisorId;
    @NotBlank(message = "移交材料不能为空")
    private String materialUrl;
    private String remark;

    public Long getSealId() { return sealId; }
    public void setSealId(Long sealId) { this.sealId = sealId; }
    public Long getReceiverId() { return receiverId; }
    public void setReceiverId(Long receiverId) { this.receiverId = receiverId; }
    public Long getSupervisorId() { return supervisorId; }
    public void setSupervisorId(Long supervisorId) { this.supervisorId = supervisorId; }
    public String getMaterialUrl() { return materialUrl; }
    public void setMaterialUrl(String materialUrl) { this.materialUrl = materialUrl; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
