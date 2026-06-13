package com.university.oms.design;

import com.university.oms.common.BusinessException;
import com.university.oms.model.User;
import org.springframework.stereotype.Component;

// 责任链模式：财务人员审核处理器，用于差旅费用审批
@Component
public class FinanceHandler extends ApprovalHandler {
    // 处理财务待审批状态的审批请求
    @Override
    public String handle(String currentStatus, String action, User operator) {
        if ("pending_finance".equals(currentStatus)) {
            if (!operator.getRoleKeys().contains("finance_staff") && !operator.getRoleKeys().contains("admin")) {
                throw new BusinessException("需要财务人员审核费用");
            }
            if ("reject".equals(action)) {
                return "rejected";
            }
            return "approved";
        }
        return next != null ? next.handle(currentStatus, action, operator) : "approved";
    }
}
