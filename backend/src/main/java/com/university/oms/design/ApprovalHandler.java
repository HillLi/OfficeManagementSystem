package com.university.oms.design;

import com.university.oms.model.User;

/**
 * Chain of Responsibility pattern — abstract approval handler.
 */
public abstract class ApprovalHandler {
    protected ApprovalHandler next;

    public ApprovalHandler setNext(ApprovalHandler next) {
        this.next = next;
        return next;
    }

    public ApprovalHandler getNext() {
        return next;
    }

    /**
     * @return new status after handling, or delegates to next handler
     */
    public abstract String handle(String currentStatus, String action, User operator);
}
