package com.university.oms.controller;

import com.university.oms.common.ApiResponse;
import com.university.oms.dto.SealApplyRequest;
import com.university.oms.dto.SealTransferRequest;
import com.university.oms.model.Seal;
import com.university.oms.model.SealApplication;
import com.university.oms.model.SealTransfer;
import com.university.oms.service.SealService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/** 印章管理控制器，负责印章使用申请和印章移交 */
@RestController
@RequestMapping("/api/seals")
public class SealController {
    private final SealService service;

    public SealController(SealService service) {
        this.service = service;
    }

    /** 查询印章列表 */
    @GetMapping
    public ApiResponse<List<Seal>> seals() {
        return ApiResponse.ok(service.seals());
    }

    /** 查询印章使用申请列表 */
    @GetMapping("/applications")
    public ApiResponse<List<SealApplication>> applications() {
        return ApiResponse.ok(service.applications());
    }

    /** 创建印章使用申请 */
    @PostMapping("/applications")
    public ApiResponse<SealApplication> apply(@Valid @RequestBody SealApplyRequest request) {
        return ApiResponse.ok(service.apply(request));
    }

    /** 提交印章使用申请 */
    @PostMapping("/applications/{id}/submit")
    public ApiResponse<SealApplication> submit(@PathVariable Long id) {
        return ApiResponse.ok(service.submit(id));
    }

    /** 标记印章已使用 */
    @PostMapping("/applications/{id}/used")
    public ApiResponse<SealApplication> used(@PathVariable Long id, @RequestParam Long keeperId) {
        return ApiResponse.ok(service.markUsed(id, keeperId));
    }

    /** 标记印章已归还 */
    @PostMapping("/applications/{id}/returned")
    public ApiResponse<SealApplication> returned(@PathVariable Long id, @RequestParam Long keeperId) {
        return ApiResponse.ok(service.markReturned(id, keeperId));
    }

    /** 创建印章移交记录 */
    @PostMapping("/transfers")
    public ApiResponse<SealTransfer> transfer(@Valid @RequestBody SealTransferRequest request) {
        return ApiResponse.ok(service.transfer(request));
    }

    /** 查询印章移交记录列表 */
    @GetMapping("/transfers")
    public ApiResponse<List<SealTransfer>> transfers() {
        return ApiResponse.ok(service.transfers());
    }
}
