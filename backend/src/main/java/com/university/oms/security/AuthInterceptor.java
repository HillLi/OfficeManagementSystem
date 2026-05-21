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

@Component
public class AuthInterceptor implements HandlerInterceptor {
    private final AuthTokenService tokenService;
    private final ObjectMapper objectMapper;

    public AuthInterceptor(AuthTokenService tokenService, ObjectMapper objectMapper) {
        this.tokenService = tokenService;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String path = request.getRequestURI();
        if ("/api/auth/login".equals(path)) {
            return true;
        }
        if (!path.startsWith("/api/")) {
            return true;
        }

        User user = tokenService.resolve(extractToken(request));
        if (user == null) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "用户未登录或登录已失效");
            return false;
        }
        if ((path.startsWith("/api/admin/") || "/api/auth/users".equals(path) || "/api/workflow/audit-logs".equals(path))
                && !user.getRoleKeys().contains("admin")) {
            writeError(response, HttpServletResponse.SC_FORBIDDEN, "无管理员权限");
            return false;
        }
        AuthContext.set(user);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AuthContext.clear();
    }

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

    private void writeError(HttpServletResponse response, int status, String message) throws Exception {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.fail(message)));
    }
}
