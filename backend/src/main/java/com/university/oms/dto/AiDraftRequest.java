package com.university.oms.dto;

import javax.validation.constraints.NotBlank;

public class AiDraftRequest {
    @NotBlank(message = "文种不能为空")
    private String docType;
    @NotBlank(message = "主题不能为空")
    private String topic;
    private String keyPoints;

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getKeyPoints() {
        return keyPoints;
    }

    public void setKeyPoints(String keyPoints) {
        this.keyPoints = keyPoints;
    }
}
