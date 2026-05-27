package com.university.oms.service;

import com.university.oms.common.BusinessException;
import com.university.oms.design.MeetingBuilder;
import com.university.oms.dto.MeetingRequest;
import com.university.oms.dto.MeetingMinutesRequest;
import com.university.oms.dto.RecommendRoomRequest;
import com.university.oms.model.Meeting;
import com.university.oms.model.MeetingRoom;
import com.university.oms.model.User;
import com.university.oms.repository.DataPersistence;
import com.university.oms.repository.InMemoryDatabase;
import com.university.oms.security.AuthContext;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
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
    private final WorkflowService workflowService;
    private final BusinessAccessService accessService;
    private final DictionaryService dictionaryService;

    public MeetingService(InMemoryDatabase db, ApprovalService approvalService, DataPersistence persistence,
                          WorkflowService workflowService, BusinessAccessService accessService,
                          DictionaryService dictionaryService) {
        this.db = db;
        this.approvalService = approvalService;
        this.persistence = persistence;
        this.workflowService = workflowService;
        this.accessService = accessService;
        this.dictionaryService = dictionaryService;
        this.meetingFeeStandards = db.meetingFeeStandards();
    }

    public List<MeetingRoom> rooms() {
        return new ArrayList<MeetingRoom>(db.rooms().values());
    }

    public List<Meeting> meetings() {
        User user = AuthContext.currentUser();
        List<Meeting> meetings = new ArrayList<Meeting>(db.meetings().values());
        if (user == null || user.getRoleKeys().contains("admin") || user.getRoleKeys().contains("office_admin")
                || user.getRoleKeys().contains("school_leader") || user.getRoleKeys().contains("security_staff")) {
            return meetings;
        }
        List<Meeting> scoped = new ArrayList<Meeting>();
        for (Meeting meeting : meetings) {
            User organizer = db.users().get(meeting.getOrganizerId());
            if (meeting.getOrganizerId().equals(user.getId())
                    || (user.getRoleKeys().contains("dept_head") && organizer != null && user.getDeptId().equals(organizer.getDeptId()))) {
                scoped.add(meeting);
            }
        }
        return scoped;
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
        dictionaryService.requireEnabled("venue_type", request.getVenueType(), "场地类型");
        dictionaryService.requireEnabled("meeting_type", request.getMeetingType(), "会议类别");
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new BusinessException("结束时间必须晚于开始时间");
        }
        Long organizerId = AuthContext.currentUserIdOr(request.getOrganizerId());
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
        if (large && workingDaysBetween(LocalDate.now(), request.getStartTime().toLocalDate()) < 15) {
            throw new BusinessException("大型活动必须至少提前15个工作日申请");
        }
        if (large && (blank(request.getRiskReportUrl()) || blank(request.getSecurityPlanUrl()) || blank(request.getEmergencyPlanUrl()))) {
            throw new BusinessException("大型活动必须上传风险报告、安全方案和应急预案");
        }
        validateMeetingFee(request);

        Meeting meeting = new MeetingBuilder()
                .title(request.getTitle())
                .roomId(request.getRoomId())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .organizerId(organizerId)
                .expectedCount(request.getExpectedCount())
                .venueType(request.getVenueType())
                .meetingType(request.getMeetingType())
                .budget(request.getBudget())
                .riskReportUrl(request.getRiskReportUrl())
                .securityPlanUrl(request.getSecurityPlanUrl())
                .emergencyPlanUrl(request.getEmergencyPlanUrl())
                .build();
        meeting.setAccommodationFee(request.getAccommodationFee());
        meeting.setMealFee(request.getMealFee());
        meeting.setVenueFee(request.getVenueFee());
        meeting.setOtherFee(request.getOtherFee());

        db.fill(meeting, db.nextId());
        meeting.setLargeActivity(large);
        meeting.setStatus(large ? "pending_security" : "pending_dept");
        db.meetings().put(meeting.getId(), meeting);
        persistence.saveMeeting(meeting);
        approvalService.record("meeting", meeting.getId(), organizerId, "submit", "提交会议申请");
        workflowService.startFlow("meeting", meeting.getId(), meeting.getStatus(), organizerId);
        return meeting;
    }

    public Meeting archiveMinutes(Long id, MeetingMinutesRequest request) {
        Meeting meeting = db.meetings().get(id);
        if (meeting == null) {
            throw new BusinessException("会议不存在");
        }
        accessService.requireMeetingMinutesArchive(meeting);
        if (!"approved".equals(meeting.getStatus())) {
            throw new BusinessException("只有审批通过的会议可以归档纪要");
        }
        meeting.setMinutes(request.getMinutes());
        meeting.setSignInCount(request.getSignInCount() == null ? 0 : request.getSignInCount());
        meeting.setStatus("archived");
        meeting.setUpdatedAt(LocalDateTime.now());
        persistence.saveMeeting(meeting);
        approvalService.record("meeting", id, AuthContext.currentUserIdOr(meeting.getOrganizerId()), "archive_minutes", "会议纪要归档");
        workflowService.advanceFlow("meeting", id, "approved", "archived", meeting.getOrganizerId());
        return meeting;
    }

    private void validateMeetingFee(MeetingRequest request) {
        String meetingType = request.getMeetingType();
        BigDecimal budget = request.getBudget() == null ? BigDecimal.ZERO : request.getBudget();
        if (request.getAccommodationFee() != null || request.getMealFee() != null
                || request.getVenueFee() != null || request.getOtherFee() != null) {
            BigDecimal total = amount(request.getAccommodationFee()).add(amount(request.getMealFee()))
                    .add(amount(request.getVenueFee())).add(amount(request.getOtherFee()));
            if (total.compareTo(budget) != 0) {
                throw new BusinessException("会议分项费用合计必须与申报预算一致");
            }
        }
        if (meetingType != null && meetingFeeStandards.containsKey(meetingType)) {
            BigDecimal limit = meetingFeeStandards.get(meetingType);
            if (budget.compareTo(limit) > 0) {
                throw new BusinessException(meetingType + "标准上限为" + limit + "元，当前预算" + budget + "元超标");
            }
        }
    }

    private BigDecimal amount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private long workingDaysBetween(LocalDate start, LocalDate end) {
        long count = 0;
        for (LocalDate date = start.plusDays(1); !date.isAfter(end); date = date.plusDays(1)) {
            if (date.getDayOfWeek() != DayOfWeek.SATURDAY && date.getDayOfWeek() != DayOfWeek.SUNDAY) {
                count++;
            }
        }
        return count;
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
