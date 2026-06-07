package com.university.oms.model;

import java.time.LocalDateTime;

public class MeetingParticipant extends BaseEntity {
    private Long meetingId;
    private Long userId;
    private boolean recorder;
    private boolean minutesConfirmed;
    private LocalDateTime confirmedAt;

    public Long getMeetingId() { return meetingId; }
    public void setMeetingId(Long meetingId) { this.meetingId = meetingId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public boolean isRecorder() { return recorder; }
    public void setRecorder(boolean recorder) { this.recorder = recorder; }
    public boolean isMinutesConfirmed() { return minutesConfirmed; }
    public void setMinutesConfirmed(boolean minutesConfirmed) { this.minutesConfirmed = minutesConfirmed; }
    public LocalDateTime getConfirmedAt() { return confirmedAt; }
    public void setConfirmedAt(LocalDateTime confirmedAt) { this.confirmedAt = confirmedAt; }
}
