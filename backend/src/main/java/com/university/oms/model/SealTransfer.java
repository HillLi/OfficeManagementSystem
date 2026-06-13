package com.university.oms.model;

import java.time.LocalDateTime;

/**
 * 印章移交记录实体
 */
public class SealTransfer extends BaseEntity {
    private Long sealId;
    /** 移交人ID */
    private Long transferorId;
    /** 接收人ID */
    private Long receiverId;
    /** 监交人ID */
    private Long supervisorId;
    /** 移交材料附件URL */
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
