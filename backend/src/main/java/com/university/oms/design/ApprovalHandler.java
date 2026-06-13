package com.university.oms.design;

import com.university.oms.model.User;

// 责任链模式：抽象审批处理器，支持链式调用
public abstract class ApprovalHandler {
    protected ApprovalHandler next; // 链中的下一个处理器

    // 设置下一个处理器并返回它，便于链式配置
    public ApprovalHandler setNext(ApprovalHandler next) {
        this.next = next;
        return next;
    }

    // 获取下一个处理器
    public ApprovalHandler getNext() {
        return next;
    }

    // 处理审批请求，返回新状态；若当前处理器不匹配则委托给下一个处理器
    public abstract String handle(String currentStatus, String action, User operator);
}
