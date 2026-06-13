package com.university.oms.model;

import java.time.LocalDateTime;

/**
 * 用印申请实体
 */
public class SealApplication extends BaseEntity {
    /** 印章ID */
    private Long sealId;
    /** 申请人ID */
    private Long applicantId;
    /** 用印事由 */
    private String purpose;
    /** 用印材料附件URL */
    private String materialUrl;
    /** 盖章份数 */
    private Integer copies;
    /** 是否外带用印 */
    private boolean takeOut;
    /** 事项等级 */
    private String matterLevel;
    /** 外带原因 */
    private String takeOutReason;
    /** 外带地点 */
    private String takeOutLocation;
    /** 监管人ID */
    private Long supervisorId;
    private String status;
    /** 用印时间 */
    private LocalDateTime useTime;
    /** 归还时间 */
    private LocalDateTime returnTime;
    /** 归还截止时间 */
    private LocalDateTime returnDeadline;
    /** 留存截止时间（印章需留存时的期限） */
    private LocalDateTime retentionUntil;
    /** 印章名称（冗余字段，方便展示） */
    private String sealName;
    /** 附件材料数量 */
    private int materialCount;

    public Long getSealId() {
        return sealId;
    }

    public void setSealId(Long sealId) {
        this.sealId = sealId;
    }

    public Long getApplicantId() {
        return applicantId;
    }

    public void setApplicantId(Long applicantId) {
        this.applicantId = applicantId;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getMaterialUrl() {
        return materialUrl;
    }

    public void setMaterialUrl(String materialUrl) {
        this.materialUrl = materialUrl;
    }

    public Integer getCopies() {
        return copies;
    }

    public void setCopies(Integer copies) {
        this.copies = copies;
    }

    public boolean isTakeOut() {
        return takeOut;
    }

    public void setTakeOut(boolean takeOut) {
        this.takeOut = takeOut;
    }

    public String getMatterLevel() {
        return matterLevel;
    }

    public void setMatterLevel(String matterLevel) {
        this.matterLevel = matterLevel;
    }

    public String getTakeOutReason() { return takeOutReason; }
    public void setTakeOutReason(String takeOutReason) { this.takeOutReason = takeOutReason; }
    public String getTakeOutLocation() { return takeOutLocation; }
    public void setTakeOutLocation(String takeOutLocation) { this.takeOutLocation = takeOutLocation; }
    public Long getSupervisorId() { return supervisorId; }
    public void setSupervisorId(Long supervisorId) { this.supervisorId = supervisorId; }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getUseTime() {
        return useTime;
    }

    public void setUseTime(LocalDateTime useTime) {
        this.useTime = useTime;
    }

    public LocalDateTime getReturnTime() {
        return returnTime;
    }

    public void setReturnTime(LocalDateTime returnTime) {
        this.returnTime = returnTime;
    }

    public LocalDateTime getReturnDeadline() {
        return returnDeadline;
    }

    public void setReturnDeadline(LocalDateTime returnDeadline) {
        this.returnDeadline = returnDeadline;
    }

    public LocalDateTime getRetentionUntil() {
        return retentionUntil;
    }

    public void setRetentionUntil(LocalDateTime retentionUntil) {
        this.retentionUntil = retentionUntil;
    }

    public String getSealName() {
        return sealName;
    }

    public void setSealName(String sealName) {
        this.sealName = sealName;
    }

    public int getMaterialCount() {
        return materialCount;
    }

    public void setMaterialCount(int materialCount) {
        this.materialCount = materialCount;
    }
}
