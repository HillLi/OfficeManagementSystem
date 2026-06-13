package com.university.oms.controller;

import com.university.oms.common.ApiResponse;
import com.university.oms.dto.AiDraftRequest;
import com.university.oms.dto.DocumentDistributionRequest;
import com.university.oms.dto.DocumentRequest;
import com.university.oms.model.AiReviewResult;
import com.university.oms.model.Document;
import com.university.oms.model.DocumentDistribution;
import com.university.oms.service.DocumentService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/** 公文管理控制器，负责公文的创建、流转、分发和AI辅助审阅 */
@RestController
@RequestMapping("/api/documents")
public class DocumentController {
    private final DocumentService service;

    public DocumentController(DocumentService service) {
        this.service = service;
    }

    /** 查询公文列表 */
    @GetMapping
    public ApiResponse<List<Document>> list() {
        return ApiResponse.ok(service.list());
    }

    /** 创建公文 */
    @PostMapping
    public ApiResponse<Document> create(@Valid @RequestBody DocumentRequest request) {
        return ApiResponse.ok(service.create(request));
    }

    /** 更新公文 */
    @PutMapping("/{id}")
    public ApiResponse<Document> update(@PathVariable Long id, @Valid @RequestBody DocumentRequest request) {
        return ApiResponse.ok(service.update(id, request));
    }

    /** 提交公文 */
    @PostMapping("/{id}/submit")
    public ApiResponse<Document> submit(@PathVariable Long id) {
        return ApiResponse.ok(service.submit(id));
    }

    /** 归档公文 */
    @PostMapping("/{id}/archive")
    public ApiResponse<Document> archive(@PathVariable Long id) {
        return ApiResponse.ok(service.archive(id));
    }

    /** 分发公文给指定部门 */
    @PostMapping("/{id}/distributions")
    public ApiResponse<DocumentDistribution> distribute(@PathVariable Long id,
                                                         @Valid @RequestBody DocumentDistributionRequest request) {
        return ApiResponse.ok(service.distribute(id, request));
    }

    /** 查询公文的分发记录 */
    @GetMapping("/{id}/distributions")
    public ApiResponse<List<DocumentDistribution>> distributions(@PathVariable Long id) {
        return ApiResponse.ok(service.distributions(id));
    }

    /** 确认签收公文 */
    @PostMapping("/{id}/distributions/{distributionId}/receipt")
    public ApiResponse<DocumentDistribution> receipt(@PathVariable Long id, @PathVariable Long distributionId) {
        return ApiResponse.ok(service.receipt(id, distributionId));
    }

    /** 催办公文分发 */
    @PostMapping("/{id}/distributions/{distributionId}/remind")
    public ApiResponse<DocumentDistribution> remind(@PathVariable Long id, @PathVariable Long distributionId) {
        return ApiResponse.ok(service.remind(id, distributionId));
    }

    /** AI审阅公文 */
    @PostMapping("/{id}/ai-review")
    public ApiResponse<AiReviewResult> review(@PathVariable Long id) {
        return ApiResponse.ok(service.review(id));
    }

    /** AI起草公文 */
    @PostMapping("/ai-draft")
    public ApiResponse<String> draft(@Valid @RequestBody AiDraftRequest request) {
        return ApiResponse.ok(service.draft(request));
    }
}
