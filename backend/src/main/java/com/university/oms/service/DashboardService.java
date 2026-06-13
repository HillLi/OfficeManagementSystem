package com.university.oms.service;

import com.university.oms.design.DashboardFacade;
import com.university.oms.model.DashboardStats;
import org.springframework.stereotype.Service;

/**
 * 仪表盘服务，聚合各业务模块统计数据
 */
@Service
public class DashboardService {
    private final DashboardFacade facade;
    private final BusinessAccessService accessService;

    public DashboardService(DashboardFacade facade, BusinessAccessService accessService) {
        this.facade = facade;
        this.accessService = accessService;
    }

    /** 获取当前用户可见的仪表盘统计数据 */
    public DashboardStats stats() {
        return facade.aggregate(accessService::canReadBusiness);
    }
}
