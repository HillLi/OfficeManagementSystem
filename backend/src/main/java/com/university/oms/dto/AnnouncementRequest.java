package com.university.oms.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 公告发布/编辑请求参数
 */
public class AnnouncementRequest {
    @NotBlank(message = "公告标题不能为空")
    @Size(max = 200, message = "公告标题不能超过200字")
    private String title;

    @NotBlank(message = "公告内容不能为空")
    @Size(max = 10000, message = "公告内容不能超过10000字")
    private String content;

    /** 公告分类 */
    private String category;
    /** 目标对象类型（如全校、指定部门） */
    private String targetType;
    /** 目标部门 ID（targetType 为指定部门时使用） */
    private Long targetDeptId;
    /** 是否置顶 */
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
