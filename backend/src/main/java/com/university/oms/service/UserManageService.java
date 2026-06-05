package com.university.oms.service;

import com.university.oms.common.BusinessException;
import com.university.oms.dto.CreateUserRequest;
import com.university.oms.dto.DeptRequest;
import com.university.oms.dto.UpdateUserRequest;
import com.university.oms.model.Department;
import com.university.oms.model.User;
import com.university.oms.repository.DataPersistence;
import com.university.oms.repository.InMemoryDatabase;
import com.university.oms.security.PasswordService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class UserManageService {
    private final InMemoryDatabase db;
    private final DataPersistence persistence;
    private final PasswordService passwordService;

    private static final Set<String> VALID_ROLES = new LinkedHashSet<>(Arrays.asList(
            "admin", "office_user", "dept_head", "school_leader",
            "office_admin", "finance_staff", "security_staff", "seal_keeper"
    ));

    public UserManageService(InMemoryDatabase db, DataPersistence persistence, PasswordService passwordService) {
        this.db = db;
        this.persistence = persistence;
        this.passwordService = passwordService;
    }

    // ========== User CRUD ==========

    public List<User> listUsers() {
        return new ArrayList<>(db.users().values());
    }

    public User getUser(Long id) {
        User user = db.users().get(id);
        if (user == null) throw new BusinessException("用户不存在");
        return user;
    }

    public User createUser(CreateUserRequest request) {
        for (User u : db.users().values()) {
            if (u.getUsername().equals(request.getUsername())) {
                throw new BusinessException("用户名已存在");
            }
        }
        User user = new User();
        db.fill(user, db.nextId());
        user.setUsername(request.getUsername());
        user.setPassword(passwordService.hash(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail());
        user.setDeptId(request.getDeptId());
        if (request.getDeptId() != null) {
            Department dept = db.departments().get(request.getDeptId());
            if (dept != null) {
                user.setDeptName(dept.getDeptName());
            }
        }
        parseRoleKeys(request.getRoleKeys(), user.getRoleKeys());
        db.users().put(user.getId(), user);
        persistence.saveUser(user);
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
            Department dept = db.departments().get(request.getDeptId());
            user.setDeptName(dept != null ? dept.getDeptName() : null);
        }
        if (request.getRoleKeys() != null) {
            user.getRoleKeys().clear();
            parseRoleKeys(request.getRoleKeys(), user.getRoleKeys());
        }
        user.setUpdatedAt(LocalDateTime.now());
        persistence.saveUser(user);
        return user;
    }

    public void deleteUser(Long id) {
        User user = getUser(id);
        if ("admin".equals(user.getUsername())) {
            throw new BusinessException("不能删除系统管理员");
        }
        db.users().remove(id);
        persistence.deleteUser(id);
    }

    // ========== Department CRUD ==========

    public List<Department> listDepts() {
        return new ArrayList<>(db.departments().values());
    }

    public Department createDept(DeptRequest request) {
        Department dept = new Department();
        db.fill(dept, db.nextId());
        dept.setDeptName(request.getDeptName());
        dept.setParentId(request.getParentId() != null ? request.getParentId() : 0L);
        db.departments().put(dept.getId(), dept);
        persistence.saveDepartment(dept);
        return dept;
    }

    public Department updateDept(Long id, DeptRequest request) {
        Department dept = db.departments().get(id);
        if (dept == null) throw new BusinessException("部门不存在");
        dept.setDeptName(request.getDeptName());
        if (request.getParentId() != null) dept.setParentId(request.getParentId());
        dept.setUpdatedAt(LocalDateTime.now());
        for (User user : db.users().values()) {
            if (id.equals(user.getDeptId())) {
                user.setDeptName(dept.getDeptName());
            }
        }
        persistence.saveDepartment(dept);
        return dept;
    }

    public void deleteDept(Long id) {
        for (User user : db.users().values()) {
            if (id.equals(user.getDeptId())) {
                throw new BusinessException("部门下还有用户，不能删除");
            }
        }
        db.departments().remove(id);
        persistence.deleteDepartment(id);
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
