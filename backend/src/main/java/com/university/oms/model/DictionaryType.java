package com.university.oms.model;

/**
 * 数据字典类型实体
 */
public class DictionaryType extends BaseEntity {
    /** 字典类型编码 */
    private String dictType;
    /** 字典类型名称 */
    private String dictName;
    /** 是否为系统内置类型（不可删除） */
    private boolean systemType;
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

    public boolean isSystemType() {
        return systemType;
    }

    public void setSystemType(boolean systemType) {
        this.systemType = systemType;
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
