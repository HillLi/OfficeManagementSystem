package com.university.oms.dto;

public class UpdateUserRequest {
    private String realName;
    private String password;
    private Long deptId;
    private String roleKeys;
    private Boolean enabled;

    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getRoleKeys() { return roleKeys; }
    public void setRoleKeys(String roleKeys) { this.roleKeys = roleKeys; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
}
