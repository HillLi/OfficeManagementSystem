package com.university.oms.controller;

import com.university.oms.common.ApiResponse;
import com.university.oms.dto.AttachmentDeleteRequest;
import com.university.oms.dto.AttachmentRequest;
import com.university.oms.dto.AttachmentUpdateRequest;
import com.university.oms.dto.WorkflowGuideResponse;
import com.university.oms.model.*;
import com.university.oms.service.WorkflowGuideService;
import com.university.oms.service.WorkflowService;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/workflow")
public class WorkflowController {
    private final WorkflowService service;
    private final WorkflowGuideService guideService;

    public WorkflowController(WorkflowService service, WorkflowGuideService guideService) {
        this.service = service;
        this.guideService = guideService;
    }

    @PostMapping("/attachments")
    public ApiResponse<Attachment> addAttachment(@Valid @RequestBody AttachmentRequest request) {
        return ApiResponse.ok(service.addAttachment(request));
    }

    @GetMapping("/attachments")
    public ApiResponse<List<Attachment>> attachments(@RequestParam(required = false) String bizType,
                                                     @RequestParam(required = false) Long bizId,
                                                     @RequestParam(defaultValue = "false") boolean includeDeleted) {
        return ApiResponse.ok(service.attachments(bizType, bizId, includeDeleted));
    }

    @PostMapping(value = "/attachments/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Attachment> uploadAttachment(@RequestParam String bizType,
                                                    @RequestParam Long bizId,
                                                    @RequestParam(required = false) String secrecyLevel,
                                                    @RequestPart("file") MultipartFile file) {
        return ApiResponse.ok(service.uploadAttachment(bizType, bizId, secrecyLevel, file));
    }

    @PutMapping("/attachments/{id}")
    public ApiResponse<Attachment> updateAttachment(@PathVariable Long id,
                                                    @Valid @RequestBody AttachmentUpdateRequest request) {
        return ApiResponse.ok(service.updateAttachment(id, request));
    }

    @DeleteMapping("/attachments/{id}")
    public ApiResponse<Attachment> deleteAttachment(@PathVariable Long id,
                                                    @Valid @RequestBody AttachmentDeleteRequest request) {
        return ApiResponse.ok(service.deleteAttachment(id, request));
    }

    @GetMapping("/attachments/{id}/download")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable Long id) {
        Attachment attachment = service.attachment(id);
        Resource resource = service.downloadAttachment(id);
        MediaType contentType = MediaType.APPLICATION_OCTET_STREAM;
        if (attachment.getContentType() != null) {
            try {
                contentType = MediaType.parseMediaType(attachment.getContentType());
            } catch (IllegalArgumentException ignored) {
                contentType = MediaType.APPLICATION_OCTET_STREAM;
            }
        }
        String filename = attachment.getOriginalName() == null ? attachment.getFileName() : attachment.getOriginalName();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(filename, StandardCharsets.UTF_8).build().toString())
                .contentType(contentType)
                .body(resource);
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

    @GetMapping("/guide")
    public ApiResponse<WorkflowGuideResponse> guide(@RequestParam String bizType,
                                                     @RequestParam Long bizId) {
        return ApiResponse.ok(guideService.guide(bizType, bizId));
    }
}
