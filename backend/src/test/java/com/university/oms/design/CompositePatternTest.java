package com.university.oms.design;

import com.university.oms.model.Department;
import com.university.oms.model.User;
import com.university.oms.repository.OmsRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class CompositePatternTest {
    @Autowired
    private DeptTreeBuilder builder;

    @Autowired
    private OmsRepository repo;

    @Test
    void treeBuildFromDepartments() {
        DeptTreeNode root = builder.buildTree();

        assertNotNull(root);
        assertTrue(root.getChildren().size() > 0, "根节点应有子部门");
    }

    @Test
    void countAllUsersInTree() {
        DeptTreeNode root = builder.buildTree();
        List<User> allUsers = repo.findAllUsers();

        int total = root.countAllUsers(allUsers);
        assertEquals(8, total, "应统计所有8个用户");
    }

    @Test
    void flattenReturnsAllDepartments() {
        DeptTreeNode root = builder.buildTree();

        List<Department> all = root.flatten();
        assertEquals(4, all.size(), "应包含所有4个部门");
    }
}
