package com.university.oms.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

public class MailSendRequest {
    @NotBlank(message = "邮件主题不能为空")
    @Size(max = 255, message = "邮件主题不能超过255个字符")
    private String subject;

    @NotBlank(message = "邮件内容不能为空")
    @Size(max = 10000, message = "邮件内容不能超过10000字")
    private String content;

    @NotEmpty(message = "收件人不能为空")
    private List<Long> toUserIds = new ArrayList<Long>();
    private List<Long> ccUserIds = new ArrayList<Long>();

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<Long> getToUserIds() {
        return toUserIds;
    }

    public void setToUserIds(List<Long> toUserIds) {
        this.toUserIds = toUserIds;
    }

    public List<Long> getCcUserIds() {
        return ccUserIds;
    }

    public void setCcUserIds(List<Long> ccUserIds) {
        this.ccUserIds = ccUserIds;
    }
}
