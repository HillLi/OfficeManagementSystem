package com.university.oms.controller;

import com.university.oms.common.ApiResponse;
import com.university.oms.model.DashboardStats;
import com.university.oms.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<DashboardStats> stats() {
        return ApiResponse.ok(service.stats());
    }
}
