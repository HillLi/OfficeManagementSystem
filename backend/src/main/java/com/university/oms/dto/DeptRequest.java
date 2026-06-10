package com.university.oms.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class DeptRequest {
    @NotBlank
    @Size(max = 100, message = "部门名称不能超过100字")
    private String deptName;
    private Long parentId;

    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
}
