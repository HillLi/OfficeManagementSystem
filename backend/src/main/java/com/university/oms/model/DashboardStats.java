package com.university.oms.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 仪表盘统计数据，用于首页数据概览展示
 */
public class DashboardStats {
    private long documentCount;
    /** 待处理公文数量 */
    private long pendingDocumentCount;
    /** 印章申请数量 */
    private long sealApplyCount;
    private long meetingCount;
    private long travelCount;
    private long reportCount;
    /** 大型活动数量 */
    private long largeActivityCount;
    /** 差旅预算总额 */
    private BigDecimal travelBudgetTotal = BigDecimal.ZERO;
    /** 公文状态分布统计 */
    private Map<String, Long> documentStatusDistribution = new LinkedHashMap<>();
    /** 月度业务数量统计 */
    private Map<String, Long> monthlyBusinessCounts = new LinkedHashMap<>();
    /** 月度日程安排项 */
    private List<DashboardScheduleItem> monthlyScheduleItems = new ArrayList<>();

    public long getDocumentCount() {
        return documentCount;
    }

    public void setDocumentCount(long documentCount) {
        this.documentCount = documentCount;
    }

    public long getPendingDocumentCount() {
        return pendingDocumentCount;
    }

    public void setPendingDocumentCount(long pendingDocumentCount) {
        this.pendingDocumentCount = pendingDocumentCount;
    }

    public long getSealApplyCount() {
        return sealApplyCount;
    }

    public void setSealApplyCount(long sealApplyCount) {
        this.sealApplyCount = sealApplyCount;
    }

    public long getMeetingCount() {
        return meetingCount;
    }

    public void setMeetingCount(long meetingCount) {
        this.meetingCount = meetingCount;
    }

    public long getTravelCount() {
        return travelCount;
    }

    public void setTravelCount(long travelCount) {
        this.travelCount = travelCount;
    }

    public long getReportCount() {
        return reportCount;
    }

    public void setReportCount(long reportCount) {
        this.reportCount = reportCount;
    }

    public long getLargeActivityCount() {
        return largeActivityCount;
    }

    public void setLargeActivityCount(long largeActivityCount) {
        this.largeActivityCount = largeActivityCount;
    }

    public BigDecimal getTravelBudgetTotal() {
        return travelBudgetTotal;
    }

    public void setTravelBudgetTotal(BigDecimal travelBudgetTotal) {
        this.travelBudgetTotal = travelBudgetTotal;
    }

    public Map<String, Long> getDocumentStatusDistribution() {
        return documentStatusDistribution;
    }

    public void setDocumentStatusDistribution(Map<String, Long> documentStatusDistribution) {
        this.documentStatusDistribution = documentStatusDistribution;
    }

    public Map<String, Long> getMonthlyBusinessCounts() {
        return monthlyBusinessCounts;
    }

    public void setMonthlyBusinessCounts(Map<String, Long> monthlyBusinessCounts) {
        this.monthlyBusinessCounts = monthlyBusinessCounts;
    }

    public List<DashboardScheduleItem> getMonthlyScheduleItems() {
        return monthlyScheduleItems;
    }

    public void setMonthlyScheduleItems(List<DashboardScheduleItem> monthlyScheduleItems) {
        this.monthlyScheduleItems = monthlyScheduleItems == null ? new ArrayList<>() : monthlyScheduleItems;
    }
}
