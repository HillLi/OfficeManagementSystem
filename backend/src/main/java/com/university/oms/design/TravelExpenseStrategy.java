package com.university.oms.design;

import com.university.oms.model.Travel;
import com.university.oms.model.TravelCheckResult;

public interface TravelExpenseStrategy {
    TravelCheckResult check(Travel travel);
}
