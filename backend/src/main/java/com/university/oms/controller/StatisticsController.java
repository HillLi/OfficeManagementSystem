package com.university.oms.controller;

import com.university.oms.common.ApiResponse;
import com.university.oms.model.DashboardStats;
import com.university.oms.service.StatisticsService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

/** 统计分析控制器，负责统计汇总和CSV导出 */
@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {
    private final StatisticsService service;

    public StatisticsController(StatisticsService service) {
        this.service = service;
    }

    /** 获取统计汇总数据 */
    @GetMapping
    public ApiResponse<DashboardStats> summary() {
        return ApiResponse.ok(service.summary());
    }

    /** 导出统计数据为CSV文件 */
    @GetMapping(value = "/export", produces = "text/csv;charset=UTF-8")
    public ResponseEntity<byte[]> export() {
        byte[] csv = service.exportCsv().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=statistics.csv")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(csv);
    }
}
