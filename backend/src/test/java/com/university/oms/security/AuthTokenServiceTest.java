package com.university.oms.security;

import com.university.oms.model.User;
import com.university.oms.repository.OmsRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@ActiveProfiles("test")
class AuthTokenServiceTest {
    @Autowired
    private AuthTokenService service;

    @Autowired
    private OmsRepository repo;

    @Test
    void cleanupExpiredTokensRemovesExpiredRecords() {
        User user = repo.findUserById(99L);
        // Use user id 2 from test data if 99 is not present
        if (user == null) {
            user = repo.findUserById(2L);
        }
        String token = service.issue(user);

        assertNotNull(service.resolve(token));
        assertEquals(1, service.activeTokenCount());
        assertEquals(1, service.cleanupExpiredTokens(LocalDateTime.now().plusHours(9)));
        assertEquals(0, service.activeTokenCount());
        assertNull(service.resolve(token));
    }
}
