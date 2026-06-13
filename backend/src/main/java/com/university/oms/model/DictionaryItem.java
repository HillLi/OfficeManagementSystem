package com.university.oms.model;

/**
 * 数据字典项实体
 */
public class DictionaryItem extends BaseEntity {
    /** 所属字典类型编码 */
    private String dictType;
    /** 字典项编码 */
    private String dictCode;
    /** 字典项显示标签 */
    private String dictLabel;
    /** 排序序号 */
    private Integer sortOrder = 0;
    private boolean enabled = true;
    /** 是否为系统内置项（不可删除） */
    private boolean systemItem;
    private String remark;

    public String getDictType() {
        return dictType;
    }

    public void setDictType(String dictType) {
        this.dictType = dictType;
    }

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

    public boolean isSystemItem() {
        return systemItem;
    }

    public void setSystemItem(boolean systemItem) {
        this.systemItem = systemItem;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
