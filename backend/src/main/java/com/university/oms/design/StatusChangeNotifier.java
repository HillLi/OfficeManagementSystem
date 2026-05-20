package com.university.oms.design;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Observer pattern — notifies all registered listeners when status changes.
 */
@Component
public class StatusChangeNotifier {
    private final List<StatusChangeListener> listeners = new CopyOnWriteArrayList<>();
    private final List<StatusChangeListener> springListeners;

    public StatusChangeNotifier(List<StatusChangeListener> springListeners) {
        this.springListeners = springListeners;
    }

    @PostConstruct
    public void init() {
        listeners.addAll(springListeners);
    }

    public void addListener(StatusChangeListener listener) {
        listeners.add(listener);
    }

    public void notify(String bizType, Long bizId, String oldStatus, String newStatus, Long operatorId) {
        for (StatusChangeListener listener : listeners) {
            listener.onStatusChange(bizType, bizId, oldStatus, newStatus, operatorId);
        }
    }

    public List<StatusChangeListener> getListeners() {
        return listeners;
    }
}
