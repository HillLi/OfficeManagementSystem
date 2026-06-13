package com.university.oms.dto;

import com.university.oms.model.User;

/**
 * 登录成功后返回的结果（包含令牌和用户信息）
 */
public class LoginResult {
    /** JWT 认证令牌 */
    private String token;
    /** 登录用户信息 */
    private User user;

    public LoginResult(String token, User user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public User getUser() {
        return user;
    }
}
