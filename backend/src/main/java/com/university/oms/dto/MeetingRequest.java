package com.university.oms.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class MeetingRequest {
    @NotBlank(message = "会议主题不能为空")
    private String title;
    @NotNull(message = "会议室不能为空")
    private Long roomId;
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;
    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;
    @NotNull(message = "组织者不能为空")
    private Long organizerId;
    private Integer expectedCount = 1;
    private String venueType = "室内";
    private String meetingType = "国内管理会议";
    private BigDecimal budget = BigDecimal.ZERO;
    private String riskReportUrl;
    private String securityPlanUrl;
    private String emergencyPlanUrl;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public Long getOrganizerId() { return organizerId; }
    public void setOrganizerId(Long organizerId) { this.organizerId = organizerId; }
    public Integer getExpectedCount() { return expectedCount; }
    public void setExpectedCount(Integer expectedCount) { this.expectedCount = expectedCount; }
    public String getVenueType() { return venueType; }
    public void setVenueType(String venueType) { this.venueType = venueType; }
    public String getMeetingType() { return meetingType; }
    public void setMeetingType(String meetingType) { this.meetingType = meetingType; }
    public BigDecimal getBudget() { return budget; }
    public void setBudget(BigDecimal budget) { this.budget = budget; }
    public String getRiskReportUrl() { return riskReportUrl; }
    public void setRiskReportUrl(String riskReportUrl) { this.riskReportUrl = riskReportUrl; }
    public String getSecurityPlanUrl() { return securityPlanUrl; }
    public void setSecurityPlanUrl(String securityPlanUrl) { this.securityPlanUrl = securityPlanUrl; }
    public String getEmergencyPlanUrl() { return emergencyPlanUrl; }
    public void setEmergencyPlanUrl(String emergencyPlanUrl) { this.emergencyPlanUrl = emergencyPlanUrl; }
}
