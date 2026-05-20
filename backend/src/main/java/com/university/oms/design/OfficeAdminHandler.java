package com.university.oms.design;

import com.university.oms.common.BusinessException;
import com.university.oms.model.User;
import org.springframework.stereotype.Component;

@Component
public class OfficeAdminHandler extends ApprovalHandler {
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
