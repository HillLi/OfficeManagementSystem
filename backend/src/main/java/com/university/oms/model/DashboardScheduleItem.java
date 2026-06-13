package com.university.oms.model;

import java.time.LocalDateTime;

/**
 * 仪表盘日程项，用于首页展示会议等日程安排
 */
public class DashboardScheduleItem {
    private Long id;
    /** 业务类型（如：meeting） */
    private String bizType;
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    /** 是否为大型活动 */
    private boolean largeActivity;
    private String roomName;
    /** 业务类型的中文显示文本 */
    private String typeText;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBizType() {
        return bizType;
    }

    public void setBizType(String bizType) {
        this.bizType = bizType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isLargeActivity() {
        return largeActivity;
    }

    public void setLargeActivity(boolean largeActivity) {
        this.largeActivity = largeActivity;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getTypeText() {
        return typeText;
    }

    public void setTypeText(String typeText) {
        this.typeText = typeText;
    }
}
