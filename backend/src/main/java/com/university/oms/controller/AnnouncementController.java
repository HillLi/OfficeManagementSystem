package com.university.oms.controller;

import com.university.oms.common.ApiResponse;
import com.university.oms.dto.AnnouncementRequest;
import com.university.oms.model.Announcement;
import com.university.oms.service.AnnouncementService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/** 公告管理控制器，负责公告的发布、撤回和增删改查 */
@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {
    private final AnnouncementService service;

    public AnnouncementController(AnnouncementService service) {
        this.service = service;
    }

    /** 查询公告列表，可选择是否包含草稿 */
    @GetMapping
    public ApiResponse<List<Announcement>> list(@RequestParam(defaultValue = "false") boolean includeDrafts) {
        return ApiResponse.ok(service.list(includeDrafts));
    }

    /** 查询最新公告（限制条数） */
    @GetMapping("/latest")
    public ApiResponse<List<Announcement>> latest(@RequestParam(defaultValue = "5") int limit) {
        return ApiResponse.ok(service.latest(limit));
    }

    /** 根据ID查询单条公告 */
    @GetMapping("/{id}")
    public ApiResponse<Announcement> get(@PathVariable Long id) {
        return ApiResponse.ok(service.get(id));
    }

    /** 创建公告 */
    @PostMapping
    public ApiResponse<Announcement> create(@Valid @RequestBody AnnouncementRequest request) {
        return ApiResponse.ok(service.create(request));
    }

    /** 更新公告 */
    @PutMapping("/{id}")
    public ApiResponse<Announcement> update(@PathVariable Long id, @Valid @RequestBody AnnouncementRequest request) {
        return ApiResponse.ok(service.update(id, request));
    }

    /** 发布公告 */
    @PostMapping("/{id}/publish")
    public ApiResponse<Announcement> publish(@PathVariable Long id) {
        return ApiResponse.ok(service.publish(id));
    }

    /** 撤回公告 */
    @PostMapping("/{id}/withdraw")
    public ApiResponse<Announcement> withdraw(@PathVariable Long id) {
        return ApiResponse.ok(service.withdraw(id));
    }
}
