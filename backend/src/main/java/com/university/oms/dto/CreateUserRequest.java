package com.university.oms.dto;

import javax.validation.constraints.NotBlank;

public class CreateUserRequest {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
    @NotBlank
    private String realName;
    private Long deptId;
    private String roleKeys;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getRoleKeys() { return roleKeys; }
    public void setRoleKeys(String roleKeys) { this.roleKeys = roleKeys; }
}
