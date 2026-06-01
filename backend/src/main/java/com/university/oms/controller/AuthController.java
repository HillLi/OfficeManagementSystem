package com.university.oms.controller;

import com.university.oms.common.ApiResponse;
import com.university.oms.dto.LoginRequest;
import com.university.oms.dto.LoginResult;
import com.university.oms.dto.UserOption;
import com.university.oms.model.Department;
import com.university.oms.model.User;
import com.university.oms.service.AuthService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResult> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        authService.logout(authorization);
        return ApiResponse.ok(null);
    }

    @GetMapping("/users")
    public ApiResponse<List<User>> users() {
        return ApiResponse.ok(authService.users());
    }

    @GetMapping("/user-options")
    public ApiResponse<List<UserOption>> userOptions() {
        return ApiResponse.ok(authService.userOptions());
    }

    @GetMapping("/dept-options")
    public ApiResponse<List<Department>> deptOptions() {
        return ApiResponse.ok(authService.deptOptions());
    }
}
