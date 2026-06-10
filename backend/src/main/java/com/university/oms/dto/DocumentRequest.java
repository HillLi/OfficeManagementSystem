package com.university.oms.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class DocumentRequest {
    @NotBlank(message = "公文标题不能为空")
    @Size(max = 200, message = "公文标题不能超过200字")
    private String title;
    @NotBlank(message = "文种不能为空")
    private String docType;
    private String urgency = "普通";
    private String secrecyLevel = "公开";
    private String knowledgeScope = "全校";
    @NotBlank(message = "正文不能为空")
    @Size(max = 10000, message = "正文不能超过10000字")
    private String content;
    @NotNull(message = "申请人不能为空")
    private Long applicantId;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public String getUrgency() {
        return urgency;
    }

    public void setUrgency(String urgency) {
        this.urgency = urgency;
    }

    public String getSecrecyLevel() {
        return secrecyLevel;
    }

    public void setSecrecyLevel(String secrecyLevel) {
        this.secrecyLevel = secrecyLevel;
    }

    public String getKnowledgeScope() {
        return knowledgeScope;
    }

    public void setKnowledgeScope(String knowledgeScope) {
        this.knowledgeScope = knowledgeScope;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getApplicantId() {
        return applicantId;
    }

    public void setApplicantId(Long applicantId) {
        this.applicantId = applicantId;
    }
}
