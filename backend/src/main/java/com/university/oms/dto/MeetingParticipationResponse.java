package com.university.oms.dto;

import java.time.LocalDateTime;

public class MeetingParticipationResponse {
    private Long id;
    private Long meetingId;
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long organizerId;
    private Long recorderId;
    private Integer expectedCount;
    private String status;
    private boolean recorder;
    private boolean minutesConfirmed;
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
