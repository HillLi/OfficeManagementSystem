package com.university.oms.model;

/**
 * 公文实体
 */
public class Document extends BaseEntity {
    /** 公文编号 */
    private String docNo;
    private String title;
    /** 公文类型 */
    private String docType;
    /** 紧急程度 */
    private String urgency;
    /** 密级 */
    private String secrecyLevel;
    /** 知悉范围 */
    private String knowledgeScope;
    private String content;
    /** 申请人ID */
    private Long applicantId;
    /** 所属部门ID */
    private Long deptId;
    private String status;
    /** 公文版本号 */
    private Integer version = 1;
    /** 分发状态（not_distributed / distributed） */
    private String distributionStatus = "not_distributed";
    /** AI审核结果 */
    private AiReviewResult aiReviewResult;

    public String getDocNo() {
        return docNo;
    }

    public void setDocNo(String docNo) {
        this.docNo = docNo;
    }

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

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getDistributionStatus() {
        return distributionStatus;
    }

    public void setDistributionStatus(String distributionStatus) {
        this.distributionStatus = distributionStatus;
    }

    public AiReviewResult getAiReviewResult() {
        return aiReviewResult;
    }

    public void setAiReviewResult(AiReviewResult aiReviewResult) {
        this.aiReviewResult = aiReviewResult;
    }
}
