package com.university.oms.dto;

import java.time.LocalDateTime;

/**
 * 会议参与信息响应（当前用户视角的会议及纪要状态）
 */
public class MeetingParticipationResponse {
    private Long id;
    private Long meetingId;
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long organizerId;
    /** 会议记录人 ID */
    private Long recorderId;
    /** 预计参会人数 */
    private Integer expectedCount;
    private String status;
    /** 当前用户是否为记录人 */
    private boolean recorder;
    /** 纪要是否已确认 */
    private boolean minutesConfirmed;
    /** 会议纪要内容 */
    private String minutes;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getMeetingId() { return meetingId; }
    public void setMeetingId(Long meetingId) { this.meetingId = meetingId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public Long getOrganizerId() { return organizerId; }
    public void setOrganizerId(Long organizerId) { this.organizerId = organizerId; }
    public Long getRecorderId() { return recorderId; }
    public void setRecorderId(Long recorderId) { this.recorderId = recorderId; }
    public Integer getExpectedCount() { return expectedCount; }
    public void setExpectedCount(Integer expectedCount) { this.expectedCount = expectedCount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public boolean isRecorder() { return recorder; }
    public void setRecorder(boolean recorder) { this.recorder = recorder; }
    public boolean isMinutesConfirmed() { return minutesConfirmed; }
    public void setMinutesConfirmed(boolean minutesConfirmed) { this.minutesConfirmed = minutesConfirmed; }
    public String getMinutes() { return minutes; }
    public void setMinutes(String minutes) { this.minutes = minutes; }
}
