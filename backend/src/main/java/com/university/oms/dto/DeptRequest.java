package com.university.oms.dto;

import javax.validation.constraints.NotBlank;

public class DeptRequest {
    @NotBlank
    private String deptName;
    private Long parentId;

    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
}
