package com.university.oms.security;

import com.university.oms.model.User;
import com.university.oms.repository.InMemoryDatabase;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthTokenService {
    private final Map<String, Long> tokens = new ConcurrentHashMap<String, Long>();
    private final SecureRandom random = new SecureRandom();
    private final InMemoryDatabase db;

    public AuthTokenService(InMemoryDatabase db) {
        this.db = db;
    }

    public String issue(User user) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        tokens.put(token, user.getId());
        return token;
    }

    public User resolve(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }
        Long userId = tokens.get(token);
        return userId == null ? null : db.users().get(userId);
    }
}
