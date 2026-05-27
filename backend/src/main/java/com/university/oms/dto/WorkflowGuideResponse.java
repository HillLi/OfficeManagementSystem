package com.university.oms.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class WorkflowGuideResponse {
    private String bizType;
    private Long bizId;
    private String title;
    private String currentNodeKey;
    private String status;
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

    public static class Step {
        private String key;
        private String label;
        private String type;
        private String status;
        private String roleKey;
        private String roleLabel;
        private Long operatorId;
        private String operatorName;
        private String opinion;
        private LocalDateTime time;
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
