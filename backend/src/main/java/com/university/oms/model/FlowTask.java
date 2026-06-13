package com.university.oms.model;

import java.time.LocalDateTime;

/**
 * 审批流程任务实体，对应流程实例中的单个审批节点任务
 */
public class FlowTask extends BaseEntity {
    /** 所属流程实例ID */
    private Long instanceId;
    /** 关联的业务类型 */
    private String bizType;
    /** 关联的业务ID */
    private Long bizId;
    /** 流程节点标识 */
    private String nodeKey;
    /** 审批人角色 */
    private String approverRole;
    /** 审批人ID */
    private Long approverId;
    private String status;
    /** 任务截止时间 */
    private LocalDateTime dueTime;

    public Long getInstanceId() { return instanceId; }
    public void setInstanceId(Long instanceId) { this.instanceId = instanceId; }
    public String getBizType() { return bizType; }
    public void setBizType(String bizType) { this.bizType = bizType; }
    public Long getBizId() { return bizId; }
    public void setBizId(Long bizId) { this.bizId = bizId; }
    public String getNodeKey() { return nodeKey; }
    public void setNodeKey(String nodeKey) { this.nodeKey = nodeKey; }
    public String getApproverRole() { return approverRole; }
    public void setApproverRole(String approverRole) { this.approverRole = approverRole; }
    public Long getApproverId() { return approverId; }
    public void setApproverId(Long approverId) { this.approverId = approverId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getDueTime() { return dueTime; }
    public void setDueTime(LocalDateTime dueTime) { this.dueTime = dueTime; }
}
