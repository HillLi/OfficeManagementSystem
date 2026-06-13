package com.university.oms.design;

import com.university.oms.common.BusinessException;
import com.university.oms.model.User;
import org.springframework.stereotype.Component;

// 责任链模式：党办校办人员审批处理器
@Component
public class OfficeAdminHandler extends ApprovalHandler {
    // 处理党办校办待审批状态和保密审查状态的审批请求
    @Override
    public String handle(String currentStatus, String action, User operator) {
        if ("pending_office".equals(currentStatus)) {
            if (!operator.getRoleKeys().contains("office_admin") && !operator.getRoleKeys().contains("admin")) {
                throw new BusinessException("需要党办校办人员审批");
            }
            if ("reject".equals(action)) {
                return "rejected";
            }
            return "pending_leader";
        }
        if ("pending_secret_review".equals(currentStatus)) {
            if (!operator.getRoleKeys().contains("office_admin") && !operator.getRoleKeys().contains("admin")) {
                throw new BusinessException("需要党办校办人员进行保密审查");
            }
            if ("reject".equals(action)) {
                return "rejected";
            }
            return "pending_dept";
        }
        return next != null ? next.handle(currentStatus, action, operator) : "approved";
    }
}
