package com.university.oms.design;

import com.university.oms.model.User;

// 状态模式：业务状态接口，封装不同状态下审批、驳回、撤回的行为
public interface BusinessState {
    // 执行审批操作，返回新状态
    String approve(User operator);
    // 执行驳回操作，返回新状态
    String reject(User operator);
    // 执行撤回操作，返回新状态
    String withdraw(Long operatorId, Long applicantId);
}
