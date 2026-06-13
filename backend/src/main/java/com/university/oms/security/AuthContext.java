package com.university.oms.security;

import com.university.oms.common.BusinessException;
import com.university.oms.model.User;

/**
 * 认证上下文，基于 ThreadLocal 存储当前线程的登录用户信息
 */
public final class AuthContext {

    /** 当前线程绑定的用户对象 */
    private static final ThreadLocal<User> CURRENT_USER = new ThreadLocal<User>();

    private AuthContext() {
    }

    /** 设置当前线程的登录用户 */
    public static void set(User user) {
        CURRENT_USER.set(user);
    }

    /** 获取当前登录用户，未登录时返回 null */
    public static User currentUser() {
        return CURRENT_USER.get();
    }

    /** 获取当前用户ID，未登录时返回备用值 */
    public static Long currentUserIdOr(Long fallback) {
        User user = currentUser();
        return user != null ? user.getId() : fallback;
    }

    /** 获取当前登录用户，未登录时抛出业务异常 */
    public static User requireUser() {
        User user = currentUser();
        if (user == null) {
            throw new BusinessException("用户未登录");
        }
        return user;
    }

    /** 清除当前线程的用户信息，防止内存泄漏 */
    public static void clear() {
        CURRENT_USER.remove();
    }
}
