package com.university.oms.design;

import com.university.oms.model.Travel;
import com.university.oms.model.TravelCheckResult;

// 策略模式：差旅费用校验策略接口
public interface TravelExpenseStrategy {
    // 根据差旅信息校验费用是否符合标准
    TravelCheckResult check(Travel travel);
}
