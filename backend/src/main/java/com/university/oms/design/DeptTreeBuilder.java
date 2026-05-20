package com.university.oms.design;

import com.university.oms.model.Department;
import com.university.oms.repository.InMemoryDatabase;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Builds a department tree (Composite pattern) from InMemoryDatabase.
 */
@Component
public class DeptTreeBuilder {
    private final InMemoryDatabase db;

    public DeptTreeBuilder(InMemoryDatabase db) {
        this.db = db;
    }

    public DeptTreeNode buildTree() {
        Map<Long, DeptTreeNode> nodeMap = new LinkedHashMap<>();
        for (Department dept : db.departments().values()) {
            nodeMap.put(dept.getId(), new DeptTreeNode(dept));
        }
        DeptTreeNode root = null;
        for (DeptTreeNode node : nodeMap.values()) {
            Long parentId = node.getDepartment().getParentId();
            if (parentId == null || parentId == 0L) {
                if (root == null) {
                    root = node;
                } else {
                    root.addChild(node);
                }
            } else {
                DeptTreeNode parent = nodeMap.get(parentId);
                if (parent != null) {
                    parent.addChild(node);
                } else if (root == null) {
                    root = node;
                } else {
                    root.addChild(node);
                }
            }
        }
        if (root == null) {
            throw new IllegalStateException("No root department found");
        }
        return root;
    }
}
