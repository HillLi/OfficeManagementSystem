package com.university.oms.controller;

import com.university.oms.common.ApiResponse;
import com.university.oms.dto.AttachmentRequest;
import com.university.oms.model.*;
import com.university.oms.service.WorkflowService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/workflow")
public class WorkflowController {
    private final WorkflowService service;

    public WorkflowController(WorkflowService service) {
        this.service = service;
    }

    @PostMapping("/attachments")
    public ApiResponse<Attachment> addAttachment(@Valid @RequestBody AttachmentRequest request) {
        return ApiResponse.ok(service.addAttachment(request));
    }

    @GetMapping("/attachments")
    public ApiResponse<List<Attachment>> attachments(@RequestParam(required = false) String bizType,
                                                     @RequestParam(required = false) Long bizId) {
        return ApiResponse.ok(service.attachments(bizType, bizId));
    }

    @GetMapping("/audit-logs")
    public ApiResponse<List<AuditLog>> auditLogs(@RequestParam(required = false) String bizType,
                                                 @RequestParam(required = false) Long bizId) {
        return ApiResponse.ok(service.auditLogs(bizType, bizId));
    }

    @GetMapping("/notifications")
    public ApiResponse<List<Notification>> notifications(@RequestParam(defaultValue = "false") boolean unreadOnly) {
        return ApiResponse.ok(service.notifications(unreadOnly));
    }

    @PostMapping("/notifications/{id}/read")
    public ApiResponse<Notification> markRead(@PathVariable Long id) {
        return ApiResponse.ok(service.markRead(id));
    }

    @GetMapping("/instances")
    public ApiResponse<List<FlowInstance>> instances() {
        return ApiResponse.ok(service.flowInstances());
    }

    @GetMapping("/tasks")
    public ApiResponse<List<FlowTask>> tasks(@RequestParam(defaultValue = "true") boolean onlyMine) {
        return ApiResponse.ok(service.tasks(onlyMine));
    }
}
