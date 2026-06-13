package com.university.oms.service;

import com.university.oms.common.BusinessException;
import com.university.oms.design.ApprovalFlowConfig;
import com.university.oms.dto.FlowNodeRequest;
import com.university.oms.model.FlowNode;
import com.university.oms.repository.OmsRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 审批流程配置服务，供管理员查看和修改各业务的审批步骤
 */
@Service
public class WorkflowConfigService {
    /** 系统支持的审批角色集合 */
    private static final List<String> ALLOWED_ROLES = Arrays.asList(
            "dept_head", "office_admin", "school_leader", "security_staff", "finance_staff");
    /** 所有可配置的流程Key及中文名 */
    private static final Map<String, String> FLOW_KEY_NAMES = new LinkedHashMap<String, String>() {{
        put("document", "公文审批");
        put("seal_office", "用印审批-校级印章（普通）");
        put("seal_dept", "用印审批-部门印章（普通）");
        put("seal_dept_major", "用印审批-部门印章（重大事项）");
        put("seal_school_major", "用印审批-校级印章（重大事项）");
        put("meeting", "会议审批（普通）");
        put("meeting_large", "会议审批（大型活动）");
        put("travel", "差旅审批");
        put("report", "请示报告审批");
    }};

    private final OmsRepository repo;
    private final ApprovalFlowConfig flowConfig;

    public WorkflowConfigService(OmsRepository repo, ApprovalFlowConfig flowConfig) {
        this.repo = repo;
        this.flowConfig = flowConfig;
    }

    /** 获取所有可配置的流程Key及中文名 */
    public Map<String, String> flowKeyNames() {
        return FLOW_KEY_NAMES;
    }

    /** 获取所有审批流程节点，按流程Key分组返回 */
    public Map<String, List<FlowNode>> allNodesGroupedByFlow() {
        Map<String, List<FlowNode>> grouped = new LinkedHashMap<>();
        for (FlowNode node : repo.findAllFlowNodes()) {
            grouped.computeIfAbsent(node.getFlowKey(), k -> new ArrayList<>()).add(node);
        }
        return grouped;
    }

    /** 查询指定流程Key的节点列表 */
    public List<FlowNode> nodesByFlowKey(String flowKey) {
        requireValidFlowKey(flowKey);
        return repo.findFlowNodesByFlowKey(flowKey);
    }

    /**
     * 整体保存某流程的步骤（先删除原有节点，再按传入顺序写入）。
     * 保存后刷新流程配置缓存，使变更即时生效。
     */
    public void saveFlow(String flowKey, List<FlowNodeRequest> steps) {
        requireValidFlowKey(flowKey);
        if (steps == null || steps.isEmpty()) {
            throw new BusinessException("审批流程至少需要一个步骤");
        }
        // 校验每个步骤
        for (FlowNodeRequest step : steps) {
            if (step.getNodeKey() == null || !step.getNodeKey().startsWith("pending_")) {
                throw new BusinessException("节点状态必须以 pending_ 开头：" + step.getNodeKey());
            }
            if (!ALLOWED_ROLES.contains(step.getRoleKey())) {
                throw new BusinessException("不支持的角色：" + step.getRoleKey());
            }
        }
        // 校验节点状态不重复
        long distinctCount = steps.stream().map(FlowNodeRequest::getNodeKey).distinct().count();
        if (distinctCount != steps.size()) {
            throw new BusinessException("同一流程内节点状态不能重复");
        }

        repo.deleteFlowNodesByFlowKey(flowKey);
        int order = 1;
        for (FlowNodeRequest step : steps) {
            FlowNode node = new FlowNode();
            OmsRepository.fillEntity(node, repo.nextId());
            node.setFlowKey(flowKey);
            node.setSortOrder(order++);
            node.setNodeKey(step.getNodeKey());
            node.setNodeLabel(step.getNodeLabel());
            node.setRoleKey(step.getRoleKey());
            node.setEnabled(true);
            repo.saveFlowNode(node);
        }
        // 刷新缓存，使配置变更即时生效
        flowConfig.reload();
    }

    private void requireValidFlowKey(String flowKey) {
        if (!FLOW_KEY_NAMES.containsKey(flowKey)) {
            throw new BusinessException("不支持的流程Key：" + flowKey);
        }
    }
}
