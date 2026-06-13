package com.university.oms.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 组织架构树节点（部门-人员层级结构）
 */
public class OrgTreeNode {
    private String id;
    /** 节点显示名称 */
    private String label;
    /** 节点类型（dept/user） */
    private String type;
    /** 关联的部门 ID（type=dept 时有值） */
    private Long deptId;
    /** 关联的用户 ID（type=user 时有值） */
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
