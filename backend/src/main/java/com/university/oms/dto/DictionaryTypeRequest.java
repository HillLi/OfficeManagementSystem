package com.university.oms.dto;

import javax.validation.constraints.NotBlank;

/**
 * 数据字典类型创建/更新请求参数
 */
public class DictionaryTypeRequest {
    /** 字典类型编码 */
    @NotBlank
    private String dictType;
    /** 字典类型名称 */
    @NotBlank
    private String dictName;
    private boolean enabled = true;
    private String remark;

    public String getDictType() {
        return dictType;
    }

    public void setDictType(String dictType) {
        this.dictType = dictType;
    }

    public String getDictName() {
        return dictName;
    }

    public void setDictName(String dictName) {
        this.dictName = dictName;
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
