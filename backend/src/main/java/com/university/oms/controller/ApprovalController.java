package com.university.oms.controller;

import com.university.oms.common.ApiResponse;
import com.university.oms.dto.ApprovalRequest;
import com.university.oms.model.ApprovalRecord;
import com.university.oms.service.ApprovalService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/** 审批记录控制器，负责查询审批记录和提交审批操作 */
@RestController
@RequestMapping("/api/approvals")
public class ApprovalController {
    private final ApprovalService service;

    public ApprovalController(ApprovalService service) {
        this.service = service;
    }

    /** 查询审批记录列表，可按业务类型和业务ID筛选 */
    @GetMapping
    public ApiResponse<List<ApprovalRecord>> list(@RequestParam(required = false) String bizType,
                                                  @RequestParam(required = false) Long bizId) {
        return ApiResponse.ok(service.list(bizType, bizId));
    }

    /** 对指定业务发起审批 */
    @PostMapping("/{bizType}/{bizId}")
    public ApiResponse<Object> approve(@PathVariable String bizType, @PathVariable Long bizId,
                                       @Valid @RequestBody ApprovalRequest request) {
        return ApiResponse.ok(service.approve(bizType, bizId, request));
    }
}
