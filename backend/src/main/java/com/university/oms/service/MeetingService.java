package com.university.oms.service;

import com.university.oms.common.BusinessException;
import com.university.oms.design.MeetingBuilder;
import com.university.oms.dto.MeetingParticipationResponse;
import com.university.oms.dto.MeetingRequest;
import com.university.oms.dto.MeetingMinutesRequest;
import com.university.oms.dto.RecommendRoomRequest;
import com.university.oms.model.Announcement;
import com.university.oms.model.Meeting;
import com.university.oms.model.MeetingParticipant;
import com.university.oms.model.MeetingRoom;
import com.university.oms.model.User;
import com.university.oms.recommend.RoomRecommendationService;
import com.university.oms.recommend.ScoredRoom;
import com.university.oms.repository.OmsRepository;
import com.university.oms.security.AuthContext;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 会议管理服务，处理会议申请、纪要归档、纪要确认和会议室推荐
 */
@Service
public class MeetingService {
    private final OmsRepository repo;
    private final ApprovalService approvalService;
    private final Map<String, BigDecimal> meetingFeeStandards;
    private final WorkflowService workflowService;
    private final BusinessAccessService accessService;
    private final DictionaryService dictionaryService;
    private final RoomRecommendationService recommendationService;

    public MeetingService(OmsRepository repo, ApprovalService approvalService,
                          WorkflowService workflowService, BusinessAccessService accessService,
                          DictionaryService dictionaryService, RoomRecommendationService recommendationService) {
        this.repo = repo;
        this.approvalService = approvalService;
        this.workflowService = workflowService;
        this.accessService = accessService;
        this.dictionaryService = dictionaryService;
        this.recommendationService = recommendationService;
        this.meetingFeeStandards = repo.findMeetingFeeStandards();
    }

    /** 获取所有会议室列表 */
    public List<MeetingRoom> rooms() {
        return repo.findAllRooms();
    }

    /** 获取当前用户可见的会议列表（根据角色过滤） */
    public List<Meeting> meetings() {
        User user = AuthContext.currentUser();
        List<Meeting> meetings = repo.findAllMeetings();
        if (user == null || user.getRoleKeys().contains("admin") || user.getRoleKeys().contains("office_admin")
                || user.getRoleKeys().contains("school_leader") || user.getRoleKeys().contains("security_staff")) {
            return meetings;
        }
        List<Meeting> scoped = new ArrayList<Meeting>();
        for (Meeting meeting : meetings) {
            User organizer = repo.findUserById(meeting.getOrganizerId());
            if (meeting.getOrganizerId().equals(user.getId())
                    || (user.getRoleKeys().contains("dept_head") && organizer != null && user.getDeptId().equals(organizer.getDeptId()))
                    || isParticipant(meeting.getId(), user.getId())) {
                scoped.add(meeting);
            }
        }
        return scoped;
    }

    /** 基础会议室推荐（按容量、设备、时间过滤） */
    public List<MeetingRoom> recommend(RecommendRoomRequest request) {
        return repo.findAllRooms().stream()
                .filter(MeetingRoom::isEnabled)
                .filter(room -> room.getCapacity() >= safeCount(request.getExpectedCount()))
                .filter(room -> request.getEquipment() == null || room.getEquipment().contains(request.getEquipment()))
                .filter(room -> request.getStartTime() == null || !hasConflict(room.getId(), request.getStartTime(), request.getEndTime()))
                .sorted(Comparator.comparing(MeetingRoom::getCapacity))
                .collect(Collectors.toList());
    }

    /** 智能会议室推荐（多维加权评分） */
    public List<ScoredRoom> recommendEnhanced(RecommendRoomRequest request) {
        return recommendationService.recommendEnhanced(request);
    }

    /**
     * 创建会议申请
     * 包含参会人校验、会议室容量校验、大型活动审批规则校验、费用校验等
     */
    public Meeting create(MeetingRequest request) {
        dictionaryService.requireEnabled("venue_type", request.getVenueType(), "场地类型");
        dictionaryService.requireEnabled("meeting_type", request.getMeetingType(), "会议类别");
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new BusinessException("结束时间必须晚于开始时间");
        }
        Long organizerId = AuthContext.currentUserIdOr(request.getOrganizerId());

        if (request.getParticipants() == null || request.getParticipants().isEmpty()) {
            throw new BusinessException("请选择至少一位参会人员");
        }
        Set<Long> participantIds = new LinkedHashSet<Long>(request.getParticipants());
        if (request.getRecorderId() == null) {
            throw new BusinessException("请指定会议记录员");
        }
        if (!participantIds.contains(request.getRecorderId())) {
            throw new BusinessException("记录员必须在参会人员中");
        }
        for (Long participantId : participantIds) {
            if (repo.findUserById(participantId) == null) {
                throw new BusinessException("参会人员不存在：" + participantId);
            }
        }
        // 实际参会人数取参会人数与预期人数的较大值
        int regulatoryExpectedCount = request.getExpectedCount() == null
                ? participantIds.size()
                : Math.max(participantIds.size(), request.getExpectedCount());
        request.setExpectedCount(participantIds.size());

        MeetingRoom room = repo.findRoomById(request.getRoomId());
        if (room == null || !room.isEnabled()) {
            throw new BusinessException("会议室不可用");
        }
        if (room.getCapacity() < safeCount(request.getExpectedCount())) {
            throw new BusinessException("会议室容量不足");
        }
        if (hasConflict(request.getRoomId(), request.getStartTime(), request.getEndTime())) {
            throw new BusinessException("会议室在该时段已被占用");
        }
        // 大型活动需提前15个工作日申请
        boolean large = isLargeActivity(request.getVenueType(), regulatoryExpectedCount);
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
        meeting.setRecorderId(request.getRecorderId());

        OmsRepository.fillEntity(meeting, repo.nextId());
        meeting.setLargeActivity(large);
        meeting.setStatus(large ? "pending_security" : "pending_dept");
        repo.saveMeeting(meeting);
        approvalService.record("meeting", meeting.getId(), organizerId, "submit", "提交会议申请");
        workflowService.startFlow("meeting", meeting.getId(), meeting.getStatus(), organizerId);
        // 保存参会人
        for (Long userId : participantIds) {
            MeetingParticipant participant = new MeetingParticipant();
            OmsRepository.fillEntity(participant, repo.nextId());
            participant.setMeetingId(meeting.getId());
            participant.setUserId(userId);
            participant.setRecorder(userId.equals(request.getRecorderId()));
            repo.saveMeetingParticipant(participant);
        }
        return meeting;
    }

    /** 记录员填写会议纪要，提交后通知所有参会人确认 */
    public Meeting archiveMinutes(Long id, MeetingMinutesRequest request) {
        Meeting meeting = repo.findMeetingById(id);
        if (meeting == null) {
            throw new BusinessException("会议不存在");
        }
        if (!"approved".equals(meeting.getStatus())) {
            throw new BusinessException("只有审批通过的会议可以填写纪要");
        }
        Long currentUserId = AuthContext.currentUserIdOr(0L);
        if (!currentUserId.equals(meeting.getRecorderId())) {
            throw new BusinessException("只有记录员可以填写会议纪要");
        }
        meeting.setMinutes(request.getMinutes());
        meeting.setSignInCount(request.getSignInCount() == null ? 0 : request.getSignInCount());
        meeting.setStatus("minutes_pending");
        meeting.setUpdatedAt(LocalDateTime.now());
        repo.saveMeeting(meeting);
        approvalService.record("meeting", id, currentUserId, "archive_minutes", "记录员填写会议纪要");
        workflowService.advanceFlow("meeting", id, "approved", "minutes_pending", meeting.getOrganizerId());
        // 通知所有参会人确认纪要
        for (MeetingParticipant p : getMeetingParticipants(meeting.getId())) {
            if (!p.isRecorder()) {
                workflowService.notifyUser(p.getUserId(), "会议纪要待确认",
                        "会议《" + meeting.getTitle() + "》的纪要已填写，请及时确认。", "meeting", meeting.getId());
            }
        }
        return meeting;
    }

    /** 获取指定会议的参会人列表 */
    public List<MeetingParticipant> getMeetingParticipants(Long meetingId) {
        return repo.findParticipantsByMeetingId(meetingId);
    }

    /** 获取当前用户参与的会议列表 */
    public List<MeetingParticipationResponse> participatedMeetings() {
        User user = AuthContext.requireUser();
        List<MeetingParticipationResponse> result = new ArrayList<MeetingParticipationResponse>();
        Set<Long> meetingIds = new HashSet<Long>();
        for (MeetingParticipant p : repo.findParticipantsByUserId(user.getId())) {
            if (!meetingIds.add(p.getMeetingId())) {
                continue;
            }
            Meeting meeting = repo.findMeetingById(p.getMeetingId());
            if (meeting != null) {
                result.add(toParticipationResponse(meeting, p));
            }
        }
        return result;
    }

    private MeetingParticipationResponse toParticipationResponse(Meeting meeting, MeetingParticipant participant) {
        MeetingParticipationResponse response = new MeetingParticipationResponse();
        response.setId(meeting.getId());
        response.setMeetingId(meeting.getId());
        response.setTitle(meeting.getTitle());
        response.setStartTime(meeting.getStartTime());
        response.setEndTime(meeting.getEndTime());
        response.setOrganizerId(meeting.getOrganizerId());
        response.setRecorderId(meeting.getRecorderId());
        response.setExpectedCount(meeting.getExpectedCount());
        response.setStatus(meeting.getStatus());
        response.setRecorder(participant.isRecorder());
        response.setMinutesConfirmed(participant.isMinutesConfirmed());
        response.setMinutes(meeting.getMinutes());
        return response;
    }

    /** 参会人确认会议纪要，全员确认后自动流转 */
    public Meeting confirmMinutes(Long meetingId) {
        User user = AuthContext.requireUser();
        Meeting meeting = repo.findMeetingById(meetingId);
        if (meeting == null) {
            throw new BusinessException("会议不存在");
        }
        if (!"minutes_pending".equals(meeting.getStatus())) {
            throw new BusinessException("当前状态无法确认纪要");
        }
        MeetingParticipant target = repo.findParticipantByMeetingIdAndUserId(meetingId, user.getId());
        if (target == null) {
            throw new BusinessException("您不是该会议的参会人员");
        }
        if (target.isMinutesConfirmed()) {
            throw new BusinessException("您已确认过纪要");
        }
        target.setMinutesConfirmed(true);
        target.setConfirmedAt(LocalDateTime.now());
        target.setUpdatedAt(LocalDateTime.now());
        repo.saveMeetingParticipant(target);
        approvalService.record("meeting", meetingId, user.getId(), "confirm_minutes", "参会人确认纪要");

        // 检查是否全员确认
        boolean allConfirmed = true;
        for (MeetingParticipant p : getMeetingParticipants(meetingId)) {
            if (!p.isMinutesConfirmed()) {
                allConfirmed = false;
                break;
            }
        }
        if (allConfirmed) {
            meeting.setStatus("minutes_confirmed");
            meeting.setUpdatedAt(LocalDateTime.now());
            repo.saveMeeting(meeting);
            workflowService.advanceFlow("meeting", meetingId, "minutes_pending", "minutes_confirmed", meeting.getOrganizerId());
            workflowService.notifyUser(meeting.getOrganizerId(), "会议纪要全员确认完成",
                    "会议《" + meeting.getTitle() + "》的纪要已由所有参会人确认，请决定是否公示。", "meeting", meetingId);
        }
        return meeting;
    }

    /** 将会议纪要发布为公告 */
    public Meeting publishMeeting(Long meetingId) {
        User user = AuthContext.requireUser();
        Meeting meeting = repo.findMeetingById(meetingId);
        if (meeting == null) {
            throw new BusinessException("会议不存在");
        }
        if (!user.getId().equals(meeting.getOrganizerId())) {
            throw new BusinessException("只有组织者可以发布");
        }
        if (!"minutes_confirmed".equals(meeting.getStatus())) {
            throw new BusinessException("只有全员确认后的会议可以发布");
        }
        // 创建通知公告
        Announcement announcement = new Announcement();
        OmsRepository.fillEntity(announcement, repo.nextId());
        announcement.setTitle("会议纪要公示：" + meeting.getTitle());
        announcement.setContent(meeting.getMinutes() != null ? meeting.getMinutes() : "");
        announcement.setCategory("会议纪要");
        announcement.setTargetType("all");
        announcement.setPublisherId(user.getId());
        announcement.setStatus("published");
        announcement.setPublishedAt(LocalDateTime.now());
        repo.saveAnnouncement(announcement);

        meeting.setStatus("archived");
        meeting.setUpdatedAt(LocalDateTime.now());
        repo.saveMeeting(meeting);
        approvalService.record("meeting", meetingId, user.getId(), "publish", "发布为公告");
        workflowService.advanceFlow("meeting", meetingId, "minutes_confirmed", "archived", meeting.getOrganizerId());
        return meeting;
    }

    /** 直接归档会议（不发布为公告） */
    public Meeting archiveDirectly(Long meetingId) {
        User user = AuthContext.requireUser();
        Meeting meeting = repo.findMeetingById(meetingId);
        if (meeting == null) {
            throw new BusinessException("会议不存在");
        }
        if (!user.getId().equals(meeting.getOrganizerId())) {
            throw new BusinessException("只有组织者可以归档");
        }
        if (!"minutes_confirmed".equals(meeting.getStatus())) {
            throw new BusinessException("只有全员确认后的会议可以归档");
        }
        meeting.setStatus("archived");
        meeting.setUpdatedAt(LocalDateTime.now());
        repo.saveMeeting(meeting);
        approvalService.record("meeting", meetingId, user.getId(), "archive", "直接归档");
        workflowService.advanceFlow("meeting", meetingId, "minutes_confirmed", "archived", meeting.getOrganizerId());
        return meeting;
    }

    /** 催办参会人确认会议纪要 */
    public void remindParticipant(Long meetingId, Long userId) {
        User currentUser = AuthContext.requireUser();
        Meeting meeting = repo.findMeetingById(meetingId);
        if (meeting == null) {
            throw new BusinessException("会议不存在");
        }
        if (!currentUser.getId().equals(meeting.getOrganizerId())) {
            throw new BusinessException("只有组织者可以催办");
        }
        // 验证被催办人是参会人且未确认
        MeetingParticipant target = repo.findParticipantByMeetingIdAndUserId(meetingId, userId);
        if (target == null) {
            throw new BusinessException("该用户不是参会人员");
        }
        if (target.isMinutesConfirmed()) {
            throw new BusinessException("该参会人已确认纪要");
        }
        workflowService.notifyUser(userId, "会议纪要确认催办",
                "组织者提醒您尽快确认会议《" + meeting.getTitle() + "》的纪要。", "meeting", meetingId);
    }

    private boolean isParticipant(Long meetingId, Long userId) {
        return repo.findParticipantByMeetingIdAndUserId(meetingId, userId) != null;
    }

    /** 校验会议费用：分项合计须与预算一致，且不超过会议类型标准上限 */
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

    /** 计算两个日期之间的工作日数 */
    private long workingDaysBetween(LocalDate start, LocalDate end) {
        long count = 0;
        for (LocalDate date = start.plusDays(1); !date.isAfter(end); date = date.plusDays(1)) {
            if (date.getDayOfWeek() != DayOfWeek.SATURDAY && date.getDayOfWeek() != DayOfWeek.SUNDAY) {
                count++;
            }
        }
        return count;
    }

    /** 检查会议室在指定时段是否有时间冲突 */
    private boolean hasConflict(Long roomId, LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return false;
        }
        for (Meeting meeting : repo.findAllMeetings()) {
            if (meeting.getRoomId().equals(roomId)
                    && !"rejected".equals(meeting.getStatus())
                    && start.isBefore(meeting.getEndTime())
                    && end.isAfter(meeting.getStartTime())) {
                return true;
            }
        }
        return false;
    }

    /** 判断是否为大型活动（室内>500人或室外>100人） */
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
