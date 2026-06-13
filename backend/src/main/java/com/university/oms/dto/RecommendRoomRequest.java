package com.university.oms.dto;

import javax.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * 会议室推荐请求参数（根据人数和设备需求推荐合适会议室）
 */
public class RecommendRoomRequest {
    /** 预期参会人数 */
    @Min(value = 1, message = "预期人数至少为1")
    private Integer expectedCount = 1;
    /** 所需设备要求描述 */
    @Size(max = 200, message = "设备要求不能超过200字")
    private String equipment;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public Integer getExpectedCount() { return expectedCount; }
    public void setExpectedCount(Integer expectedCount) { this.expectedCount = expectedCount; }
    public String getEquipment() { return equipment; }
    public void setEquipment(String equipment) { this.equipment = equipment; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
}
