package com.university.oms.service;

import com.university.oms.common.BusinessException;
import com.university.oms.dto.LoginRequest;
import com.university.oms.dto.LoginResult;
import com.university.oms.dto.UserOption;
import com.university.oms.model.User;
import com.university.oms.repository.DataPersistence;
import com.university.oms.repository.InMemoryDatabase;
import com.university.oms.security.AuthTokenService;
import com.university.oms.security.PasswordService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AuthService {
    private final InMemoryDatabase db;
    private final AuthTokenService tokenService;
    private final PasswordService passwordService;
    private final DataPersistence persistence;

    public AuthService(InMemoryDatabase db, AuthTokenService tokenService, PasswordService passwordService,
                       DataPersistence persistence) {
        this.db = db;
        this.tokenService = tokenService;
        this.passwordService = passwordService;
        this.persistence = persistence;
    }

    public LoginResult login(LoginRequest request) {
        for (User user : db.users().values()) {
            if (user.getUsername().equals(request.getUsername()) && passwordService.matches(request.getPassword(), user.getPassword())) {
                if (passwordService.needsUpgrade(user.getPassword())) {
                    user.setPassword(passwordService.hash(request.getPassword()));
                    persistence.saveUser(user);
                }
                return new LoginResult(tokenService.issue(user), user);
            }
        }
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

    public void logout(String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            tokenService.revoke(authorization.substring("Bearer ".length()).trim());
        } else {
            tokenService.revoke(authorization);
        }
    }
}
