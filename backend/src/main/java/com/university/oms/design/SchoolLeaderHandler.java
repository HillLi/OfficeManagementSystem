package com.university.oms.design;

import com.university.oms.common.BusinessException;
import com.university.oms.model.User;
import org.springframework.stereotype.Component;

// 责任链模式：校级领导审批处理器
@Component
public class SchoolLeaderHandler extends ApprovalHandler {
    // 处理校级领导待审批状态的审批请求
    @Override
    public String handle(String currentStatus, String action, User operator) {
        if ("pending_leader".equals(currentStatus)) {
            if (!operator.getRoleKeys().contains("school_leader") && !operator.getRoleKeys().contains("admin")) {
                throw new BusinessException("需要校级领导审批");
            }
            if ("reject".equals(action)) {
                return "rejected";
            }
            return "approved";
        }
        return next != null ? next.handle(currentStatus, action, operator) : "approved";
    }
}
