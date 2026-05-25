package com.university.oms.service;

import com.university.oms.model.DashboardStats;
import org.springframework.stereotype.Service;

@Service
public class StatisticsService {
    private final DashboardService dashboardService;

    public StatisticsService(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    public DashboardStats summary() {
        return dashboardService.stats();
    }

    public String exportCsv() {
        DashboardStats stats = summary();
        StringBuilder csv = new StringBuilder();
        csv.append("module,count\n");
        csv.append("document,").append(stats.getDocumentCount()).append('\n');
        csv.append("pending_document,").append(stats.getPendingDocumentCount()).append('\n');
        csv.append("seal,").append(stats.getSealApplyCount()).append('\n');
        csv.append("meeting,").append(stats.getMeetingCount()).append('\n');
        csv.append("large_activity,").append(stats.getLargeActivityCount()).append('\n');
        csv.append("travel,").append(stats.getTravelCount()).append('\n');
        csv.append("report,").append(stats.getReportCount()).append('\n');
        csv.append("travel_budget,").append(stats.getTravelBudgetTotal()).append('\n');
        return csv.toString();
    }
}
