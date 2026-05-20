package com.university.oms.design;

import com.university.oms.common.BusinessException;
import com.university.oms.model.User;

public class RejectedState implements BusinessState {
    @Override
    public String approve(User operator) {
        throw new BusinessException("已驳回，不能审批");
    }

    @Override
    public String reject(User operator) {
        throw new BusinessException("已驳回");
    }

    @Override
    public String withdraw(Long operatorId, Long applicantId) {
        if (operatorId.equals(applicantId)) {
            return "draft";
        }
        throw new BusinessException("只有申请人可以重新提交");
    }
}
