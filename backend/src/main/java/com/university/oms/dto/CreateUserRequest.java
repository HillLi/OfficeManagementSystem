package com.university.oms.dto;

import javax.validation.constraints.*;

/**
 * 创建用户请求参数
 */
public class CreateUserRequest {
    @NotBlank
    @Size(min = 3, max = 50, message = "用户名长度必须在3到50字之间")
    private String username;
    @NotBlank
    @Size(min = 6, max = 50, message = "密码长度必须在6到50字之间")
    private String password;
    /** 用户真实姓名 */
    @NotBlank
    @Size(max = 50, message = "姓名不能超过50字")
    private String realName;
    @NotBlank
    @Email
    @Size(max = 100, message = "邮箱不能超过100字")
    private String email;
    /** 所属部门 ID */
    private Long deptId;
    /** 角色编码列表（逗号分隔） */
    private String roleKeys;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getRoleKeys() { return roleKeys; }
    public void setRoleKeys(String roleKeys) { this.roleKeys = roleKeys; }
}
