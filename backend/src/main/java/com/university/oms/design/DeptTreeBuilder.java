package com.university.oms.design;

import com.university.oms.model.Department;
import com.university.oms.repository.OmsRepository;
import org.springframework.stereotype.Component;

import java.util.*;

// 建造者模式：从数据库构建部门树结构
@Component
public class DeptTreeBuilder {
    private final OmsRepository repo;

    public DeptTreeBuilder(OmsRepository repo) {
        this.repo = repo;
    }

    // 从数据库加载所有部门并构建树形结构
    public DeptTreeNode buildTree() {
        Map<Long, DeptTreeNode> nodeMap = new LinkedHashMap<>();
        for (Department dept : repo.findAllDepartments()) {
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
