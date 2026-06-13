package com.university.oms.design;

// 观察者模式：业务状态变更监听器接口
public interface StatusChangeListener {
    // 当业务状态发生变更时触发
    void onStatusChange(String bizType, Long bizId, String oldStatus, String newStatus, Long operatorId);
}
