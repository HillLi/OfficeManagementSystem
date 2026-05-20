package com.university.oms.design;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Observer — notifies the applicant when their business status changes.
 */
@Component
public class ApplicantNotificationListener implements StatusChangeListener {
    private static final Logger log = LoggerFactory.getLogger(ApplicantNotificationListener.class);

    @Override
    public void onStatusChange(String bizType, Long bizId, String oldStatus, String newStatus, Long operatorId) {
        log.info("[通知] 业务类型={}, 业务ID={}, 状态从{}变为{}", bizType, bizId, oldStatus, newStatus);
    }
}
