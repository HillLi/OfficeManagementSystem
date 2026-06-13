package com.university.oms.model;

/**
 * 审批流程节点实体：定义某流程（flow_key）中的一个审批步骤
 */
public class FlowNode extends BaseEntity {
    /** 所属流程Key，如 document、seal_dept、meeting_large */
    private String flowKey;
    /** 步骤顺序（从1开始） */
    private int sortOrder;
    /** 节点状态Key，如 pending_dept、pending_office */
    private String nodeKey;
    /** 节点显示名称，如 部门负责人审批 */
    private String nodeLabel;
    /** 审批角色Key，如 dept_head、office_admin */
    private String roleKey;
    /** 是否启用 */
    private boolean enabled = true;

    public String getFlowKey() {
        return flowKey;
    }

    public void setFlowKey(String flowKey) {
        this.flowKey = flowKey;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getNodeKey() {
        return nodeKey;
    }

    public void setNodeKey(String nodeKey) {
        this.nodeKey = nodeKey;
    }

    public String getNodeLabel() {
        return nodeLabel;
    }

    public void setNodeLabel(String nodeLabel) {
        this.nodeLabel = nodeLabel;
    }

    public String getRoleKey() {
        return roleKey;
    }

    public void setRoleKey(String roleKey) {
        this.roleKey = roleKey;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
