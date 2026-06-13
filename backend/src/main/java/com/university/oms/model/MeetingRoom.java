package com.university.oms.model;

/**
 * 会议室实体
 */
public class MeetingRoom extends BaseEntity {
    private String roomName;
    /** 容纳人数 */
    private Integer capacity;
    /** 设备配置描述 */
    private String equipment;
    /** 会议室位置 */
    private String location;
    /** 是否启用 */
    private boolean enabled;

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public String getEquipment() {
        return equipment;
    }

    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
