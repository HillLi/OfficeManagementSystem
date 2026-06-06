package com.university.oms.service;

import com.university.oms.common.BusinessException;
import com.university.oms.dto.LoginRequest;
import com.university.oms.dto.LoginResult;
import com.university.oms.dto.UserOption;
import com.university.oms.model.Department;
import com.university.oms.model.User;
import com.university.oms.repository.DataPersistence;
import com.university.oms.repository.InMemoryDatabase;
import com.university.oms.security.AuthTokenService;
import com.university.oms.security.PasswordService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {
    private static final int MAX_FAILURES = 5;
    private static final long LOCK_MINUTES = 10;
    private final InMemoryDatabase db;
    private final AuthTokenService tokenService;
    private final PasswordService passwordService;
    private final DataPersistence persistence;
    private final Map<String, LoginFailure> loginFailures = new ConcurrentHashMap<String, LoginFailure>();

    public AuthService(InMemoryDatabase db, AuthTokenService tokenService, PasswordService passwordService,
                       DataPersistence persistence) {
        this.db = db;
        this.tokenService = tokenService;
        this.passwordService = passwordService;
        this.persistence = persistence;
    }

    public LoginResult login(LoginRequest request) {
        String username = request.getUsername() == null ? "" : request.getUsername().trim().toLowerCase();
        assertLoginAllowed(username);
        for (User user : db.users().values()) {
            if (user.getUsername().equals(request.getUsername()) && passwordService.matches(request.getPassword(), user.getPassword())) {
                loginFailures.remove(username);
                if (passwordService.needsUpgrade(user.getPassword())) {
                    user.setPassword(passwordService.hash(request.getPassword()));
                    persistence.saveUser(user);
                }
                return new LoginResult(tokenService.issue(user), user);
            }
        }
        recordLoginFailure(username);
        throw new BusinessException("用户名或密码错误");
    }

    public List<User> users() {
        return new ArrayList<User>(db.users().values());
    }

    public List<UserOption> userOptions() {
        List<UserOption> options = new ArrayList<UserOption>();
        for (User user : db.users().values()) {
            options.add(new UserOption(user.getId(), user.getRealName(), user.getDeptId(), user.getDeptName()));
        }
        return options;
    }

    public List<Department> deptOptions() {
        return new ArrayList<Department>(db.departments().values());
    }

    public void logout(String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            tokenService.revoke(authorization.substring("Bearer ".length()).trim());
        } else {
            tokenService.revoke(authorization);
        }
    }

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

    private void recordLoginFailure(String username) {
        LoginFailure failure = loginFailures.computeIfAbsent(username, key -> new LoginFailure());
        failure.count++;
        if (failure.count >= MAX_FAILURES) {
            failure.lockedUntil = LocalDateTime.now().plusMinutes(LOCK_MINUTES);
        }
    }

    private static class LoginFailure {
        private int count;
        private LocalDateTime lockedUntil;
    }
}
