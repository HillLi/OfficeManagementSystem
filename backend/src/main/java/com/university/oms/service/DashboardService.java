package com.university.oms.service;

import com.university.oms.design.DashboardFacade;
import com.university.oms.model.DashboardStats;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {
    private final DashboardFacade facade;
    private final BusinessAccessService accessService;

    public DashboardService(DashboardFacade facade, BusinessAccessService accessService) {
        this.facade = facade;
        this.accessService = accessService;
    }

    public DashboardStats stats() {
        return facade.aggregate(accessService::canReadBusiness);
    }
}
