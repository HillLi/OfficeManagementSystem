package com.university.oms.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class AnnouncementRequest {
    @NotBlank(message = "公告标题不能为空")
    @Size(max = 200, message = "公告标题不能超过200字")
    private String title;

    @NotBlank(message = "公告内容不能为空")
    @Size(max = 10000, message = "公告内容不能超过10000字")
    private String content;

    private String category;
    private String targetType;
    private Long targetDeptId;
    private Boolean pinned;

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
    public Boolean getPinned() { return pinned; }
    public void setPinned(Boolean pinned) { this.pinned = pinned; }
}
