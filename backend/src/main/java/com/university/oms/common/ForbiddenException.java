package com.university.oms.common;

/**
 * 权限不足异常，用于表示用户没有访问资源的权限
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
