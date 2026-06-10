package com.university.oms.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class MeetingMinutesRequest {
    @NotBlank(message = "会议纪要不能为空")
    @Size(max = 10000, message = "纪要不能超过10000字")
    private String minutes;
    @Min(value = 0, message = "签到人数不能为负数")
    private Integer signInCount;

    public String getMinutes() { return minutes; }
    public void setMinutes(String minutes) { this.minutes = minutes; }
    public Integer getSignInCount() { return signInCount; }
    public void setSignInCount(Integer signInCount) { this.signInCount = signInCount; }
}
