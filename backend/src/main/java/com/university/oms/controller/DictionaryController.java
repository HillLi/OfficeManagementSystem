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

@RestController
public class DictionaryController {
    private final DictionaryService service;

    public DictionaryController(DictionaryService service) {
        this.service = service;
    }

    @GetMapping("/api/dictionaries")
    public ApiResponse<DictionaryCatalogResponse> catalog() {
        return ApiResponse.ok(service.catalog());
    }

    @GetMapping("/api/dictionaries/version")
    public ApiResponse<String> version() {
        return ApiResponse.ok(service.version());
    }

    @GetMapping("/api/admin/dictionaries/types")
    public ApiResponse<List<DictionaryType>> listTypes() {
        return ApiResponse.ok(service.listTypes());
    }

    @PostMapping("/api/admin/dictionaries/types")
    public ApiResponse<DictionaryType> createType(@Valid @RequestBody DictionaryTypeRequest request) {
        return ApiResponse.ok(service.createType(request));
    }

    @PutMapping("/api/admin/dictionaries/types/{dictType}")
    public ApiResponse<DictionaryType> updateType(@PathVariable String dictType,
                                                   @Valid @RequestBody DictionaryTypeRequest request) {
        return ApiResponse.ok(service.updateType(dictType, request));
    }

    @GetMapping("/api/admin/dictionaries/types/{dictType}/items")
    public ApiResponse<List<DictionaryItem>> listItems(@PathVariable String dictType) {
        return ApiResponse.ok(service.listItems(dictType));
    }

    @PostMapping("/api/admin/dictionaries/types/{dictType}/items")
    public ApiResponse<DictionaryItem> createItem(@PathVariable String dictType,
                                                   @Valid @RequestBody DictionaryItemRequest request) {
        return ApiResponse.ok(service.createItem(dictType, request));
    }

    @PutMapping("/api/admin/dictionaries/types/{dictType}/items/{code}")
    public ApiResponse<DictionaryItem> updateItem(@PathVariable String dictType,
                                                   @PathVariable String code,
                                                   @Valid @RequestBody DictionaryItemRequest request) {
        return ApiResponse.ok(service.updateItem(dictType, code, request));
    }
}
