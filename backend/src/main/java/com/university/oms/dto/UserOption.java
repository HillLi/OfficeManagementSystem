package com.university.oms.dto;

public class UserOption {
    private Long id;
    private String realName;
    private Long deptId;
    private String deptName;

    public UserOption(Long id, String realName, Long deptId, String deptName) {
        this.id = id;
        this.realName = realName;
        this.deptId = deptId;
        this.deptName = deptName;
    }

    public Long getId() { return id; }
    public String getRealName() { return realName; }
    public Long getDeptId() { return deptId; }
    public String getDeptName() { return deptName; }
}
