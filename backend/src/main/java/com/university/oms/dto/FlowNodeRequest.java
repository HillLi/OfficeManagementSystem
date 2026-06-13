package com.university.oms.dto;

import javax.validation.constraints.NotBlank;

/**
 * 审批流程节点保存请求参数（用于管理员配置流程步骤）
 */
public class FlowNodeRequest {
    /** 节点状态Key，如 pending_dept */
    @NotBlank(message = "节点状态不能为空")
    private String nodeKey;
    /** 节点显示名称，如 部门负责人审批 */
    private String nodeLabel;
    /** 审批角色Key，如 dept_head */
    @NotBlank(message = "审批角色不能为空")
    private String roleKey;

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
}
