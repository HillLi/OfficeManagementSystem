package com.university.oms.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import javax.validation.constraints.*;

public class TravelRequest {
    @NotNull(message = "申请人不能为空")
    private Long applicantId;
    @NotBlank(message = "目的地不能为空")
    @Size(max = 100, message = "目的地不能超过100字")
    private String destination;
    @NotNull(message = "出发日期不能为空")
    private LocalDate startDate;
    @NotNull(message = "返回日期不能为空")
    private LocalDate endDate;
    @NotBlank(message = "出差事由不能为空")
    @Size(max = 500, message = "出差事由不能超过500字")
    private String reason;
    private String staffLevel = "三类";
    private String travelType = "教学科研业务";
    private String transport = "高铁二等座";
    @DecimalMin(value = "0", message = "预算不能为负数")
    private BigDecimal budget = BigDecimal.ZERO;
    private BigDecimal actualExpense = BigDecimal.ZERO;

    public Long getApplicantId() { return applicantId; }
    public void setApplicantId(Long applicantId) { this.applicantId = applicantId; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getStaffLevel() { return staffLevel; }
    public void setStaffLevel(String staffLevel) { this.staffLevel = staffLevel; }
    public String getTravelType() { return travelType; }
    public void setTravelType(String travelType) { this.travelType = travelType; }
    public String getTransport() { return transport; }
    public void setTransport(String transport) { this.transport = transport; }
    public BigDecimal getBudget() { return budget; }
    public void setBudget(BigDecimal budget) { this.budget = budget; }
    public BigDecimal getActualExpense() { return actualExpense; }
    public void setActualExpense(BigDecimal actualExpense) { this.actualExpense = actualExpense; }
}
