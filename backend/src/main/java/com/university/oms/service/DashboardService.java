package com.university.oms.service;

import com.university.oms.design.DashboardFacade;
import com.university.oms.model.DashboardStats;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {
    private final DashboardFacade facade;

    public DashboardService(DashboardFacade facade) {
        this.facade = facade;
    }

    public DashboardStats stats() {
        return facade.aggregate();
    }
}
