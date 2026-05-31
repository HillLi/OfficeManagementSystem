package com.university.oms.model;

import java.time.LocalDateTime;

public class SealApplication extends BaseEntity {
    private Long sealId;
    private Long applicantId;
    private String purpose;
    private String materialUrl;
    private Integer copies;
    private boolean takeOut;
    private String matterLevel;
    private String takeOutReason;
    private String takeOutLocation;
    private Long supervisorId;
    private String status;
    private LocalDateTime useTime;
    private LocalDateTime returnTime;
    private LocalDateTime returnDeadline;
    private LocalDateTime retentionUntil;
    private String sealName;
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
