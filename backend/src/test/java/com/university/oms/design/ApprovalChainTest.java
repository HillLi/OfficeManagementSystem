package com.university.oms.design;

import com.university.oms.common.BusinessException;
import com.university.oms.model.User;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedHashSet;

import static org.junit.jupiter.api.Assertions.*;

class ApprovalChainTest {
    private User userWithRole(String... roles) {
        User u = new User();
        u.setId(1L);
        u.setRoleKeys(new LinkedHashSet<>(Arrays.asList(roles)));
        return u;
    }

    @Test
    void documentFullChain() {
        // Each step in the chain is performed by a different approver
        DeptHeadHandler deptHead = new DeptHeadHandler();
        OfficeAdminHandler officeAdmin = new OfficeAdminHandler();
        SchoolLeaderHandler leader = new SchoolLeaderHandler();

        // Step 1: dept head approves pending_dept -> returns next status
        String result = deptHead.handle("pending_dept", "approve", userWithRole("dept_head"));
        assertEquals("pending_office", result);

        // Step 2: office admin approves pending_office
        result = officeAdmin.handle("pending_office", "approve", userWithRole("office_admin"));
        assertEquals("pending_leader", result);

        // Step 3: school leader approves pending_leader
        result = leader.handle("pending_leader", "approve", userWithRole("school_leader"));
        assertEquals("approved", result);
    }

    @Test
    void rejectionStopsChain() {
        DeptHeadHandler deptHead = new DeptHeadHandler();
        String result = deptHead.handle("pending_dept", "reject", userWithRole("dept_head"));
        assertEquals("rejected", result);
    }

    @Test
    void wrongRoleThrows() {
        DeptHeadHandler deptHead = new DeptHeadHandler();
        assertThrows(BusinessException.class, () ->
                deptHead.handle("pending_dept", "approve", userWithRole("office_user")));
    }

    @Test
    void travelChainWithFinance() {
        DeptHeadHandler deptHead = new DeptHeadHandler();
        FinanceHandler finance = new FinanceHandler();

        String result = deptHead.handle("pending_dept", "approve", userWithRole("dept_head"));
        assertEquals("pending_office", result);
        result = finance.handle("pending_finance", "approve", userWithRole("finance_staff"));
        assertEquals("approved", result);
    }
}
