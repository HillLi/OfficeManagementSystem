package com.university.oms.controller;

import com.university.oms.common.ApiResponse;
import com.university.oms.dto.OrgTreeNode;
import com.university.oms.service.OrgTreeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 组织架构控制器，负责返回组织架构树 */
@RestController
@RequestMapping("/api/org")
public class OrgController {
    private final OrgTreeService service;

    public OrgController(OrgTreeService service) {
        this.service = service;
    }

    /** 获取组织架构树 */
    @GetMapping("/tree")
    public ApiResponse<List<OrgTreeNode>> tree() {
        return ApiResponse.ok(service.buildTree());
    }
}
