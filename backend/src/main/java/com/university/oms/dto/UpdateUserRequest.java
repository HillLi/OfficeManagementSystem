package com.university.oms.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;

public class UpdateUserRequest {
    private String realName;
    private String password;
    @Email
    @Pattern(regexp = ".*\\S.*", message = "邮箱不能为空")
    private String email;
    private Long deptId;
    private String roleKeys;
    private Boolean enabled;

    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getRoleKeys() { return roleKeys; }
    public void setRoleKeys(String roleKeys) { this.roleKeys = roleKeys; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
}
