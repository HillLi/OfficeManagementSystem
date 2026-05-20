package com.university.oms.design;

import com.university.oms.common.BusinessException;
import com.university.oms.model.User;

public class PendingState implements BusinessState {
    private final String requiredRole;
    private final String nextStatus;

    public PendingState(String requiredRole, String nextStatus) {
        this.requiredRole = requiredRole;
        this.nextStatus = nextStatus;
    }

    @Override
    public String approve(User operator) {
        if (!operator.getRoleKeys().contains(requiredRole) && !operator.getRoleKeys().contains("admin")) {
            throw new BusinessException("需要角色 " + requiredRole + " 审批");
        }
        return nextStatus;
    }

    @Override
    public String reject(User operator) {
        if (!operator.getRoleKeys().contains(requiredRole) && !operator.getRoleKeys().contains("admin")) {
            throw new BusinessException("需要角色 " + requiredRole + " 审批");
        }
        return "rejected";
    }

    @Override
    public String withdraw(Long operatorId, Long applicantId) {
        return "draft";
    }

    public String getRequiredRole() { return requiredRole; }
    public String getNextStatus() { return nextStatus; }
}
