package com.university.oms.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

/**
 * 附件实体，关联各类业务单据的文件附件
 */
public class Attachment extends BaseEntity {
    /** 关联的业务类型 */
    private String bizType;
    /** 关联的业务ID */
    private Long bizId;
    /** 存储文件名 */
    private String fileName;
    /** 原始文件名 */
    private String originalName;
    /** 文件访问URL */
    private String fileUrl;
    /** 服务器存储路径（不返回给前端） */
    @JsonIgnore
    private String storagePath;
    private Long fileSize;
    /** MIME类型 */
    private String contentType;
    /** 密级 */
    private String secrecyLevel;
    private Long uploaderId;
    /** 是否已删除（逻辑删除） */
    private boolean deleted;
    private Long deletedBy;
    private LocalDateTime deletedAt;
    private String deleteReason;

    public String getBizType() { return bizType; }
    public void setBizType(String bizType) { this.bizType = bizType; }
    public Long getBizId() { return bizId; }
    public void setBizId(Long bizId) { this.bizId = bizId; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public String getSecrecyLevel() { return secrecyLevel; }
    public void setSecrecyLevel(String secrecyLevel) { this.secrecyLevel = secrecyLevel; }
    public Long getUploaderId() { return uploaderId; }
    public void setUploaderId(Long uploaderId) { this.uploaderId = uploaderId; }
    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
    public Long getDeletedBy() { return deletedBy; }
    public void setDeletedBy(Long deletedBy) { this.deletedBy = deletedBy; }
    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }
    public String getDeleteReason() { return deleteReason; }
    public void setDeleteReason(String deleteReason) { this.deleteReason = deleteReason; }
}
