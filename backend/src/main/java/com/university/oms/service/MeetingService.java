package com.university.oms.service;

import com.university.oms.common.BusinessException;
import com.university.oms.design.MeetingBuilder;
import com.university.oms.dto.MeetingRequest;
import com.university.oms.dto.RecommendRoomRequest;
import com.university.oms.model.Meeting;
import com.university.oms.model.MeetingRoom;
import com.university.oms.repository.DataPersistence;
import com.university.oms.repository.InMemoryDatabase;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MeetingService {
    private final InMemoryDatabase db;
    private final ApprovalService approvalService;
    private final DataPersistence persistence;
    private final Map<String, BigDecimal> meetingFeeStandards;

    public MeetingService(InMemoryDatabase db, ApprovalService approvalService, DataPersistence persistence) {
        this.db = db;
        this.approvalService = approvalService;
        this.persistence = persistence;
        this.meetingFeeStandards = db.meetingFeeStandards();
    }

    public List<MeetingRoom> rooms() {
        return new ArrayList<MeetingRoom>(db.rooms().values());
    }

    public List<Meeting> meetings() {
        return new ArrayList<Meeting>(db.meetings().values());
    }

    public List<MeetingRoom> recommend(RecommendRoomRequest request) {
        return db.rooms().values().stream()
                .filter(MeetingRoom::isEnabled)
                .filter(room -> room.getCapacity() >= safeCount(request.getExpectedCount()))
                .filter(room -> request.getEquipment() == null || room.getEquipment().contains(request.getEquipment()))
                .filter(room -> request.getStartTime() == null || !hasConflict(room.getId(), request.getStartTime(), request.getEndTime()))
                .sorted(Comparator.comparing(MeetingRoom::getCapacity))
                .collect(Collectors.toList());
    }

    public Meeting create(MeetingRequest request) {
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new BusinessException("结束时间必须晚于开始时间");
        }
        MeetingRoom room = db.rooms().get(request.getRoomId());
        if (room == null || !room.isEnabled()) {
            throw new BusinessException("会议室不可用");
        }
        if (room.getCapacity() < safeCount(request.getExpectedCount())) {
            throw new BusinessException("会议室容量不足");
        }
        if (hasConflict(request.getRoomId(), request.getStartTime(), request.getEndTime())) {
            throw new BusinessException("会议室在该时段已被占用");
        }
        boolean large = isLargeActivity(request.getVenueType(), request.getExpectedCount());
        if (large && (blank(request.getRiskReportUrl()) || blank(request.getSecurityPlanUrl()) || blank(request.getEmergencyPlanUrl()))) {
            throw new BusinessException("大型活动必须上传风险报告、安全方案和应急预案");
        }
        validateMeetingFee(request);

        Meeting meeting = new MeetingBuilder()
                .title(request.getTitle())
                .roomId(request.getRoomId())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .organizerId(request.getOrganizerId())
                .expectedCount(request.getExpectedCount())
                .venueType(request.getVenueType())
                .meetingType(request.getMeetingType())
                .budget(request.getBudget())
                .riskReportUrl(request.getRiskReportUrl())
                .securityPlanUrl(request.getSecurityPlanUrl())
                .emergencyPlanUrl(request.getEmergencyPlanUrl())
                .build();

        db.fill(meeting, db.nextId());
        meeting.setLargeActivity(large);
        meeting.setStatus(large ? "pending_security" : "pending_dept");
        db.meetings().put(meeting.getId(), meeting);
        persistence.saveMeeting(meeting);
        approvalService.record("meeting", meeting.getId(), request.getOrganizerId(), "submit", "提交会议申请");
        return meeting;
    }

    private void validateMeetingFee(MeetingRequest request) {
        String meetingType = request.getMeetingType();
        BigDecimal budget = request.getBudget() == null ? BigDecimal.ZERO : request.getBudget();
        if (meetingType != null && meetingFeeStandards.containsKey(meetingType)) {
            BigDecimal limit = meetingFeeStandards.get(meetingType);
            if (budget.compareTo(limit) > 0) {
                throw new BusinessException(meetingType + "标准上限为" + limit + "元，当前预算" + budget + "元超标");
            }
        }
    }

    private boolean hasConflict(Long roomId, LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return false;
        }
        for (Meeting meeting : db.meetings().values()) {
            if (meeting.getRoomId().equals(roomId)
                    && !"rejected".equals(meeting.getStatus())
                    && start.isBefore(meeting.getEndTime())
                    && end.isAfter(meeting.getStartTime())) {
                return true;
            }
        }
        return false;
    }

    private boolean isLargeActivity(String venueType, Integer expectedCount) {
        int count = expectedCount == null ? 0 : expectedCount;
        return ("室内".equals(venueType) && count > 500) || ("室外".equals(venueType) && count > 100);
    }

    private int safeCount(Integer count) {
        if (count == null) throw new BusinessException("预计参会人数不能为空");
        return count;
    }

    private boolean blank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
