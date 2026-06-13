package com.university.oms.design;

import com.university.oms.common.BusinessException;
import com.university.oms.model.Meeting;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// 建造者模式：逐步构建会议对象并进行校验
public class MeetingBuilder {
    private final Meeting meeting = new Meeting();

    public MeetingBuilder title(String title) {
        meeting.setTitle(title);
        return this;
    }

    public MeetingBuilder roomId(Long roomId) {
        meeting.setRoomId(roomId);
        return this;
    }

    public MeetingBuilder startTime(LocalDateTime startTime) {
        meeting.setStartTime(startTime);
        return this;
    }

    public MeetingBuilder endTime(LocalDateTime endTime) {
        meeting.setEndTime(endTime);
        return this;
    }

    public MeetingBuilder organizerId(Long organizerId) {
        meeting.setOrganizerId(organizerId);
        return this;
    }

    public MeetingBuilder expectedCount(Integer expectedCount) {
        meeting.setExpectedCount(expectedCount);
        return this;
    }

    public MeetingBuilder venueType(String venueType) {
        meeting.setVenueType(venueType);
        return this;
    }

    public MeetingBuilder meetingType(String meetingType) {
        meeting.setMeetingType(meetingType);
        return this;
    }

    public MeetingBuilder budget(BigDecimal budget) {
        meeting.setBudget(budget);
        return this;
    }

    public MeetingBuilder riskReportUrl(String url) {
        meeting.setRiskReportUrl(url);
        return this;
    }

    public MeetingBuilder securityPlanUrl(String url) {
        meeting.setSecurityPlanUrl(url);
        return this;
    }

    public MeetingBuilder emergencyPlanUrl(String url) {
        meeting.setEmergencyPlanUrl(url);
        return this;
    }

    // 构建会议对象，执行必要的字段校验
    public Meeting build() {
        if (meeting.getTitle() == null || meeting.getTitle().trim().isEmpty()) {
            throw new BusinessException("会议主题不能为空");
        }
        if (meeting.getRoomId() == null) {
            throw new BusinessException("会议室不能为空");
        }
        if (meeting.getOrganizerId() == null) {
            throw new BusinessException("组织者不能为空");
        }
        if (meeting.getStartTime() == null || meeting.getEndTime() == null) {
            throw new BusinessException("会议时间不能为空");
        }
        if (!meeting.getEndTime().isAfter(meeting.getStartTime())) {
            throw new BusinessException("结束时间必须晚于开始时间");
        }
        return meeting;
    }
}
