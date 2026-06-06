package com.university.oms.security;

import com.university.oms.model.User;
import com.university.oms.repository.InMemoryDatabase;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class AuthTokenServiceTest {
    @Test
    void cleanupExpiredTokensRemovesExpiredRecords() {
        InMemoryDatabase db = new InMemoryDatabase(new PasswordService());
        User user = new User();
        db.fill(user, 99L);
        db.users().put(user.getId(), user);
        AuthTokenService service = new AuthTokenService(db);
        String token = service.issue(user);

        assertNotNull(service.resolve(token));
        assertEquals(1, service.activeTokenCount());
        assertEquals(1, service.cleanupExpiredTokens(LocalDateTime.now().plusHours(9)));
        assertEquals(0, service.activeTokenCount());
        assertNull(service.resolve(token));
    }
}
