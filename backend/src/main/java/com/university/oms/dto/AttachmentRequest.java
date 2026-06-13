package com.university.oms.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 附件上传请求参数
 */
public class AttachmentRequest {
    /** 关联的业务类型（如 document、report 等） */
    @NotBlank(message = "业务类型不能为空")
    private String bizType;
    /** 关联的业务主键 ID */
    @NotNull(message = "业务ID不能为空")
    private Long bizId;
    @NotBlank(message = "文件名不能为空")
    private String fileName;
    @NotBlank(message = "文件地址不能为空")
    private String fileUrl;
    /** 保密等级，默认"公开" */
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
