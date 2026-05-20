package com.university.oms.design;

import com.university.oms.common.BusinessException;
import com.university.oms.model.User;
import org.springframework.stereotype.Component;

@Component
public class DeptHeadHandler extends ApprovalHandler {
    @Override
    public String handle(String currentStatus, String action, User operator) {
        if ("pending_dept".equals(currentStatus)) {
            if (!operator.getRoleKeys().contains("dept_head") && !operator.getRoleKeys().contains("admin")) {
                throw new BusinessException("需要部门负责人审批");
            }
            if ("reject".equals(action)) {
                return "rejected";
            }
            // Return next status in the flow; chain factory decides actual next
            return "pending_office";
        }
        return next != null ? next.handle(currentStatus, action, operator) : "approved";
    }
}
