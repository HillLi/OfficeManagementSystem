package com.university.oms.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 更新用户信息请求参数（所有字段可选，仅更新传入的字段）
 */
public class UpdateUserRequest {
    /** 用户真实姓名 */
    @Size(max = 50, message = "姓名不能超过50字")
    private String realName;
    @Size(min = 6, max = 50, message = "密码长度必须在6到50字之间")
    private String password;
    @Email
    @Pattern(regexp = ".*\\S.*", message = "邮箱不能为空")
    private String email;
    /** 所属部门 ID */
    private Long deptId;
    /** 角色编码列表（逗号分隔） */
    private String roleKeys;
    /** 是否启用 */
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
