package com.university.oms.controller;

import com.university.oms.common.ApiResponse;
import com.university.oms.dto.TravelReimburseRequest;
import com.university.oms.dto.TravelRequest;
import com.university.oms.model.Travel;
import com.university.oms.service.TravelService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/** 出差管理控制器，负责出差申请和差旅报销 */
@RestController
@RequestMapping("/api/travels")
public class TravelController {
    private final TravelService service;

    public TravelController(TravelService service) {
        this.service = service;
    }

    /** 查询出差记录列表 */
    @GetMapping
    public ApiResponse<List<Travel>> list() {
        return ApiResponse.ok(service.list());
    }

    /** 创建出差申请 */
    @PostMapping
    public ApiResponse<Travel> create(@Valid @RequestBody TravelRequest request) {
        return ApiResponse.ok(service.create(request));
    }

    /** 提交差旅报销 */
    @PostMapping("/{id}/reimburse")
    public ApiResponse<Travel> reimburse(@PathVariable Long id, @Valid @RequestBody TravelReimburseRequest request) {
        return ApiResponse.ok(service.reimburse(id, request));
    }
}
