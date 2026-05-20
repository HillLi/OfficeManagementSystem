package com.university.oms.design;

import com.university.oms.common.BusinessException;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * State pattern — creates state objects based on current business status.
 * Uses ApprovalFlowConfig to determine the next status and required role.
 */
@Component
public class StateFactory {
    private final ApprovalFlowConfig flowConfig;

    public StateFactory(ApprovalFlowConfig flowConfig) {
        this.flowConfig = flowConfig;
    }

    public BusinessState getState(String status) {
        if (status == null) {
            throw new BusinessException("状态不能为空");
        }
        switch (status) {
            case "draft":
                return new DraftState();
            case "approved":
                return new ApprovedState();
            case "rejected":
                return new RejectedState();
            default:
                if (status.startsWith("pending_")) {
                    String role = flowConfig.getRequiredRole(status);
                    if (role == null) {
                        role = "admin";
                    }
                    return new PendingState(role, "approved");
                }
                throw new BusinessException("未知状态: " + status);
        }
    }

    public BusinessState getState(String bizType, String status) {
        if (status == null) {
            throw new BusinessException("状态不能为空");
        }
        switch (status) {
            case "draft":
                return new DraftState();
            case "approved":
                return new ApprovedState();
            case "rejected":
                return new RejectedState();
            default:
                if (status.startsWith("pending_")) {
                    String role = flowConfig.getRequiredRole(status);
                    if (role == null) {
                        role = "admin";
                    }
                    String next = flowConfig.getNextStatus(bizType, status);
                    return new PendingState(role, next != null ? next : "approved");
                }
                throw new BusinessException("未知状态: " + status);
        }
    }
}
