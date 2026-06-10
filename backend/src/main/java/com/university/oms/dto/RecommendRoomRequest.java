package com.university.oms.dto;

import javax.validation.constraints.*;
import java.time.LocalDateTime;

public class RecommendRoomRequest {
    @Min(value = 1, message = "预期人数至少为1")
    private Integer expectedCount = 1;
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
