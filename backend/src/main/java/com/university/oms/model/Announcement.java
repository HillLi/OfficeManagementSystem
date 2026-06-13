package com.university.oms.model;

import java.time.LocalDateTime;

/**
 * 公告实体
 */
public class Announcement extends BaseEntity {
    private String title;
    private String content;
    /** 公告分类 */
    private String category;
    /** 目标对象类型（如：全员、指定部门） */
    private String targetType;
    /** 目标部门ID */
    private Long targetDeptId;
    private String targetDeptName;
    /** 是否置顶 */
    private boolean pinned;
    private String status;
    private Long publisherId;
    private LocalDateTime publishedAt;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public Long getTargetDeptId() { return targetDeptId; }
    public void setTargetDeptId(Long targetDeptId) { this.targetDeptId = targetDeptId; }
    public String getTargetDeptName() { return targetDeptName; }
    public void setTargetDeptName(String targetDeptName) { this.targetDeptName = targetDeptName; }
    public boolean isPinned() { return pinned; }
    public void setPinned(boolean pinned) { this.pinned = pinned; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getPublisherId() { return publisherId; }
    public void setPublisherId(Long publisherId) { this.publisherId = publisherId; }
    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }
}
