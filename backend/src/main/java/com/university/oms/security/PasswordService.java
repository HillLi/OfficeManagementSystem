package com.university.oms.security;

import org.springframework.stereotype.Service;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 密码服务，基于 PBKDF2 算法实现密码哈希和校验
 */
@Service
public class PasswordService {

    /** 哈希算法标识前缀 */
    private static final String PREFIX = "pbkdf2";
    /** 迭代次数 */
    private static final int ITERATIONS = 120000;
    /** 密钥长度（位） */
    private static final int KEY_LENGTH = 256;
    private final SecureRandom random = new SecureRandom();

    /** 对明文密码进行哈希，返回格式为 pbkdf2$迭代次数$盐$哈希值 */
    public String hash(String rawPassword) {
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        byte[] hash = pbkdf2(rawPassword, salt, ITERATIONS);
        return PREFIX + "$" + ITERATIONS + "$"
                + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(hash);
    }

    /** 校验明文密码与存储的哈希密码是否匹配 */
    public boolean matches(String rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null) {
            return false;
        }
        // 兼容明文密码（用于历史数据迁移）
        if (!storedPassword.startsWith(PREFIX + "$")) {
            return rawPassword.equals(storedPassword);
        }
        String[] parts = storedPassword.split("\\$");
        if (parts.length != 4) {
            return false;
        }
        int iterations = Integer.parseInt(parts[1]);
        byte[] salt = Base64.getDecoder().decode(parts[2]);
        byte[] expected = Base64.getDecoder().decode(parts[3]);
        byte[] actual = pbkdf2(rawPassword, salt, iterations);
        return constantTimeEquals(expected, actual);
    }

    /** 判断存储的密码是否需要升级为 PBKDF2 哈希格式 */
    public boolean needsUpgrade(String storedPassword) {
        return storedPassword == null || !storedPassword.startsWith(PREFIX + "$");
    }

    /** 使用 PBKDF2WithHmacSHA256 算法计算密码哈希 */
    private byte[] pbkdf2(String password, byte[] salt, int iterations) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, KEY_LENGTH);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("密码哈希失败", e);
        }
    }

    /** 恒定时间比较，防止时序攻击 */
    private boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a.length != b.length) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        return result == 0;
    }
}
