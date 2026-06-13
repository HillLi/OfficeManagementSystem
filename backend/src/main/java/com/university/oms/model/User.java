package com.university.oms.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 用户实体
 */
public class User extends BaseEntity {
    private String username;
    private String password;
    /** 真实姓名 */
    private String realName;
    private String email;
    /** 所属部门ID */
    private Long deptId;
    /** 所属部门名称（冗余字段） */
    private String deptName;
    /** 用户角色标识集合 */
    private Set<String> roleKeys = new LinkedHashSet<String>();

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @JsonIgnore
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public Set<String> getRoleKeys() {
        return roleKeys;
    }

    public void setRoleKeys(Set<String> roleKeys) {
        this.roleKeys = roleKeys;
    }
}
