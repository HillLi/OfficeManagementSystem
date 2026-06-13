package com.university.oms.dto;

import com.university.oms.model.DictionaryItem;

import java.util.List;
import java.util.Map;

/**
 * 数据字典目录响应（按类型分组返回所有字典项）
 */
public class DictionaryCatalogResponse {
    /** 字典版本号 */
    private String version;
    /** 按字典类型分组的字典项集合 */
    private Map<String, List<DictionaryItem>> dictionaries;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Map<String, List<DictionaryItem>> getDictionaries() {
        return dictionaries;
    }

    public void setDictionaries(Map<String, List<DictionaryItem>> dictionaries) {
        this.dictionaries = dictionaries;
    }
}
