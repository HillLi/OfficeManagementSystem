package com.university.oms.controller;

import com.university.oms.common.ApiResponse;
import com.university.oms.dto.MailDetailResponse;
import com.university.oms.dto.MailSendRequest;
import com.university.oms.service.MailService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/mails")
public class MailController {
    private final MailService service;

    public MailController(MailService service) {
        this.service = service;
    }

    @PostMapping
    public ApiResponse<MailDetailResponse> send(@Valid @RequestBody MailSendRequest request) {
        return ApiResponse.ok(service.send(request));
    }

    @GetMapping("/inbox")
    public ApiResponse<List<MailDetailResponse>> inbox() {
        return ApiResponse.ok(service.inbox());
    }

    @GetMapping("/sent")
    public ApiResponse<List<MailDetailResponse>> sent() {
        return ApiResponse.ok(service.sent());
    }

    @GetMapping("/{id}")
    public ApiResponse<MailDetailResponse> detail(@PathVariable Long id) {
        return ApiResponse.ok(service.detail(id));
    }

    @PostMapping("/{id}/read")
    public ApiResponse<MailDetailResponse> markRead(@PathVariable Long id) {
        return ApiResponse.ok(service.markRead(id));
    }

    @PostMapping("/{id}/retry-email")
    public ApiResponse<MailDetailResponse> retryEmail(@PathVariable Long id) {
        return ApiResponse.ok(service.retryEmail(id));
    }
}
