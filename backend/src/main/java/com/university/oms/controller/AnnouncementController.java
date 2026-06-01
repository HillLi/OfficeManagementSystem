package com.university.oms.controller;

import com.university.oms.common.ApiResponse;
import com.university.oms.dto.AnnouncementRequest;
import com.university.oms.model.Announcement;
import com.university.oms.service.AnnouncementService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {
    private final AnnouncementService service;

    public AnnouncementController(AnnouncementService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<Announcement>> list(@RequestParam(defaultValue = "false") boolean includeDrafts) {
        return ApiResponse.ok(service.list(includeDrafts));
    }

    @GetMapping("/latest")
    public ApiResponse<List<Announcement>> latest(@RequestParam(defaultValue = "5") int limit) {
        return ApiResponse.ok(service.latest(limit));
    }

    @GetMapping("/{id}")
    public ApiResponse<Announcement> get(@PathVariable Long id) {
        return ApiResponse.ok(service.get(id));
    }

    @PostMapping
    public ApiResponse<Announcement> create(@Valid @RequestBody AnnouncementRequest request) {
        return ApiResponse.ok(service.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<Announcement> update(@PathVariable Long id, @Valid @RequestBody AnnouncementRequest request) {
        return ApiResponse.ok(service.update(id, request));
    }

    @PostMapping("/{id}/publish")
    public ApiResponse<Announcement> publish(@PathVariable Long id) {
        return ApiResponse.ok(service.publish(id));
    }

    @PostMapping("/{id}/withdraw")
    public ApiResponse<Announcement> withdraw(@PathVariable Long id) {
        return ApiResponse.ok(service.withdraw(id));
    }
}
