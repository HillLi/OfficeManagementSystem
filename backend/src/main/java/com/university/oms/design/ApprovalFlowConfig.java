package com.university.oms.design;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * Defines per-business-type approval step sequences and role mappings.
 */
@Component
public class ApprovalFlowConfig {
    private final Map<String, List<String>> flows = new LinkedHashMap<>();
    private final Map<String, String> statusToRole = new LinkedHashMap<>();

    @PostConstruct
    public void init() {
        flows.put("document", Arrays.asList("pending_dept", "pending_office", "pending_leader", "approved"));
        flows.put("seal_office", Arrays.asList("pending_office", "approved"));
        flows.put("seal_dept", Arrays.asList("pending_dept", "approved"));
        flows.put("meeting", Arrays.asList("pending_dept", "approved"));
        flows.put("meeting_large", Arrays.asList("pending_security", "pending_dept", "approved"));
        flows.put("travel", Arrays.asList("pending_dept", "pending_finance", "approved"));
        flows.put("report", Arrays.asList("pending_secret_review", "pending_dept", "approved"));

        statusToRole.put("pending_dept", "dept_head");
        statusToRole.put("pending_office", "office_admin");
        statusToRole.put("pending_leader", "school_leader");
        statusToRole.put("pending_security", "security_staff");
        statusToRole.put("pending_finance", "finance_staff");
        statusToRole.put("pending_secret_review", "office_admin");
    }

    public String getRequiredRole(String pendingStatus) {
        return statusToRole.get(pendingStatus);
    }

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
