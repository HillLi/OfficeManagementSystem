package com.university.oms.design;

import com.university.oms.common.BusinessException;
import com.university.oms.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StatePatternTest {
    private User adminUser;
    private User normalUser;

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setRoleKeys(new java.util.LinkedHashSet<>(java.util.Arrays.asList("admin")));

        normalUser = new User();
        normalUser.setId(2L);
        normalUser.setRoleKeys(new java.util.LinkedHashSet<>(java.util.Arrays.asList("office_user")));
    }

    @Test
    void draftStateCannotBeApproved() {
        BusinessState state = new DraftState();
        assertThrows(BusinessException.class, () -> state.approve(adminUser));
    }

    @Test
    void approvedStateCannotChange() {
        BusinessState state = new ApprovedState();
        assertThrows(BusinessException.class, () -> state.approve(adminUser));
        assertThrows(BusinessException.class, () -> state.reject(adminUser));
    }

    @Test
    void pendingStateApprovesWithCorrectRole() {
        BusinessState state = new PendingState("dept_head", "pending_office");
        assertThrows(BusinessException.class, () -> state.approve(normalUser));

        User deptHead = new User();
        deptHead.setId(3L);
        deptHead.setRoleKeys(new java.util.LinkedHashSet<>(java.util.Arrays.asList("dept_head")));
        assertEquals("pending_office", state.approve(deptHead));
    }

    @Test
    void rejectedStateCanWithdrawByApplicant() {
        BusinessState state = new RejectedState();
        assertEquals("draft", state.withdraw(2L, 2L));
        assertThrows(BusinessException.class, () -> state.withdraw(3L, 2L));
    }
}
