package com.university.oms.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 印章移交请求参数
 */
public class SealTransferRequest {
    @NotNull(message = "印章不能为空")
    private Long sealId;
    /** 接收人 ID */
    @NotNull(message = "接收人不能为空")
    private Long receiverId;
    /** 监交人 ID */
    @NotNull(message = "监交人不能为空")
    private Long supervisorId;
    /** 移交材料文件地址 */
    @NotBlank(message = "移交材料不能为空")
    @Size(max = 500, message = "移交材料不能超过500字")
    private String materialUrl;
    @Size(max = 500, message = "备注不能超过500字")
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
