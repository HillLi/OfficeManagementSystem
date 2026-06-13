package com.university.oms.service;

import com.university.oms.common.BusinessException;
import com.university.oms.dto.DictionaryCatalogResponse;
import com.university.oms.dto.DictionaryItemRequest;
import com.university.oms.dto.DictionaryTypeRequest;
import com.university.oms.model.AuditLog;
import com.university.oms.model.DictionaryItem;
import com.university.oms.model.DictionaryType;
import com.university.oms.repository.OmsRepository;
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

/**
 * 数据字典服务，管理字典类型和字典项的增删改查
 */
@Service
public class DictionaryService {
    /** 系统内置字典类型，不可停用 */
    private static final Set<String> SYSTEM_TYPES = new LinkedHashSet<String>(Arrays.asList(
            "business_status", "distribution_status", "biz_type",
            "flow_node", "role_key", "secrecy_level"
    ));

    private final OmsRepository repo;

    public DictionaryService(OmsRepository repo) {
        this.repo = repo;
    }

    /** 获取完整的字典目录（含所有类型和项） */
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

    /** 获取字典数据的版本号（基于最新更新时间） */
    public String version() {
        LocalDateTime latest = null;
        for (DictionaryType type : repo.findAllDictionaryTypes()) {
            latest = latest(latest, type.getUpdatedAt());
        }
        for (DictionaryItem item : repo.findAllDictionaryItems()) {
            latest = latest(latest, item.getUpdatedAt());
        }
        return latest == null ? "" : latest.toString();
    }

    /** 获取所有字典类型列表，按类型代码排序 */
    public List<DictionaryType> listTypes() {
        List<DictionaryType> types = new ArrayList<DictionaryType>(repo.findAllDictionaryTypes());
        types.sort(Comparator.comparing(DictionaryType::getDictType));
        return types;
    }

    /** 获取指定字典类型下的所有字典项 */
    public List<DictionaryItem> listItems(String dictType) {
        requireType(dictType);
        List<DictionaryItem> items = new ArrayList<DictionaryItem>(repo.findDictionaryItemsByType(dictType));
        items.sort(Comparator.comparing(DictionaryItem::getSortOrder)
                .thenComparing(DictionaryItem::getDictCode));
        return items;
    }

    /** 创建新的字典类型 */
    public DictionaryType createType(DictionaryTypeRequest request) {
        if (repo.findDictionaryTypeByType(request.getDictType()) != null) {
            throw new BusinessException("字典类型代码已存在");
        }
        DictionaryType type = new DictionaryType();
        OmsRepository.fillEntity(type, repo.nextId());
        type.setDictType(request.getDictType());
        type.setDictName(request.getDictName());
        type.setSystemType(false);
        type.setEnabled(request.isEnabled());
        type.setRemark(request.getRemark());
        type.setUpdatedAt(LocalDateTime.now());
        repo.saveDictionaryType(type);
        audit("create_type", type.getDictType(), type.getId());
        return type;
    }

    /** 更新字典类型信息 */
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
        repo.saveDictionaryType(type);
        audit("update_type", dictType, type.getId());
        return type;
    }

    /** 创建新的字典项 */
    public DictionaryItem createItem(String dictType, DictionaryItemRequest request) {
        requireType(dictType);
        if (request.getDictCode() == null || request.getDictCode().trim().isEmpty()) {
            throw new BusinessException("字典项代码不能为空");
        }
        if (repo.findDictionaryItemByTypeAndCode(dictType, request.getDictCode()) != null) {
            throw new BusinessException("字典项代码已存在");
        }
        DictionaryItem item = new DictionaryItem();
        OmsRepository.fillEntity(item, repo.nextId());
        item.setDictType(dictType);
        item.setDictCode(request.getDictCode());
        item.setDictLabel(request.getDictLabel());
        item.setSortOrder(sortOrder(request.getSortOrder()));
        item.setEnabled(request.isEnabled());
        item.setSystemItem(false);
        item.setRemark(request.getRemark());
        item.setUpdatedAt(LocalDateTime.now());
        repo.saveDictionaryItem(item);
        audit("create_item", dictType + "/" + item.getDictCode(), item.getId());
        return item;
    }

    /** 更新字典项信息 */
    public DictionaryItem updateItem(String dictType, String code, DictionaryItemRequest request) {
        requireType(dictType);
        DictionaryItem item = repo.findDictionaryItemByTypeAndCode(dictType, code);
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
        repo.saveDictionaryItem(item);
        audit("update_item", dictType + "/" + code, item.getId());
        return item;
    }

    /** 校验字典项是否启用，未启用则抛异常 */
    public void requireEnabled(String dictType, String code, String fieldLabel) {
        DictionaryItem item = repo.findDictionaryItemByTypeAndCode(dictType, code);
        if (item == null || !item.isEnabled()) {
            throw new BusinessException(fieldLabel + "不在可选字典范围内：" + code);
        }
    }

    /** 根据类型代码查询字典类型，不存在则抛异常 */
    private DictionaryType requireType(String dictType) {
        DictionaryType type = repo.findDictionaryTypeByType(dictType);
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

    /** 记录字典操作审计日志 */
    private void audit(String action, String detail, Long bizId) {
        AuditLog log = new AuditLog();
        OmsRepository.fillEntity(log, repo.nextId());
        log.setOperatorId(AuthContext.currentUserIdOr(0L));
        log.setModule("dictionary");
        log.setAction(action);
        log.setBizType("dictionary");
        log.setBizId(bizId);
        log.setDetail(detail);
        repo.saveAuditLog(log);
    }
}
