package com.university.oms.controller;

import com.university.oms.common.ApiResponse;
import com.university.oms.dto.AiDraftRequest;
import com.university.oms.dto.DocumentRequest;
import com.university.oms.model.AiReviewResult;
import com.university.oms.model.Document;
import com.university.oms.service.DocumentService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {
    private final DocumentService service;

    public DocumentController(DocumentService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<Document>> list() {
        return ApiResponse.ok(service.list());
    }

    @PostMapping
    public ApiResponse<Document> create(@Valid @RequestBody DocumentRequest request) {
        return ApiResponse.ok(service.create(request));
    }

    @PostMapping("/{id}/submit")
    public ApiResponse<Document> submit(@PathVariable Long id) {
        return ApiResponse.ok(service.submit(id));
    }

    @PostMapping("/{id}/ai-review")
    public ApiResponse<AiReviewResult> review(@PathVariable Long id) {
        return ApiResponse.ok(service.review(id));
    }

    @PostMapping("/ai-draft")
    public ApiResponse<String> draft(@Valid @RequestBody AiDraftRequest request) {
        return ApiResponse.ok(service.draft(request));
    }
}
