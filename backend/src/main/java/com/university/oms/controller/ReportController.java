package com.university.oms.controller;

import com.university.oms.common.ApiResponse;
import com.university.oms.dto.ReportReplyRequest;
import com.university.oms.dto.ReportRequest;
import com.university.oms.model.Report;
import com.university.oms.service.ReportService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/** 工作汇报控制器，负责汇报的创建与回复 */
@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final ReportService service;

    public ReportController(ReportService service) {
        this.service = service;
    }

    /** 查询汇报列表 */
    @GetMapping
    public ApiResponse<List<Report>> list() {
        return ApiResponse.ok(service.list());
    }

    /** 创建工作汇报 */
    @PostMapping
    public ApiResponse<Report> create(@Valid @RequestBody ReportRequest request) {
        return ApiResponse.ok(service.create(request));
    }

    /** 回复指定汇报 */
    @PostMapping("/{id}/reply")
    public ApiResponse<Report> reply(@PathVariable Long id, @Valid @RequestBody ReportReplyRequest request) {
        return ApiResponse.ok(service.reply(id, request));
    }
}
