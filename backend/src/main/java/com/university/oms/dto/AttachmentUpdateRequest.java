package com.university.oms.dto;

import javax.validation.constraints.NotBlank;

/**
 * 附件更新请求参数（修改名称和密级）
 */
public class AttachmentUpdateRequest {
    @NotBlank(message = "材料名称不能为空")
    private String fileName;
    @NotBlank(message = "材料密级不能为空")
    private String secrecyLevel;

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getSecrecyLevel() { return secrecyLevel; }
    public void setSecrecyLevel(String secrecyLevel) { this.secrecyLevel = secrecyLevel; }
}
