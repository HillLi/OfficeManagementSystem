package com.university.oms.security;

import com.university.oms.common.BusinessException;
import com.university.oms.model.User;

public final class AuthContext {
    private static final ThreadLocal<User> CURRENT_USER = new ThreadLocal<User>();

    private AuthContext() {
    }

    public static void set(User user) {
        CURRENT_USER.set(user);
    }

    public static User currentUser() {
        return CURRENT_USER.get();
    }

    public static Long currentUserIdOr(Long fallback) {
        User user = currentUser();
        return user != null ? user.getId() : fallback;
    }

    public static User requireUser() {
        User user = currentUser();
        if (user == null) {
            throw new BusinessException("用户未登录");
        }
        return user;
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}
