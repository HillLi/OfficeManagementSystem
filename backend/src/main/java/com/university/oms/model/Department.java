package com.university.oms.model;

/**
 * 部门实体
 */
public class Department extends BaseEntity {
    /** 部门名称 */
    private String deptName;
    /** 上级部门ID（用于构建部门树） */
    private Long parentId;

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
}
