package com.university.oms.security;

import com.university.oms.model.User;
import com.university.oms.repository.OmsRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Token 认证服务，负责令牌的签发、解析和过期清理
 */
@Service
public class AuthTokenService {

    /** Token 与用户信息的映射表 */
    private final Map<String, TokenRecord> tokens = new ConcurrentHashMap<String, TokenRecord>();
    private final SecureRandom random = new SecureRandom();
    private final OmsRepository repo;

    public AuthTokenService(OmsRepository repo) {
        this.repo = repo;
    }

    /** 为指定用户签发 Token，有效期8小时 */
    public String issue(User user) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        tokens.put(token, new TokenRecord(user.getId(), LocalDateTime.now().plusHours(8)));
        return token;
    }

    /** 根据 Token 解析对应的用户，过期或无效时返回 null */
    public User resolve(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }
        TokenRecord record = tokens.get(token);
        if (record == null) {
            return null;
        }
        // Token 已过期则移除
        if (record.expiresAt.isBefore(LocalDateTime.now())) {
            tokens.remove(token);
            return null;
        }
        return repo.findUserById(record.userId);
    }

    /** 撤销指定 Token（注销登录） */
    public void revoke(String token) {
        if (token != null) {
            tokens.remove(token);
        }
    }

    /** 定时清理过期 Token，每小时执行一次 */
    @Scheduled(fixedDelay = 3600000)
    public void cleanupExpiredTokens() {
        cleanupExpiredTokens(LocalDateTime.now());
    }

    /** 清理所有已过期的 Token，返回清理数量 */
    int cleanupExpiredTokens(LocalDateTime now) {
        int before = tokens.size();
        tokens.entrySet().removeIf(entry -> entry.getValue().expiresAt.isBefore(now));
        return before - tokens.size();
    }

    /** 获取当前活跃 Token 数量 */
    int activeTokenCount() {
        return tokens.size();
    }

    /** Token 记录，保存用户ID和过期时间 */
    private static class TokenRecord {
        private final Long userId;
        private final LocalDateTime expiresAt;

        private TokenRecord(Long userId, LocalDateTime expiresAt) {
            this.userId = userId;
            this.expiresAt = expiresAt;
        }
    }
}
