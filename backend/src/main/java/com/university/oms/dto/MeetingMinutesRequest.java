package com.university.oms.dto;

import javax.validation.constraints.NotBlank;

public class MeetingMinutesRequest {
    @NotBlank(message = "会议纪要不能为空")
    private String minutes;
    private Integer signInCount;

    public String getMinutes() { return minutes; }
    public void setMinutes(String minutes) { this.minutes = minutes; }
    public Integer getSignInCount() { return signInCount; }
    public void setSignInCount(Integer signInCount) { this.signInCount = signInCount; }
}
