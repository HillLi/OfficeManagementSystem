package com.university.oms.controller;

import com.university.oms.common.ApiResponse;
import com.university.oms.dto.ReportRequest;
import com.university.oms.model.Report;
import com.university.oms.service.ReportService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final ReportService service;

    public ReportController(ReportService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<Report>> list() {
        return ApiResponse.ok(service.list());
    }

    @PostMapping
    public ApiResponse<Report> create(@Valid @RequestBody ReportRequest request) {
        return ApiResponse.ok(service.create(request));
    }
}
