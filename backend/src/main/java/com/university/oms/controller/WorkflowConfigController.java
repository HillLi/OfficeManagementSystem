package com.university.oms.controller;

import com.university.oms.common.ApiResponse;
import com.university.oms.dto.FlowNodeRequest;
import com.university.oms.model.FlowNode;
import com.university.oms.service.WorkflowConfigService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/** 审批流程配置控制器（仅管理员可访问，由 /api/admin/ 前缀自动鉴权） */
@RestController
@RequestMapping("/api/admin/workflow")
public class WorkflowConfigController {
    private final WorkflowConfigService workflowConfigService;

    public WorkflowConfigController(WorkflowConfigService workflowConfigService) {
        this.workflowConfigService = workflowConfigService;
    }

    /** 查询所有可配置的流程Key及中文名 */
    @GetMapping("/flow-keys")
    public ApiResponse<Map<String, String>> flowKeys() {
        return ApiResponse.ok(workflowConfigService.flowKeyNames());
    }

    /** 查询所有审批流程节点（按流程Key分组） */
    @GetMapping("/nodes")
    public ApiResponse<Map<String, List<FlowNode>>> nodes() {
        return ApiResponse.ok(workflowConfigService.allNodesGroupedByFlow());
    }

    /** 查询指定流程Key的节点列表 */
    @GetMapping("/flow-key/{flowKey}")
    public ApiResponse<List<FlowNode>> nodesByFlowKey(@PathVariable String flowKey) {
        return ApiResponse.ok(workflowConfigService.nodesByFlowKey(flowKey));
    }

    /** 整体保存某流程的步骤（传入有序步骤数组） */
    @PutMapping("/flow-key/{flowKey}")
    public ApiResponse<Void> saveFlow(@PathVariable String flowKey,
                                      @Valid @RequestBody List<FlowNodeRequest> steps) {
        workflowConfigService.saveFlow(flowKey, steps);
        return ApiResponse.ok(null);
    }
}
