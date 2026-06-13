package com.university.oms.common;

/**
 * 业务逻辑异常，用于表示业务规则校验失败等可预期的错误
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
