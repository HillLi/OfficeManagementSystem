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

/** 内部邮件控制器，负责邮件发送、收件箱和已发送管理 */
@RestController
@RequestMapping("/api/mails")
public class MailController {
    private final MailService service;

    public MailController(MailService service) {
        this.service = service;
    }

    /** 发送邮件 */
    @PostMapping
    public ApiResponse<MailDetailResponse> send(@Valid @RequestBody MailSendRequest request) {
        return ApiResponse.ok(service.send(request));
    }

    /** 查询收件箱列表 */
    @GetMapping("/inbox")
    public ApiResponse<List<MailDetailResponse>> inbox() {
        return ApiResponse.ok(service.inbox());
    }

    /** 查询已发送邮件列表 */
    @GetMapping("/sent")
    public ApiResponse<List<MailDetailResponse>> sent() {
        return ApiResponse.ok(service.sent());
    }

    /** 查询邮件详情 */
    @GetMapping("/{id}")
    public ApiResponse<MailDetailResponse> detail(@PathVariable Long id) {
        return ApiResponse.ok(service.detail(id));
    }

    /** 标记邮件为已读 */
    @PostMapping("/{id}/read")
    public ApiResponse<MailDetailResponse> markRead(@PathVariable Long id) {
        return ApiResponse.ok(service.markRead(id));
    }

    /** 重试发送外部邮件通知 */
    @PostMapping("/{id}/retry-email")
    public ApiResponse<MailDetailResponse> retryEmail(@PathVariable Long id) {
        return ApiResponse.ok(service.retryEmail(id));
    }
}
