package com.university.oms.controller;

import com.university.oms.common.ApiResponse;
import com.university.oms.dto.ApprovalRequest;
import com.university.oms.model.ApprovalRecord;
import com.university.oms.service.ApprovalService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/approvals")
public class ApprovalController {
    private final ApprovalService service;

    public ApprovalController(ApprovalService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<ApprovalRecord>> list(@RequestParam(required = false) String bizType,
                                                  @RequestParam(required = false) Long bizId) {
        return ApiResponse.ok(service.list(bizType, bizId));
    }

    @PostMapping("/{bizType}/{bizId}")
    public ApiResponse<Object> approve(@PathVariable String bizType, @PathVariable Long bizId,
                                       @Valid @RequestBody ApprovalRequest request) {
        return ApiResponse.ok(service.approve(bizType, bizId, request));
    }
}
