package com.university.oms.controller;

import com.university.oms.common.ApiResponse;
import com.university.oms.dto.MeetingRequest;
import com.university.oms.dto.MeetingMinutesRequest;
import com.university.oms.dto.RecommendRoomRequest;
import com.university.oms.model.Meeting;
import com.university.oms.model.MeetingRoom;
import com.university.oms.service.MeetingService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/meetings")
public class MeetingController {
    private final MeetingService service;

    public MeetingController(MeetingService service) {
        this.service = service;
    }

    @GetMapping("/rooms")
    public ApiResponse<List<MeetingRoom>> rooms() {
        return ApiResponse.ok(service.rooms());
    }

    @PostMapping("/recommend")
    public ApiResponse<List<MeetingRoom>> recommend(@RequestBody RecommendRoomRequest request) {
        return ApiResponse.ok(service.recommend(request));
    }

    @GetMapping
    public ApiResponse<List<Meeting>> meetings() {
        return ApiResponse.ok(service.meetings());
    }

    @PostMapping
    public ApiResponse<Meeting> create(@Valid @RequestBody MeetingRequest request) {
        return ApiResponse.ok(service.create(request));
    }

    @PostMapping("/{id}/minutes")
    public ApiResponse<Meeting> archiveMinutes(@PathVariable Long id, @Valid @RequestBody MeetingMinutesRequest request) {
        return ApiResponse.ok(service.archiveMinutes(id, request));
    }
}
