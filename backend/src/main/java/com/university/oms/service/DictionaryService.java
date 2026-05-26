package com.university.oms.service;

import com.university.oms.common.BusinessException;
import com.university.oms.dto.DictionaryCatalogResponse;
import com.university.oms.dto.DictionaryItemRequest;
import com.university.oms.dto.DictionaryTypeRequest;
import com.university.oms.model.AuditLog;
import com.university.oms.model.DictionaryItem;
import com.university.oms.model.DictionaryType;
import com.university.oms.repository.DataPersistence;
import com.university.oms.repository.InMemoryDatabase;
import com.university.oms.security.AuthContext;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class DictionaryService {
    private static final Set<String> SYSTEM_TYPES = new LinkedHashSet<String>(Arrays.asList(
            "business_status", "distribution_status", "biz_type",
            "flow_node", "role_key", "secrecy_level"
    ));

    private final InMemoryDatabase db;
    private final DataPersistence persistence;

    public DictionaryService(InMemoryDatabase db, DataPersistence persistence) {
        this.db = db;
        this.persistence = persistence;
    }

    public DictionaryCatalogResponse catalog() {
        Map<String, List<DictionaryItem>> dictionaries = new LinkedHashMap<String, List<DictionaryItem>>();
        for (DictionaryType type : listTypes()) {
            dictionaries.put(type.getDictType(), listItems(type.getDictType()));
        }
        DictionaryCatalogResponse response = new DictionaryCatalogResponse();
        response.setVersion(version());
        response.setDictionaries(dictionaries);
        return response;
    }

    public String version() {
        LocalDateTime latest = null;
        for (DictionaryType type : db.dictionaryTypes().values()) {
            latest = latest(latest, type.getUpdatedAt());
        }
        for (DictionaryItem item : db.dictionaryItems().values()) {
            latest = latest(latest, item.getUpdatedAt());
        }
        return latest == null ? "" : latest.toString();
    }

    public List<DictionaryType> listTypes() {
        List<DictionaryType> types = new ArrayList<DictionaryType>(db.dictionaryTypes().values());
        types.sort(Comparator.comparing(DictionaryType::getDictType));
        return types;
    }

    public List<DictionaryItem> listItems(String dictType) {
        requireType(dictType);
        List<DictionaryItem> items = new ArrayList<DictionaryItem>();
        for (DictionaryItem item : db.dictionaryItems().values()) {
            if (dictType.equals(item.getDictType())) {
                items.add(item);
            }
        }
        items.sort(Comparator.comparing(DictionaryItem::getSortOrder)
                .thenComparing(DictionaryItem::getDictCode));
        return items;
    }

    public DictionaryType createType(DictionaryTypeRequest request) {
        if (db.dictionaryTypes().containsKey(request.getDictType())) {
            throw new BusinessException("字典类型代码已存在");
        }
        DictionaryType type = new DictionaryType();
        db.fill(type, db.nextId());
        type.setDictType(request.getDictType());
        type.setDictName(request.getDictName());
        type.setSystemType(false);
        type.setEnabled(request.isEnabled());
        type.setRemark(request.getRemark());
        type.setUpdatedAt(LocalDateTime.now());
        db.dictionaryTypes().put(type.getDictType(), type);
        persistence.saveDictionaryType(type);
        audit("create_type", type.getDictType(), type.getId());
        return type;
    }

    public DictionaryType updateType(String dictType, DictionaryTypeRequest request) {
        DictionaryType type = requireType(dictType);
        if (!dictType.equals(request.getDictType())) {
            throw new BusinessException("已有字典类型代码不可修改");
        }
        if (SYSTEM_TYPES.contains(dictType) && !request.isEnabled()) {
            throw new BusinessException("系统字典类型不可停用");
        }
        type.setDictName(request.getDictName());
        type.setEnabled(request.isEnabled());
        type.setRemark(request.getRemark());
        type.setUpdatedAt(LocalDateTime.now());
        persistence.saveDictionaryType(type);
        audit("update_type", dictType, type.getId());
        return type;
    }

    public DictionaryItem createItem(String dictType, DictionaryItemRequest request) {
        requireType(dictType);
        if (request.getDictCode() == null || request.getDictCode().trim().isEmpty()) {
            throw new BusinessException("字典项代码不能为空");
        }
        String key = db.dictionaryItemKey(dictType, request.getDictCode());
        if (db.dictionaryItems().get(key) != null) {
            throw new BusinessException("字典项代码已存在");
        }
        DictionaryItem item = new DictionaryItem();
        db.fill(item, db.nextId());
        item.setDictType(dictType);
        item.setDictCode(request.getDictCode());
        item.setDictLabel(request.getDictLabel());
        item.setSortOrder(sortOrder(request.getSortOrder()));
        item.setEnabled(request.isEnabled());
        item.setSystemItem(false);
        item.setRemark(request.getRemark());
        item.setUpdatedAt(LocalDateTime.now());
        db.dictionaryItems().put(key, item);
        persistence.saveDictionaryItem(item);
        audit("create_item", dictType + "/" + item.getDictCode(), item.getId());
        return item;
    }

    public DictionaryItem updateItem(String dictType, String code, DictionaryItemRequest request) {
        requireType(dictType);
        DictionaryItem item = db.dictionaryItems().get(db.dictionaryItemKey(dictType, code));
        if (item == null) {
            throw new BusinessException("字典项不存在");
        }
        if (request.getDictCode() != null && !code.equals(request.getDictCode())) {
            throw new BusinessException("已有字典项代码不可修改");
        }
        if (SYSTEM_TYPES.contains(dictType) && item.isSystemItem() && !request.isEnabled()) {
            throw new BusinessException("系统字典项不可停用");
        }
        item.setDictLabel(request.getDictLabel());
        item.setSortOrder(sortOrder(request.getSortOrder()));
        item.setEnabled(request.isEnabled());
        item.setRemark(request.getRemark());
        item.setUpdatedAt(LocalDateTime.now());
        persistence.saveDictionaryItem(item);
        audit("update_item", dictType + "/" + code, item.getId());
        return item;
    }

    public void requireEnabled(String dictType, String code, String fieldLabel) {
        DictionaryItem item = db.dictionaryItems().get(db.dictionaryItemKey(dictType, code));
        if (item == null || !item.isEnabled()) {
            throw new BusinessException(fieldLabel + "不在可选字典范围内：" + code);
        }
    }

    private DictionaryType requireType(String dictType) {
        DictionaryType type = db.dictionaryTypes().get(dictType);
        if (type == null) {
            throw new BusinessException("字典类型不存在");
        }
        return type;
    }

    private Integer sortOrder(Integer value) {
        return value == null ? Integer.valueOf(0) : value;
    }

    private LocalDateTime latest(LocalDateTime left, LocalDateTime right) {
        if (right == null || left != null && !right.isAfter(left)) {
            return left;
        }
        return right;
    }

    private void audit(String action, String detail, Long bizId) {
        AuditLog log = new AuditLog();
        db.fill(log, db.nextId());
        log.setOperatorId(AuthContext.currentUserIdOr(0L));
        log.setModule("dictionary");
        log.setAction(action);
        log.setBizType("dictionary");
        log.setBizId(bizId);
        log.setDetail(detail);
        db.auditLogs().add(log);
        persistence.saveAuditLog(log);
    }
}
