package com.university.oms.controller;

import com.university.oms.common.ApiResponse;
import com.university.oms.dto.ChangePasswordRequest;
import com.university.oms.dto.LoginRequest;
import com.university.oms.dto.LoginResult;
import com.university.oms.dto.UserOption;
import com.university.oms.model.Department;
import com.university.oms.model.User;
import com.university.oms.service.AuthService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/** 认证控制器，负责登录登出和用户、部门选项查询 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** 用户登录 */
    @PostMapping("/login")
    public ApiResponse<LoginResult> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    /** 用户修改密码 */
    @PostMapping("/change-password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return ApiResponse.ok(null);
    }

    /** 用户登出 */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        authService.logout(authorization);
        return ApiResponse.ok(null);
    }

    /** 查询所有用户列表 */
    @GetMapping("/users")
    public ApiResponse<List<User>> users() {
        return ApiResponse.ok(authService.users());
    }

    /** 查询用户选项列表（用于下拉框） */
    @GetMapping("/user-options")
    public ApiResponse<List<UserOption>> userOptions() {
        return ApiResponse.ok(authService.userOptions());
    }

    /** 查询部门选项列表（用于下拉框） */
    @GetMapping("/dept-options")
    public ApiResponse<List<Department>> deptOptions() {
        return ApiResponse.ok(authService.deptOptions());
    }
}
