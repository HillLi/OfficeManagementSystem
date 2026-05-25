package com.university.oms.model;

import java.time.LocalDateTime;

public class SealTransfer extends BaseEntity {
    private Long sealId;
    private Long transferorId;
    private Long receiverId;
    private Long supervisorId;
    private String materialUrl;
    private String remark;
    private LocalDateTime transferTime;

    public Long getSealId() { return sealId; }
    public void setSealId(Long sealId) { this.sealId = sealId; }
    public Long getTransferorId() { return transferorId; }
    public void setTransferorId(Long transferorId) { this.transferorId = transferorId; }
    public Long getReceiverId() { return receiverId; }
    public void setReceiverId(Long receiverId) { this.receiverId = receiverId; }
    public Long getSupervisorId() { return supervisorId; }
    public void setSupervisorId(Long supervisorId) { this.supervisorId = supervisorId; }
    public String getMaterialUrl() { return materialUrl; }
    public void setMaterialUrl(String materialUrl) { this.materialUrl = materialUrl; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public LocalDateTime getTransferTime() { return transferTime; }
    public void setTransferTime(LocalDateTime transferTime) { this.transferTime = transferTime; }
}
