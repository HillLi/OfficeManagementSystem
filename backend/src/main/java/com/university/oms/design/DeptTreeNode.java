package com.university.oms.design;

import com.university.oms.model.Department;
import com.university.oms.model.User;

import java.util.ArrayList;
import java.util.List;

// 组合模式：部门树节点，支持树形层级结构的递归操作
public class DeptTreeNode {
    private final Department department;
    private final List<DeptTreeNode> children = new ArrayList<>();

    public DeptTreeNode(Department department) {
        this.department = department;
    }

    // 添加子部门节点
    public void addChild(DeptTreeNode child) {
        children.add(child);
    }

    public Department getDepartment() {
        return department;
    }

    public List<DeptTreeNode> getChildren() {
        return children;
    }

    // 递归统计当前部门及其子部门的所有用户数量
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

    // 递归将树形结构展平为部门列表
    public List<Department> flatten() {
        List<Department> result = new ArrayList<>();
        result.add(department);
        for (DeptTreeNode child : children) {
            result.addAll(child.flatten());
        }
        return result;
    }
}
