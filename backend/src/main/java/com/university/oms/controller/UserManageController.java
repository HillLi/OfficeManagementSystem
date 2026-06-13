package com.university.oms.controller;

import com.university.oms.common.ApiResponse;
import com.university.oms.dto.CreateUserRequest;
import com.university.oms.dto.DeptRequest;
import com.university.oms.dto.UpdateUserRequest;
import com.university.oms.model.Department;
import com.university.oms.model.User;
import com.university.oms.service.UserManageService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;

/** 用户与部门管理控制器，负责后台管理中的用户、部门和角色管理 */
@RestController
@RequestMapping("/api/admin")
public class UserManageController {
    private final UserManageService service;

    public UserManageController(UserManageService service) {
        this.service = service;
    }

    // ===== Users =====

    /** 查询用户列表 */
    @GetMapping("/users")
    public ApiResponse<List<User>> listUsers() {
        return ApiResponse.ok(service.listUsers());
    }

    /** 根据ID查询用户 */
    @GetMapping("/users/{id}")
    public ApiResponse<User> getUser(@PathVariable Long id) {
        return ApiResponse.ok(service.getUser(id));
    }

    /** 创建用户 */
    @PostMapping("/users")
    public ApiResponse<User> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ApiResponse.ok(service.createUser(request));
    }

    /** 更新用户信息 */
    @PutMapping("/users/{id}")
    public ApiResponse<User> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        return ApiResponse.ok(service.updateUser(id, request));
    }

    /** 删除用户 */
    @DeleteMapping("/users/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        service.deleteUser(id);
        return ApiResponse.ok(null);
    }

    // ===== Departments =====

    /** 查询部门列表 */
    @GetMapping("/depts")
    public ApiResponse<List<Department>> listDepts() {
        return ApiResponse.ok(service.listDepts());
    }

    /** 创建部门 */
    @PostMapping("/depts")
    public ApiResponse<Department> createDept(@Valid @RequestBody DeptRequest request) {
        return ApiResponse.ok(service.createDept(request));
    }

    /** 更新部门信息 */
    @PutMapping("/depts/{id}")
    public ApiResponse<Department> updateDept(@PathVariable Long id, @Valid @RequestBody DeptRequest request) {
        return ApiResponse.ok(service.updateDept(id, request));
    }

    /** 删除部门 */
    @DeleteMapping("/depts/{id}")
    public ApiResponse<Void> deleteDept(@PathVariable Long id) {
        service.deleteDept(id);
        return ApiResponse.ok(null);
    }

    // ===== Roles =====

    /** 查询角色列表 */
    @GetMapping("/roles")
    public ApiResponse<Set<String>> listRoles() {
        return ApiResponse.ok(service.listRoles());
    }
}
