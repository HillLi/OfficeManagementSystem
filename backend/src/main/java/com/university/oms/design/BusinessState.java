package com.university.oms.design;

import com.university.oms.model.User;

/**
 * State pattern — encapsulates behavior for each business status.
 */
public interface BusinessState {
    String approve(User operator);
    String reject(User operator);
    String withdraw(Long operatorId, Long applicantId);
}
