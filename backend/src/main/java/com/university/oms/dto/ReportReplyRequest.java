package com.university.oms.dto;

import javax.validation.constraints.NotBlank;

/**
 * 报告批复请求参数
 */
public class ReportReplyRequest {
    @NotBlank(message = "批复内容不能为空")
    private String reply;

    public String getReply() { return reply; }
    public void setReply(String reply) { this.reply = reply; }
}
