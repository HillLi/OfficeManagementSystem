package com.university.oms.design;

/**
 * Observer pattern — listens for business status changes.
 */
public interface StatusChangeListener {
    void onStatusChange(String bizType, Long bizId, String oldStatus, String newStatus, Long operatorId);
}
