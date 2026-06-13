package com.university.oms.design;

import com.university.oms.common.BusinessException;

// 模板方法模式：业务提交工作流的抽象基类，定义提交的标准流程骨架
public abstract class AbstractBusinessService<T> {
    // 提交业务的标准流程：校验 -> 执行提交 -> 提交后处理
    public final T submit(T entity, Long applicantId) {
        validate(entity);
        T prepared = doSubmit(entity, applicantId);
        postSubmit(prepared);
        return prepared;
    }

    // 校验业务实体，子类必须实现
    protected abstract void validate(T entity);

    // 执行实际的提交逻辑，子类必须实现
    protected abstract T doSubmit(T entity, Long applicantId);

    // 提交后的钩子方法，子类可选覆盖
    protected void postSubmit(T entity) {
        // default: no-op
    }
}
