package com.university.oms.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 出差申请实体
 */
public class Travel extends BaseEntity {
    /** 申请人ID */
    private Long applicantId;
    private String destination;
    private LocalDate startDate;
    private LocalDate endDate;
    /** 出差事由 */
    private String reason;
    /** 人员级别（用于确定差旅标准） */
    private String staffLevel;
    /** 出差类型 */
    private String travelType;
    /** 交通方式 */
    private String transport;
    private BigDecimal budget;
    /** 实际报销金额 */
    private BigDecimal actualExpense;
    /** 报销凭证附件URL */
    private String receiptUrl;
    /** 超标原因 */
    private String overLimitReason;
    /** 是否已提交报销 */
    private boolean reimbursementSubmitted;
    /** 差旅标准校验结果 */
    private TravelCheckResult checkResult;
    private String status;

    public Long getApplicantId() {
        return applicantId;
    }

    public void setApplicantId(Long applicantId) {
        this.applicantId = applicantId;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStaffLevel() {
        return staffLevel;
    }

    public void setStaffLevel(String staffLevel) {
        this.staffLevel = staffLevel;
    }

    public String getTravelType() {
        return travelType;
    }

    public void setTravelType(String travelType) {
        this.travelType = travelType;
    }

    public String getTransport() {
        return transport;
    }

    public void setTransport(String transport) {
        this.transport = transport;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }

    public BigDecimal getActualExpense() {
        return actualExpense;
    }

    public void setActualExpense(BigDecimal actualExpense) {
        this.actualExpense = actualExpense;
    }

    public String getReceiptUrl() { return receiptUrl; }
    public void setReceiptUrl(String receiptUrl) { this.receiptUrl = receiptUrl; }
    public String getOverLimitReason() { return overLimitReason; }
    public void setOverLimitReason(String overLimitReason) { this.overLimitReason = overLimitReason; }
    public boolean isReimbursementSubmitted() { return reimbursementSubmitted; }
    public void setReimbursementSubmitted(boolean reimbursementSubmitted) { this.reimbursementSubmitted = reimbursementSubmitted; }

    public TravelCheckResult getCheckResult() {
        return checkResult;
    }

    public void setCheckResult(TravelCheckResult checkResult) {
        this.checkResult = checkResult;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
