package com.university.oms.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class SealApplyRequest {
    @NotNull(message = "印章不能为空")
    private Long sealId;
    @NotNull(message = "申请人不能为空")
    private Long applicantId;
    @NotBlank(message = "用印用途不能为空")
    private String purpose;
    private String materialUrl;
    private Integer copies = 1;
    private boolean takeOut;
    private String matterLevel = "常规事项";
    private String takeOutReason;
    private String takeOutLocation;
    private Long supervisorId;
    private LocalDateTime expectedReturnTime;

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
    public LocalDateTime getExpectedReturnTime() { return expectedReturnTime; }
    public void setExpectedReturnTime(LocalDateTime expectedReturnTime) { this.expectedReturnTime = expectedReturnTime; }
}
