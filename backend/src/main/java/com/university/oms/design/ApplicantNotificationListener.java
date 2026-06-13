package com.university.oms.design;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

// 观察者模式：申请人通知监听器，当业务状态变更时通知申请人
@Component
public class ApplicantNotificationListener implements StatusChangeListener {
    private static final Logger log = LoggerFactory.getLogger(ApplicantNotificationListener.class);

    @Override
    public void onStatusChange(String bizType, Long bizId, String oldStatus, String newStatus, Long operatorId) {
        log.info("[通知] 业务类型={}, 业务ID={}, 状态从{}变为{}", bizType, bizId, oldStatus, newStatus);
    }
}
