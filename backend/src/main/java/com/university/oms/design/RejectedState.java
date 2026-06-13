package com.university.oms.design;

import com.university.oms.common.BusinessException;
import com.university.oms.model.User;

// 状态模式：已驳回状态，仅允许申请人撤回后重新编辑
public class RejectedState implements BusinessState {
    @Override
    public String approve(User operator) {
        throw new BusinessException("已驳回，不能审批");
    }

    @Override
    public String reject(User operator) {
        throw new BusinessException("已驳回");
    }

    // 仅允许申请人撤回，撤回后回到草稿状态
    @Override
    public String withdraw(Long operatorId, Long applicantId) {
        if (operatorId.equals(applicantId)) {
            return "draft";
        }
        throw new BusinessException("只有申请人可以重新提交");
    }
}
