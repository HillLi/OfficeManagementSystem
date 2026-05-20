package com.university.oms.service;

import com.university.oms.common.BusinessException;
import com.university.oms.dto.LoginRequest;
import com.university.oms.dto.LoginResult;
import com.university.oms.model.User;
import com.university.oms.repository.InMemoryDatabase;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AuthService {
    private final InMemoryDatabase db;

    public AuthService(InMemoryDatabase db) {
        this.db = db;
    }

    public LoginResult login(LoginRequest request) {
        for (User user : db.users().values()) {
            if (user.getUsername().equals(request.getUsername()) && user.getPassword().equals(request.getPassword())) {
                return new LoginResult("demo-token-" + user.getId(), user);
            }
        }
        throw new BusinessException("用户名或密码错误");
    }

    public List<User> users() {
        return new ArrayList<User>(db.users().values());
    }
}
