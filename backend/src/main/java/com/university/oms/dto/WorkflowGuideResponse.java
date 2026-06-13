package com.university.oms.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 工作流审批指引响应（展示审批流程各步骤及当前进度）
 */
public class WorkflowGuideResponse {
    private String bizType;
    private Long bizId;
    private String title;
    /** 当前所在流程节点标识 */
    private String currentNodeKey;
    private String status;
    /** 审批步骤列表 */
    private List<Step> steps = new ArrayList<Step>();

    public String getBizType() { return bizType; }
    public void setBizType(String bizType) { this.bizType = bizType; }
    public Long getBizId() { return bizId; }
    public void setBizId(Long bizId) { this.bizId = bizId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCurrentNodeKey() { return currentNodeKey; }
    public void setCurrentNodeKey(String currentNodeKey) { this.currentNodeKey = currentNodeKey; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<Step> getSteps() { return steps; }
    public void setSteps(List<Step> steps) { this.steps = steps; }

    /**
     * 审批步骤详情
     */
    public static class Step {
        private String key;
        /** 步骤显示名称 */
        private String label;
        /** 步骤类型（如 approval、submit 等） */
        private String type;
        private String status;
        private String roleKey;
        private String roleLabel;
        /** 实际操作人 ID */
        private Long operatorId;
        private String operatorName;
        /** 审批意见 */
        private String opinion;
        /** 操作时间 */
        private LocalDateTime time;
        /** 截止时间 */
        private LocalDateTime dueTime;

        public Step() { }

        public Step(String key, String label, String type, String status) {
            this.key = key;
            this.label = label;
            this.type = type;
            this.status = status;
        }

        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getRoleKey() { return roleKey; }
        public void setRoleKey(String roleKey) { this.roleKey = roleKey; }
        public String getRoleLabel() { return roleLabel; }
        public void setRoleLabel(String roleLabel) { this.roleLabel = roleLabel; }
        public Long getOperatorId() { return operatorId; }
        public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }
        public String getOperatorName() { return operatorName; }
        public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
        public String getOpinion() { return opinion; }
        public void setOpinion(String opinion) { this.opinion = opinion; }
        public LocalDateTime getTime() { return time; }
        public void setTime(LocalDateTime time) { this.time = time; }
        public LocalDateTime getDueTime() { return dueTime; }
        public void setDueTime(LocalDateTime dueTime) { this.dueTime = dueTime; }
    }
}
