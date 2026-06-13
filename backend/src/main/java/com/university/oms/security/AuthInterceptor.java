package com.university.oms.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.university.oms.common.ApiResponse;
import com.university.oms.model.User;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;

/**
 * 认证拦截器，校验请求中的 Token 并管理用户权限
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final AuthTokenService tokenService;
    private final ObjectMapper objectMapper;

    public AuthInterceptor(AuthTokenService tokenService, ObjectMapper objectMapper) {
        this.tokenService = tokenService;
        this.objectMapper = objectMapper;
    }

    /** 请求前置处理：校验登录状态和管理员权限 */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        // 放行登录接口
        String path = request.getRequestURI();
        if ("/api/auth/login".equals(path)) {
            return true;
        }
        // 放行非API路径（静态资源等）
        if (!path.startsWith("/api/")) {
            return true;
        }

        // 解析 Token 获取用户信息
        User user = tokenService.resolve(extractToken(request));
        if (user == null) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "用户未登录或登录已失效");
            return false;
        }
        // 校验管理员权限
        if ((path.startsWith("/api/admin/") || "/api/auth/users".equals(path) || "/api/workflow/audit-logs".equals(path))
                && !user.getRoleKeys().contains("admin")) {
            writeError(response, HttpServletResponse.SC_FORBIDDEN, "无管理员权限");
            return false;
        }
        // 将用户信息存入上下文
        AuthContext.set(user);
        return true;
    }

    /** 请求完成后清理 ThreadLocal，防止内存泄漏 */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AuthContext.clear();
    }

    /** 从请求头中提取 Token */
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null) {
            return null;
        }
        if (header.startsWith("Bearer ")) {
            return header.substring("Bearer ".length()).trim();
        }
        return header.trim();
    }

    /** 向响应中写入错误信息 */
    private void writeError(HttpServletResponse response, int status, String message) throws Exception {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.fail(message)));
    }
}
