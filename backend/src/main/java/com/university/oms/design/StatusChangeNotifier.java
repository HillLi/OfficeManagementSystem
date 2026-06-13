package com.university.oms.design;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

// 观察者模式：状态变更通知器，管理并通知所有监听器
@Component
public class StatusChangeNotifier {
    private final List<StatusChangeListener> listeners = new CopyOnWriteArrayList<>(); // 注册的监听器列表
    private final List<StatusChangeListener> springListeners; // Spring自动注入的监听器

    public StatusChangeNotifier(List<StatusChangeListener> springListeners) {
        this.springListeners = springListeners;
    }

    // 初始化时将Spring注入的监听器注册到列表中
    @PostConstruct
    public void init() {
        listeners.addAll(springListeners);
    }

    // 动态注册监听器
    public void addListener(StatusChangeListener listener) {
        listeners.add(listener);
    }

    // 通知所有监听器状态已变更
    public void notify(String bizType, Long bizId, String oldStatus, String newStatus, Long operatorId) {
        for (StatusChangeListener listener : listeners) {
            listener.onStatusChange(bizType, bizId, oldStatus, newStatus, operatorId);
        }
    }

    public List<StatusChangeListener> getListeners() {
        return listeners;
    }
}
