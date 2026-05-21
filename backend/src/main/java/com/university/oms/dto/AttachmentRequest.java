package com.university.oms.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class AttachmentRequest {
    @NotBlank(message = "业务类型不能为空")
    private String bizType;
    @NotNull(message = "业务ID不能为空")
    private Long bizId;
    @NotBlank(message = "文件名不能为空")
    private String fileName;
    @NotBlank(message = "文件地址不能为空")
    private String fileUrl;
    private String secrecyLevel = "公开";

    public String getBizType() { return bizType; }
    public void setBizType(String bizType) { this.bizType = bizType; }
    public Long getBizId() { return bizId; }
    public void setBizId(Long bizId) { this.bizId = bizId; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public String getSecrecyLevel() { return secrecyLevel; }
    public void setSecrecyLevel(String secrecyLevel) { this.secrecyLevel = secrecyLevel; }
}
