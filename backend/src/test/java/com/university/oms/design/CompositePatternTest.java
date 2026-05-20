package com.university.oms.design;

import com.university.oms.model.Department;
import com.university.oms.model.User;
import com.university.oms.repository.InMemoryDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CompositePatternTest {
    private InMemoryDatabase db;

    @BeforeEach
    void setUp() {
        db = new InMemoryDatabase();
        db.init();
    }

    @Test
    void treeBuildFromDepartments() {
        DeptTreeBuilder builder = new DeptTreeBuilder(db);
        DeptTreeNode root = builder.buildTree();

        assertNotNull(root);
        assertTrue(root.getChildren().size() > 0, "根节点应有子部门");
    }

    @Test
    void countAllUsersInTree() {
        DeptTreeBuilder builder = new DeptTreeBuilder(db);
        DeptTreeNode root = builder.buildTree();

        int total = root.countAllUsers(db);
        assertEquals(8, total, "应统计所有8个用户");
    }

    @Test
    void flattenReturnsAllDepartments() {
        DeptTreeBuilder builder = new DeptTreeBuilder(db);
        DeptTreeNode root = builder.buildTree();

        List<Department> all = root.flatten();
        assertEquals(4, all.size(), "应包含所有4个部门");
    }
}
