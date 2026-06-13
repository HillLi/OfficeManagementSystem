package com.university.oms.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 报告/请示创建请求参数
 */
public class ReportRequest {
    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题不能超过200字")
    private String title;
    /** 报告类型，默认"请示" */
    private String type = "请示";
    /** 保密等级，默认"内部" */
    private String secrecyLevel = "内部";
    @NotBlank(message = "内容不能为空")
    @Size(max = 10000, message = "内容不能超过10000字")
    private String content;
    @NotNull(message = "申请人不能为空")
    private Long applicantId;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getSecrecyLevel() { return secrecyLevel; }
    public void setSecrecyLevel(String secrecyLevel) { this.secrecyLevel = secrecyLevel; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Long getApplicantId() { return applicantId; }
    public void setApplicantId(Long applicantId) { this.applicantId = applicantId; }
}
