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

@RestController
@RequestMapping("/api/admin")
public class UserManageController {
    private final UserManageService service;

    public UserManageController(UserManageService service) {
        this.service = service;
    }

    // ===== Users =====

    @GetMapping("/users")
    public ApiResponse<List<User>> listUsers() {
        return ApiResponse.ok(service.listUsers());
    }

    @GetMapping("/users/{id}")
    public ApiResponse<User> getUser(@PathVariable Long id) {
        return ApiResponse.ok(service.getUser(id));
    }

    @PostMapping("/users")
    public ApiResponse<User> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ApiResponse.ok(service.createUser(request));
    }

    @PutMapping("/users/{id}")
    public ApiResponse<User> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        return ApiResponse.ok(service.updateUser(id, request));
    }

    @DeleteMapping("/users/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        service.deleteUser(id);
        return ApiResponse.ok(null);
    }

    // ===== Departments =====

    @GetMapping("/depts")
    public ApiResponse<List<Department>> listDepts() {
        return ApiResponse.ok(service.listDepts());
    }

    @PostMapping("/depts")
    public ApiResponse<Department> createDept(@Valid @RequestBody DeptRequest request) {
        return ApiResponse.ok(service.createDept(request));
    }

    @PutMapping("/depts/{id}")
    public ApiResponse<Department> updateDept(@PathVariable Long id, @Valid @RequestBody DeptRequest request) {
        return ApiResponse.ok(service.updateDept(id, request));
    }

    @DeleteMapping("/depts/{id}")
    public ApiResponse<Void> deleteDept(@PathVariable Long id) {
        service.deleteDept(id);
        return ApiResponse.ok(null);
    }

    // ===== Roles =====

    @GetMapping("/roles")
    public ApiResponse<Set<String>> listRoles() {
        return ApiResponse.ok(service.listRoles());
    }
}
