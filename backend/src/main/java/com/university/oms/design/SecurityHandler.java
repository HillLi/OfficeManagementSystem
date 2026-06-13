package com.university.oms.design;

import com.university.oms.common.BusinessException;
import com.university.oms.model.User;
import org.springframework.stereotype.Component;

// 责任链模式：保卫处安全审批处理器，用于大型活动安全审核
@Component
public class SecurityHandler extends ApprovalHandler {
    // 处理保卫处待审批状态的审批请求
    @Override
    public String handle(String currentStatus, String action, User operator) {
        if ("pending_security".equals(currentStatus)) {
            if (!operator.getRoleKeys().contains("security_staff") && !operator.getRoleKeys().contains("admin")) {
                throw new BusinessException("需要保卫人员审批大型活动安全");
            }
            if ("reject".equals(action)) {
                return "rejected";
            }
            return "pending_dept";
        }
        return next != null ? next.handle(currentStatus, action, operator) : "approved";
    }
}
