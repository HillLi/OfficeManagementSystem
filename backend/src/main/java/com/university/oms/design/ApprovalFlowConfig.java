package com.university.oms.design;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

// 审批流程配置：定义各业务类型的审批步骤顺序和状态与角色的映射关系
@Component
public class ApprovalFlowConfig {
    private final Map<String, List<String>> flows = new LinkedHashMap<>(); // 业务类型 -> 审批步骤列表
    private final Map<String, String> statusToRole = new LinkedHashMap<>(); // 审批状态 -> 所需角色

    // 初始化各业务类型的审批流程和状态角色映射
    @PostConstruct
    public void init() {
        flows.put("document", Arrays.asList("pending_dept", "pending_office", "pending_leader", "approved"));
        flows.put("seal_office", Arrays.asList("pending_office", "approved"));
        flows.put("seal_dept", Arrays.asList("pending_dept", "approved"));
        flows.put("seal_dept_major", Arrays.asList("pending_dept", "pending_office", "approved"));
        flows.put("seal_school_major", Arrays.asList("pending_office", "pending_leader", "approved"));
        flows.put("meeting", Arrays.asList("pending_dept", "approved"));
        flows.put("meeting_large", Arrays.asList("pending_security", "pending_dept", "pending_leader", "approved"));
        flows.put("travel", Arrays.asList("pending_dept", "pending_finance", "approved"));
        flows.put("report", Arrays.asList("pending_secret_review", "pending_dept", "pending_leader", "approved"));

        statusToRole.put("pending_dept", "dept_head");
        statusToRole.put("pending_office", "office_admin");
        statusToRole.put("pending_leader", "school_leader");
        statusToRole.put("pending_security", "security_staff");
        statusToRole.put("pending_finance", "finance_staff");
        statusToRole.put("pending_secret_review", "office_admin");
    }

    // 根据待审批状态查询所需的角色
    public String getRequiredRole(String pendingStatus) {
        return statusToRole.get(pendingStatus);
    }

    // 根据业务类型和当前状态获取下一个审批状态
    public String getNextStatus(String bizType, String currentStatus) {
        List<String> steps = flows.get(bizType);
        if (steps == null) return "approved";
        int idx = steps.indexOf(currentStatus);
        if (idx >= 0 && idx < steps.size() - 1) {
            return steps.get(idx + 1);
        }
        return null;
    }

    public Map<String, List<String>> getFlows() {
        return flows;
    }
}
