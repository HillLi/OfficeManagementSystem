package com.university.oms.controller;

import com.university.oms.common.ApiResponse;
import com.university.oms.dto.DictionaryCatalogResponse;
import com.university.oms.dto.DictionaryItemRequest;
import com.university.oms.dto.DictionaryTypeRequest;
import com.university.oms.model.DictionaryItem;
import com.university.oms.model.DictionaryType;
import com.university.oms.service.DictionaryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/** 数据字典控制器，负责字典类型和字典项的增删改查 */
@RestController
public class DictionaryController {
    private final DictionaryService service;

    public DictionaryController(DictionaryService service) {
        this.service = service;
    }

    /** 获取字典目录（前端下拉框等使用） */
    @GetMapping("/api/dictionaries")
    public ApiResponse<DictionaryCatalogResponse> catalog() {
        return ApiResponse.ok(service.catalog());
    }

    /** 获取字典版本号，用于前端缓存判断 */
    @GetMapping("/api/dictionaries/version")
    public ApiResponse<String> version() {
        return ApiResponse.ok(service.version());
    }

    /** 查询所有字典类型 */
    @GetMapping("/api/admin/dictionaries/types")
    public ApiResponse<List<DictionaryType>> listTypes() {
        return ApiResponse.ok(service.listTypes());
    }

    /** 创建字典类型 */
    @PostMapping("/api/admin/dictionaries/types")
    public ApiResponse<DictionaryType> createType(@Valid @RequestBody DictionaryTypeRequest request) {
        return ApiResponse.ok(service.createType(request));
    }

    /** 更新字典类型 */
    @PutMapping("/api/admin/dictionaries/types/{dictType}")
    public ApiResponse<DictionaryType> updateType(@PathVariable String dictType,
                                                   @Valid @RequestBody DictionaryTypeRequest request) {
        return ApiResponse.ok(service.updateType(dictType, request));
    }

    /** 查询指定类型下的字典项列表 */
    @GetMapping("/api/admin/dictionaries/types/{dictType}/items")
    public ApiResponse<List<DictionaryItem>> listItems(@PathVariable String dictType) {
        return ApiResponse.ok(service.listItems(dictType));
    }

    /** 在指定类型下创建字典项 */
    @PostMapping("/api/admin/dictionaries/types/{dictType}/items")
    public ApiResponse<DictionaryItem> createItem(@PathVariable String dictType,
                                                   @Valid @RequestBody DictionaryItemRequest request) {
        return ApiResponse.ok(service.createItem(dictType, request));
    }

    /** 更新指定字典项 */
    @PutMapping("/api/admin/dictionaries/types/{dictType}/items/{code}")
    public ApiResponse<DictionaryItem> updateItem(@PathVariable String dictType,
                                                   @PathVariable String code,
                                                   @Valid @RequestBody DictionaryItemRequest request) {
        return ApiResponse.ok(service.updateItem(dictType, code, request));
    }
}
