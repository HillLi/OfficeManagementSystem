package com.university.oms.service;

import com.university.oms.design.DashboardFacade;
import com.university.oms.model.DashboardStats;
import org.springframework.stereotype.Service;

/**
 * 统计服务，提供数据汇总与CSV导出功能
 */
@Service
public class StatisticsService {
    private final DashboardFacade facade;

    public StatisticsService(DashboardFacade facade) {
        this.facade = facade;
    }

    /** 获取统计摘要数据（不过滤权限，展示全量汇总） */
    public DashboardStats summary() {
        return facade.aggregate();
    }

    /** 将统计数据导出为CSV格式 */
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
