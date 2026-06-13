package com.university.oms.design;

import com.university.oms.common.BusinessException;
import org.springframework.stereotype.Component;

import java.util.*;

// 工厂模式：根据业务状态字符串创建对应的状态模式对象
@Component
public class StateFactory {
    private final ApprovalFlowConfig flowConfig;

    public StateFactory(ApprovalFlowConfig flowConfig) {
        this.flowConfig = flowConfig;
    }

    // 根据状态字符串创建状态对象（不含业务类型信息，角色按全局查找）
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
                    String role = flowConfig.getRequiredRole(null, status);
                    if (role == null) {
                        role = "admin";
                    }
                    return new PendingState(role, "approved");
                }
                throw new BusinessException("未知状态: " + status);
        }
    }

    // 根据业务类型和状态字符串创建状态对象，可确定下一个审批状态
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
                    String role = flowConfig.getRequiredRole(bizType, status);
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
