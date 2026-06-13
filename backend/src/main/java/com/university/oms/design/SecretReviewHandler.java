package com.university.oms.design;

import com.university.oms.common.BusinessException;
import com.university.oms.model.User;
import org.springframework.stereotype.Component;

// 责任链模式：保密审查处理器，用于报告类公文的保密审核
@Component
public class SecretReviewHandler extends ApprovalHandler {
    // 处理保密审查待审批状态的审批请求
    @Override
    public String handle(String currentStatus, String action, User operator) {
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
