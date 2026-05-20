package com.university.oms.design;

import com.university.oms.common.BusinessException;

/**
 * Template Method pattern — defines skeleton for business submission workflow.
 */
public abstract class AbstractBusinessService<T> {
    public final T submit(T entity, Long applicantId) {
        validate(entity);
        T prepared = doSubmit(entity, applicantId);
        postSubmit(prepared);
        return prepared;
    }

    protected abstract void validate(T entity);

    protected abstract T doSubmit(T entity, Long applicantId);

    protected void postSubmit(T entity) {
        // default: no-op
    }
}
