package com.university.oms.controller;

import com.university.oms.common.ApiResponse;
import com.university.oms.model.DashboardStats;
import com.university.oms.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 仪表盘控制器，负责返回首页统计数据 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    /** 获取仪表盘统计数据 */
    @GetMapping
    public ApiResponse<DashboardStats> stats() {
        return ApiResponse.ok(service.stats());
    }
}
