package com.university.oms.dto;

import javax.validation.constraints.NotBlank;

/**
 * AI 辅助拟稿请求参数
 */
public class AiDraftRequest {
    /** 文种（如通知、请示等） */
    @NotBlank(message = "文种不能为空")
    private String docType;
    /** 公文主题 */
    @NotBlank(message = "主题不能为空")
    private String topic;
    /** 要点提示（可选） */
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
