package com.university.oms.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 会议实体
 */
public class Meeting extends BaseEntity {
    private String title;
    /** 会议室ID */
    private Long roomId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    /** 组织者ID */
    private Long organizerId;
    /** 预计参会人数 */
    private Integer expectedCount;
    /** 会场类型 */
    private String venueType;
    /** 会议类型 */
    private String meetingType;
    /** 会议预算 */
    private BigDecimal budget;
    /** 住宿费用 */
    private BigDecimal accommodationFee;
    /** 餐饮费用 */
    private BigDecimal mealFee;
    /** 场地费用 */
    private BigDecimal venueFee;
    /** 其他费用 */
    private BigDecimal otherFee;
    /** 风险评估报告附件URL */
    private String riskReportUrl;
    /** 安全工作方案附件URL */
    private String securityPlanUrl;
    /** 应急预案附件URL */
    private String emergencyPlanUrl;
    /** 是否为大型活动 */
    private boolean largeActivity;
    /** 签到人数 */
    private Integer signInCount = 0;
    /** 会议纪要内容 */
    private String minutes;
    private String status;
    /** 会议记录人ID */
    private Long recorderId;

    public Long getRecorderId() { return recorderId; }
    public void setRecorderId(Long recorderId) { this.recorderId = recorderId; }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Long getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(Long organizerId) {
        this.organizerId = organizerId;
    }

    public Integer getExpectedCount() {
        return expectedCount;
    }

    public void setExpectedCount(Integer expectedCount) {
        this.expectedCount = expectedCount;
    }

    public String getVenueType() {
        return venueType;
    }

    public void setVenueType(String venueType) {
        this.venueType = venueType;
    }

    public String getMeetingType() {
        return meetingType;
    }

    public void setMeetingType(String meetingType) {
        this.meetingType = meetingType;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }

    public BigDecimal getAccommodationFee() { return accommodationFee; }
    public void setAccommodationFee(BigDecimal accommodationFee) { this.accommodationFee = accommodationFee; }
    public BigDecimal getMealFee() { return mealFee; }
    public void setMealFee(BigDecimal mealFee) { this.mealFee = mealFee; }
    public BigDecimal getVenueFee() { return venueFee; }
    public void setVenueFee(BigDecimal venueFee) { this.venueFee = venueFee; }
    public BigDecimal getOtherFee() { return otherFee; }
    public void setOtherFee(BigDecimal otherFee) { this.otherFee = otherFee; }

    public String getRiskReportUrl() {
        return riskReportUrl;
    }

    public void setRiskReportUrl(String riskReportUrl) {
        this.riskReportUrl = riskReportUrl;
    }

    public String getSecurityPlanUrl() {
        return securityPlanUrl;
    }

    public void setSecurityPlanUrl(String securityPlanUrl) {
        this.securityPlanUrl = securityPlanUrl;
    }

    public String getEmergencyPlanUrl() {
        return emergencyPlanUrl;
    }

    public void setEmergencyPlanUrl(String emergencyPlanUrl) {
        this.emergencyPlanUrl = emergencyPlanUrl;
    }

    public boolean isLargeActivity() {
        return largeActivity;
    }

    public void setLargeActivity(boolean largeActivity) {
        this.largeActivity = largeActivity;
    }

    public Integer getSignInCount() {
        return signInCount;
    }

    public void setSignInCount(Integer signInCount) {
        this.signInCount = signInCount;
    }

    public String getMinutes() {
        return minutes;
    }

    public void setMinutes(String minutes) {
        this.minutes = minutes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
