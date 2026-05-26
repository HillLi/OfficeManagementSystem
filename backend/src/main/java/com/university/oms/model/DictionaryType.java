package com.university.oms.model;

public class DictionaryType extends BaseEntity {
    private String dictType;
    private String dictName;
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
