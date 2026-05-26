package com.university.oms.dto;

import com.university.oms.model.DictionaryItem;

import java.util.List;
import java.util.Map;

public class DictionaryCatalogResponse {
    private String version;
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
