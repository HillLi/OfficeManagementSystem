package com.university.oms.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Meeting extends BaseEntity {
    private String title;
    private Long roomId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long organizerId;
    private Integer expectedCount;
    private String venueType;
    private String meetingType;
    private BigDecimal budget;
    private BigDecimal accommodationFee;
    private BigDecimal mealFee;
    private BigDecimal venueFee;
    private BigDecimal otherFee;
    private String riskReportUrl;
    private String securityPlanUrl;
    private String emergencyPlanUrl;
    private boolean largeActivity;
    private Integer signInCount = 0;
    private String minutes;
    private String status;
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
