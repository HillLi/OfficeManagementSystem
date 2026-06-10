package com.university.oms.design;

import com.university.oms.model.Department;
import com.university.oms.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Composite pattern — department tree node.
 * Supports recursive operations across the organizational hierarchy.
 */
public class DeptTreeNode {
    private final Department department;
    private final List<DeptTreeNode> children = new ArrayList<>();

    public DeptTreeNode(Department department) {
        this.department = department;
    }

    public void addChild(DeptTreeNode child) {
        children.add(child);
    }

    public Department getDepartment() {
        return department;
    }

    public List<DeptTreeNode> getChildren() {
        return children;
    }

    public int countAllUsers(List<User> allUsers) {
        int count = 0;
        for (User user : allUsers) {
            if (department.getId().equals(user.getDeptId())) {
                count++;
            }
        }
        for (DeptTreeNode child : children) {
            count += child.countAllUsers(allUsers);
        }
        return count;
    }

    public List<Department> flatten() {
        List<Department> result = new ArrayList<>();
        result.add(department);
        for (DeptTreeNode child : children) {
            result.addAll(child.flatten());
        }
        return result;
    }
}
