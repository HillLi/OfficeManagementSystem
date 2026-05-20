package com.university.oms.design;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Observer — notifies the next approver when a business enters their pending state.
 */
@Component
public class NextApproverNotificationListener implements StatusChangeListener {
    private static final Logger log = LoggerFactory.getLogger(NextApproverNotificationListener.class);

    @Override
    public void onStatusChange(String bizType, Long bizId, String oldStatus, String newStatus, Long operatorId) {
        if (newStatus != null && newStatus.startsWith("pending_")) {
            log.info("[通知] 待办提醒：业务类型={}, 业务ID={}, 当前状态={}, 请相关审批人及时处理", bizType, bizId, newStatus);
        }
    }
}
