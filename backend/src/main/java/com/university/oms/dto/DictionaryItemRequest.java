package com.university.oms.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 数据字典项创建/更新请求参数
 */
public class DictionaryItemRequest {
    /** 字典项编码 */
    @NotBlank(message = "项目代码不能为空")
    private String dictCode;
    /** 字典项显示值 */
    @NotBlank
    @Size(max = 100, message = "显示值不能超过100字")
    private String dictLabel;
    /** 排序序号 */
    private Integer sortOrder = 0;
    private boolean enabled = true;
    private String remark;

    public String getDictCode() {
        return dictCode;
    }

    public void setDictCode(String dictCode) {
        this.dictCode = dictCode;
    }

    public String getDictLabel() {
        return dictLabel;
    }

    public void setDictLabel(String dictLabel) {
        this.dictLabel = dictLabel;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
