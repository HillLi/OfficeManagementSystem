package com.university.oms.dto;

import javax.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * 用印申请请求参数
 */
public class SealApplyRequest {
    /** 申请使用的印章 ID */
    @NotNull(message = "印章不能为空")
    private Long sealId;
    @NotNull(message = "申请人不能为空")
    private Long applicantId;
    @NotBlank(message = "用印用途不能为空")
    @Size(max = 500, message = "用印用途不能超过500字")
    private String purpose;
    /** 申请材料文件地址 */
    private String materialUrl;
    @Min(value = 1, message = "用印份数至少为1")
    private Integer copies = 1;
    /** 是否外带用印 */
    private boolean takeOut;
    /** 事项等级，默认"常规事项" */
    private String matterLevel = "常规事项";
    /** 外带原因 */
    private String takeOutReason;
    /** 外带地点 */
    private String takeOutLocation;
    /** 监交人 ID */
    private Long supervisorId;
    /** 预计归还时间（外带时使用） */
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
