package com.university.oms.service;

import com.university.oms.common.BusinessException;
import com.university.oms.dto.CreateUserRequest;
import com.university.oms.dto.DeptRequest;
import com.university.oms.dto.UpdateUserRequest;
import com.university.oms.model.Department;
import com.university.oms.model.User;
import com.university.oms.repository.OmsRepository;
import com.university.oms.security.PasswordService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class UserManageService {
    private final OmsRepository repo;
    private final PasswordService passwordService;

    private static final Set<String> VALID_ROLES = new LinkedHashSet<>(Arrays.asList(
            "admin", "office_user", "dept_head", "school_leader",
            "office_admin", "finance_staff", "security_staff", "seal_keeper"
    ));

    public UserManageService(OmsRepository repo, PasswordService passwordService) {
        this.repo = repo;
        this.passwordService = passwordService;
    }

    // ========== User CRUD ==========

    public List<User> listUsers() {
        return repo.findAllUsers();
    }

    public User getUser(Long id) {
        User user = repo.findUserById(id);
        if (user == null) throw new BusinessException("用户不存在");
        return user;
    }

    public User createUser(CreateUserRequest request) {
        for (User u : repo.findAllUsers()) {
            if (u.getUsername().equals(request.getUsername())) {
                throw new BusinessException("用户名已存在");
            }
        }
        User user = new User();
        OmsRepository.fillEntity(user, repo.nextId());
        user.setUsername(request.getUsername());
        user.setPassword(passwordService.hash(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail());
        user.setDeptId(request.getDeptId());
        if (request.getDeptId() != null) {
            Department dept = repo.findDepartmentById(request.getDeptId());
            if (dept != null) {
                user.setDeptName(dept.getDeptName());
            }
        }
        parseRoleKeys(request.getRoleKeys(), user.getRoleKeys());
        repo.saveUser(user);
        return user;
    }

    public User updateUser(Long id, UpdateUserRequest request) {
        User user = getUser(id);
        if (request.getRealName() != null) user.setRealName(request.getRealName());
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordService.hash(request.getPassword()));
        }
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getDeptId() != null) {
            user.setDeptId(request.getDeptId());
            Department dept = repo.findDepartmentById(request.getDeptId());
            user.setDeptName(dept != null ? dept.getDeptName() : null);
        }
        if (request.getRoleKeys() != null) {
            user.getRoleKeys().clear();
            parseRoleKeys(request.getRoleKeys(), user.getRoleKeys());
        }
        user.setUpdatedAt(LocalDateTime.now());
        repo.saveUser(user);
        return user;
    }

    public void deleteUser(Long id) {
        User user = getUser(id);
        if ("admin".equals(user.getUsername())) {
            throw new BusinessException("不能删除系统管理员");
        }
        repo.deleteUser(id);
    }

    // ========== Department CRUD ==========

    public List<Department> listDepts() {
        return repo.findAllDepartments();
    }

    public Department createDept(DeptRequest request) {
        Department dept = new Department();
        OmsRepository.fillEntity(dept, repo.nextId());
        dept.setDeptName(request.getDeptName());
        dept.setParentId(request.getParentId() != null ? request.getParentId() : 0L);
        repo.saveDepartment(dept);
        return dept;
    }

    public Department updateDept(Long id, DeptRequest request) {
        Department dept = repo.findDepartmentById(id);
        if (dept == null) throw new BusinessException("部门不存在");
        dept.setDeptName(request.getDeptName());
        if (request.getParentId() != null) dept.setParentId(request.getParentId());
        dept.setUpdatedAt(LocalDateTime.now());
        for (User user : repo.findAllUsers()) {
            if (id.equals(user.getDeptId())) {
                user.setDeptName(dept.getDeptName());
            }
        }
        repo.saveDepartment(dept);
        return dept;
    }

    public void deleteDept(Long id) {
        for (User user : repo.findAllUsers()) {
            if (id.equals(user.getDeptId())) {
                throw new BusinessException("部门下还有用户，不能删除");
            }
        }
        repo.deleteDepartment(id);
    }

    // ========== Roles ==========

    public Set<String> listRoles() {
        return VALID_ROLES;
    }

    // ========== Helpers ==========

    private void parseRoleKeys(String raw, Set<String> target) {
        target.clear();
        if (raw == null || raw.trim().isEmpty()) return;
        for (String part : raw.split(",")) {
            String role = part.trim();
            if (VALID_ROLES.contains(role)) {
                target.add(role);
            }
        }
    }
}
