package com.university.oms.controller;

import com.university.oms.common.ApiResponse;
import com.university.oms.dto.MeetingParticipationResponse;
import com.university.oms.dto.MeetingRequest;
import com.university.oms.dto.MeetingMinutesRequest;
import com.university.oms.dto.RecommendRoomRequest;
import com.university.oms.model.Meeting;
import com.university.oms.model.MeetingParticipant;
import com.university.oms.model.MeetingRoom;
import com.university.oms.recommend.ScoredRoom;
import com.university.oms.service.MeetingService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/** 会议管理控制器，负责会议室、会议安排和会议纪要管理 */
@RestController
@RequestMapping("/api/meetings")
public class MeetingController {
    private final MeetingService service;

    public MeetingController(MeetingService service) {
        this.service = service;
    }

    /** 查询会议室列表 */
    @GetMapping("/rooms")
    public ApiResponse<List<MeetingRoom>> rooms() {
        return ApiResponse.ok(service.rooms());
    }

    /** 根据条件推荐会议室 */
    @PostMapping("/recommend")
    public ApiResponse<List<MeetingRoom>> recommend(@Valid @RequestBody RecommendRoomRequest request) {
        return ApiResponse.ok(service.recommend(request));
    }

    /** 根据条件推荐会议室（增强版，返回评分详情） */
    @PostMapping("/recommend/enhanced")
    public ApiResponse<List<ScoredRoom>> recommendEnhanced(@Valid @RequestBody RecommendRoomRequest request) {
        return ApiResponse.ok(service.recommendEnhanced(request));
    }

    /** 查询会议列表 */
    @GetMapping
    public ApiResponse<List<Meeting>> meetings() {
        return ApiResponse.ok(service.meetings());
    }

    /** 创建会议 */
    @PostMapping
    public ApiResponse<Meeting> create(@Valid @RequestBody MeetingRequest request) {
        return ApiResponse.ok(service.create(request));
    }

    /** 归档会议纪要 */
    @PostMapping("/{id}/minutes")
    public ApiResponse<Meeting> archiveMinutes(@PathVariable Long id, @Valid @RequestBody MeetingMinutesRequest request) {
        return ApiResponse.ok(service.archiveMinutes(id, request));
    }

    /** 查询当前用户参与的会议 */
    @GetMapping("/participated")
    public ApiResponse<List<MeetingParticipationResponse>> participatedMeetings() {
        return ApiResponse.ok(service.participatedMeetings());
    }

    /** 查询指定会议的参与者列表 */
    @GetMapping("/{id}/participants")
    public ApiResponse<List<MeetingParticipant>> getParticipants(@PathVariable Long id) {
        return ApiResponse.ok(service.getMeetingParticipants(id));
    }

    /** 确认会议纪要 */
    @PostMapping("/{id}/confirm-minutes")
    public ApiResponse<Meeting> confirmMinutes(@PathVariable Long id) {
        return ApiResponse.ok(service.confirmMinutes(id));
    }

    /** 发布会议 */
    @PostMapping("/{id}/publish")
    public ApiResponse<Meeting> publish(@PathVariable Long id) {
        return ApiResponse.ok(service.publishMeeting(id));
    }

    /** 归档会议 */
    @PostMapping("/{id}/archive")
    public ApiResponse<Meeting> archive(@PathVariable Long id) {
        return ApiResponse.ok(service.archiveDirectly(id));
    }

    /** 提醒指定参会人员 */
    @PostMapping("/{id}/remind-participant/{userId}")
    public ApiResponse<Void> remindParticipant(@PathVariable Long id, @PathVariable Long userId) {
        service.remindParticipant(id, userId);
        return ApiResponse.ok(null);
    }
}
