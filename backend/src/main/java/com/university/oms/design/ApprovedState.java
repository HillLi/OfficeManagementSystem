package com.university.oms.design;

import com.university.oms.common.BusinessException;
import com.university.oms.model.User;

public class ApprovedState implements BusinessState {
    @Override
    public String approve(User operator) {
        throw new BusinessException("已审批完成，不能重复审批");
    }

    @Override
    public String reject(User operator) {
        throw new BusinessException("已审批完成，不能驳回");
    }

    @Override
    public String withdraw(Long operatorId, Long applicantId) {
        throw new BusinessException("已审批完成，不能撤回");
    }
}
