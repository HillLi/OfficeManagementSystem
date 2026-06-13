package com.university.oms.service;

import com.university.oms.common.BusinessException;
import com.university.oms.dto.LoginRequest;
import com.university.oms.dto.LoginResult;
import com.university.oms.dto.UserOption;
import com.university.oms.model.Department;
import com.university.oms.model.User;
import com.university.oms.repository.OmsRepository;
import com.university.oms.security.AuthTokenService;
import com.university.oms.security.PasswordService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 认证服务，处理用户登录、登出和登录失败锁定
 */
@Service
public class AuthService {
    private static final int MAX_FAILURES = 5;
    private static final long LOCK_MINUTES = 10;
    private final OmsRepository repo;
    private final AuthTokenService tokenService;
    private final PasswordService passwordService;
    private final Map<String, LoginFailure> loginFailures = new ConcurrentHashMap<String, LoginFailure>();

    public AuthService(OmsRepository repo, AuthTokenService tokenService, PasswordService passwordService) {
        this.repo = repo;
        this.tokenService = tokenService;
        this.passwordService = passwordService;
    }

    /** 用户登录，校验密码并签发Token */
    public LoginResult login(LoginRequest request) {
        String username = request.getUsername() == null ? "" : request.getUsername().trim().toLowerCase();
        assertLoginAllowed(username);
        for (User user : repo.findAllUsers()) {
            if (user.getUsername().equals(request.getUsername()) && passwordService.matches(request.getPassword(), user.getPassword())) {
                loginFailures.remove(username);
                // 密码格式需要升级时自动重新hash
                if (passwordService.needsUpgrade(user.getPassword())) {
                    user.setPassword(passwordService.hash(request.getPassword()));
                    repo.saveUser(user);
                }
                return new LoginResult(tokenService.issue(user), user);
            }
        }
        recordLoginFailure(username);
        throw new BusinessException("用户名或密码错误");
    }

    /** 获取所有用户列表 */
    public List<User> users() {
        return repo.findAllUsers();
    }

    /** 获取用户选项列表（用于下拉选择） */
    public List<UserOption> userOptions() {
        List<UserOption> options = new ArrayList<UserOption>();
        for (User user : repo.findAllUsers()) {
            options.add(new UserOption(user.getId(), user.getRealName(), user.getDeptId(), user.getDeptName()));
        }
        return options;
    }

    /** 获取部门选项列表 */
    public List<Department> deptOptions() {
        return repo.findAllDepartments();
    }

    /** 用户登出，注销Token */
    public void logout(String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            tokenService.revoke(authorization.substring("Bearer ".length()).trim());
        } else {
            tokenService.revoke(authorization);
        }
    }

    /** 检查登录是否被锁定 */
    private void assertLoginAllowed(String username) {
        LoginFailure failure = loginFailures.get(username);
        if (failure == null) {
            return;
        }
        if (failure.lockedUntil != null && failure.lockedUntil.isAfter(LocalDateTime.now())) {
            throw new BusinessException("登录失败次数过多，请稍后再试");
        }
        if (failure.lockedUntil != null) {
            loginFailures.remove(username);
        }
    }

    /** 记录登录失败次数，达到上限后锁定账号 */
    private void recordLoginFailure(String username) {
        LoginFailure failure = loginFailures.computeIfAbsent(username, key -> new LoginFailure());
        failure.count++;
        if (failure.count >= MAX_FAILURES) {
            failure.lockedUntil = LocalDateTime.now().plusMinutes(LOCK_MINUTES);
        }
    }

    /** 登录失败记录 */
    private static class LoginFailure {
        private int count;
        private LocalDateTime lockedUntil;
    }
}
