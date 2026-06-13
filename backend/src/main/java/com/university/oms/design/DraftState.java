package com.university.oms.design;

import com.university.oms.common.BusinessException;
import com.university.oms.model.User;

// 状态模式：草稿状态，不允许审批、驳回和撤回操作
public class DraftState implements BusinessState {
    @Override
    public String approve(User operator) {
        throw new BusinessException("草稿不能被审批，请先提交");
    }

    @Override
    public String reject(User operator) {
        throw new BusinessException("草稿不能被驳回");
    }

    @Override
    public String withdraw(Long operatorId, Long applicantId) {
        throw new BusinessException("草稿无需撤回");
    }
}
