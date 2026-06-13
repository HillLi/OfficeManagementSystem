package com.university.oms.design;

import com.university.oms.model.FlowNode;
import com.university.oms.repository.OmsRepository;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

// 审批流程配置：从数据库加载各业务类型的审批步骤顺序和状态与角色的映射关系
@Component
public class ApprovalFlowConfig {
    private final OmsRepository repo;
    // 流程Key -> 审批节点状态列表（按顺序）
    private final Map<String, List<String>> flows = new LinkedHashMap<>();
    // 流程Key -> (节点状态 -> 审批角色)
    private final Map<String, Map<String, String>> nodeRoles = new LinkedHashMap<>();
    // 节点状态 -> 显示名称
    private final Map<String, String> nodeLabels = new LinkedHashMap<>();

    public ApprovalFlowConfig(OmsRepository repo) {
        this.repo = repo;
    }

    // 启动时从数据库加载审批流程配置
    @PostConstruct
    public synchronized void init() {
        reload();
    }

    /** 重新从数据库加载配置（管理员修改后调用） */
    public synchronized void reload() {
        flows.clear();
        nodeRoles.clear();
        nodeLabels.clear();
        for (FlowNode node : repo.findAllFlowNodes()) {
            if (!node.isEnabled()) {
                continue;
            }
            flows.computeIfAbsent(node.getFlowKey(), k -> new ArrayList<>()).add(node.getNodeKey());
            nodeRoles.computeIfAbsent(node.getFlowKey(), k -> new LinkedHashMap<>())
                    .put(node.getNodeKey(), node.getRoleKey());
            if (node.getNodeLabel() != null) {
                nodeLabels.putIfAbsent(node.getNodeKey(), node.getNodeLabel());
            }
        }
    }

    /** 根据流程Key和待审批状态查询所需的审批角色 */
    public String getRequiredRole(String flowKey, String pendingStatus) {
        Map<String, String> roles = nodeRoles.get(flowKey);
        if (roles != null) {
            String role = roles.get(pendingStatus);
            if (role != null) {
                return role;
            }
        }
        // 兜底：跨流程查找该状态对应的角色
        for (Map<String, String> map : nodeRoles.values()) {
            if (map.containsKey(pendingStatus)) {
                return map.get(pendingStatus);
            }
        }
        return null;
    }

    /** 根据业务类型和当前状态获取下一个审批状态 */
    public String getNextStatus(String bizType, String currentStatus) {
        List<String> steps = flows.get(bizType);
        if (steps == null) return "approved";
        int idx = steps.indexOf(currentStatus);
        if (idx >= 0 && idx < steps.size() - 1) {
            return steps.get(idx + 1);
        }
        return null;
    }

    /** 获取节点显示名称 */
    public String getNodeLabel(String nodeKey) {
        return nodeLabels.get(nodeKey);
    }

    /** 获取全部流程配置（流程Key -> 节点状态列表） */
    public Map<String, List<String>> getFlows() {
        return flows;
    }
}
