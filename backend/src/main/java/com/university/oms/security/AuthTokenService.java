package com.university.oms.security;

import com.university.oms.model.User;
import com.university.oms.repository.InMemoryDatabase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthTokenService {
    private final Map<String, TokenRecord> tokens = new ConcurrentHashMap<String, TokenRecord>();
    private final SecureRandom random = new SecureRandom();
    private final InMemoryDatabase db;

    public AuthTokenService(InMemoryDatabase db) {
        this.db = db;
    }

    public String issue(User user) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        tokens.put(token, new TokenRecord(user.getId(), LocalDateTime.now().plusHours(8)));
        return token;
    }

    public User resolve(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }
        TokenRecord record = tokens.get(token);
        if (record == null) {
            return null;
        }
        if (record.expiresAt.isBefore(LocalDateTime.now())) {
            tokens.remove(token);
            return null;
        }
        return db.users().get(record.userId);
    }

    public void revoke(String token) {
        if (token != null) {
            tokens.remove(token);
        }
    }

    @Scheduled(fixedDelay = 3600000)
    public void cleanupExpiredTokens() {
        cleanupExpiredTokens(LocalDateTime.now());
    }

    int cleanupExpiredTokens(LocalDateTime now) {
        int before = tokens.size();
        tokens.entrySet().removeIf(entry -> entry.getValue().expiresAt.isBefore(now));
        return before - tokens.size();
    }

    int activeTokenCount() {
        return tokens.size();
    }

    private static class TokenRecord {
        private final Long userId;
        private final LocalDateTime expiresAt;

        private TokenRecord(Long userId, LocalDateTime expiresAt) {
            this.userId = userId;
            this.expiresAt = expiresAt;
        }
    }
}
