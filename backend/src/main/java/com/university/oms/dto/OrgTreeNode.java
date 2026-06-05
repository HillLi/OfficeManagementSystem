package com.university.oms.dto;

import java.util.ArrayList;
import java.util.List;

public class OrgTreeNode {
    private String id;
    private String label;
    private String type;
    private Long deptId;
    private Long userId;
    private String email;
    private List<OrgTreeNode> children = new ArrayList<OrgTreeNode>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<OrgTreeNode> getChildren() {
        return children;
    }

    public void setChildren(List<OrgTreeNode> children) {
        this.children = children;
    }
}
