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

@RestController
@RequestMapping("/api/seals")
public class SealController {
    private final SealService service;

    public SealController(SealService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<Seal>> seals() {
        return ApiResponse.ok(service.seals());
    }

    @GetMapping("/applications")
    public ApiResponse<List<SealApplication>> applications() {
        return ApiResponse.ok(service.applications());
    }

    @PostMapping("/applications")
    public ApiResponse<SealApplication> apply(@Valid @RequestBody SealApplyRequest request) {
        return ApiResponse.ok(service.apply(request));
    }

    @PostMapping("/applications/{id}/submit")
    public ApiResponse<SealApplication> submit(@PathVariable Long id) {
        return ApiResponse.ok(service.submit(id));
    }

    @PostMapping("/applications/{id}/used")
    public ApiResponse<SealApplication> used(@PathVariable Long id, @RequestParam Long keeperId) {
        return ApiResponse.ok(service.markUsed(id, keeperId));
    }

    @PostMapping("/applications/{id}/returned")
    public ApiResponse<SealApplication> returned(@PathVariable Long id, @RequestParam Long keeperId) {
        return ApiResponse.ok(service.markReturned(id, keeperId));
    }

    @PostMapping("/transfers")
    public ApiResponse<SealTransfer> transfer(@Valid @RequestBody SealTransferRequest request) {
        return ApiResponse.ok(service.transfer(request));
    }

    @GetMapping("/transfers")
    public ApiResponse<List<SealTransfer>> transfers() {
        return ApiResponse.ok(service.transfers());
    }
}
