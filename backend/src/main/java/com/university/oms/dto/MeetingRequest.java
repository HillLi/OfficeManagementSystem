package com.university.oms.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import javax.validation.constraints.*;

public class MeetingRequest {
    @NotBlank(message = "会议主题不能为空")
    @Size(max = 200, message = "会议主题不能超过200字")
    private String title;
    @NotNull(message = "会议室不能为空")
    private Long roomId;
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;
    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;
    @NotNull(message = "组织者不能为空")
    private Long organizerId;
    @Min(value = 1, message = "参会人数至少为1")
    private Integer expectedCount = 1;
    private String venueType = "室内";
    private String meetingType = "国内管理会议";
    @DecimalMin(value = "0", message = "预算不能为负数")
    private BigDecimal budget = BigDecimal.ZERO;
    @DecimalMin(value = "0", message = "费用不能为负数")
    private BigDecimal accommodationFee;
    @DecimalMin(value = "0", message = "费用不能为负数")
    private BigDecimal mealFee;
    @DecimalMin(value = "0", message = "费用不能为负数")
    private BigDecimal venueFee;
    @DecimalMin(value = "0", message = "费用不能为负数")
    private BigDecimal otherFee;
    private String riskReportUrl;
    private String securityPlanUrl;
    private String emergencyPlanUrl;
    @NotEmpty(message = "参会人员不能为空")
    private List<Long> participants;
    private Long recorderId;

    public List<Long> getParticipants() { return participants; }
    public void setParticipants(List<Long> participants) { this.participants = participants; }
    public Long getRecorderId() { return recorderId; }
    public void setRecorderId(Long recorderId) { this.recorderId = recorderId; }

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
    public BigDecimal getAccommodationFee() { return accommodationFee; }
    public void setAccommodationFee(BigDecimal accommodationFee) { this.accommodationFee = accommodationFee; }
    public BigDecimal getMealFee() { return mealFee; }
    public void setMealFee(BigDecimal mealFee) { this.mealFee = mealFee; }
    public BigDecimal getVenueFee() { return venueFee; }
    public void setVenueFee(BigDecimal venueFee) { this.venueFee = venueFee; }
    public BigDecimal getOtherFee() { return otherFee; }
    public void setOtherFee(BigDecimal otherFee) { this.otherFee = otherFee; }
    public String getRiskReportUrl() { return riskReportUrl; }
    public void setRiskReportUrl(String riskReportUrl) { this.riskReportUrl = riskReportUrl; }
    public String getSecurityPlanUrl() { return securityPlanUrl; }
    public void setSecurityPlanUrl(String securityPlanUrl) { this.securityPlanUrl = securityPlanUrl; }
    public String getEmergencyPlanUrl() { return emergencyPlanUrl; }
    public void setEmergencyPlanUrl(String emergencyPlanUrl) { this.emergencyPlanUrl = emergencyPlanUrl; }
}
