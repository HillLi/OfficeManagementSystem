package com.university.oms.service;

import com.university.oms.dto.OrgTreeNode;
import com.university.oms.model.Department;
import com.university.oms.model.User;
import com.university.oms.repository.InMemoryDatabase;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrgTreeService {
    private final InMemoryDatabase db;

    public OrgTreeService(InMemoryDatabase db) {
        this.db = db;
    }

    public List<OrgTreeNode> buildTree() {
        Map<Long, OrgTreeNode> departments = new LinkedHashMap<Long, OrgTreeNode>();
        for (Department department : db.departments().values()) {
            departments.put(department.getId(), departmentNode(department));
        }

        List<OrgTreeNode> roots = new ArrayList<OrgTreeNode>();
        for (Department department : db.departments().values()) {
            OrgTreeNode node = departments.get(department.getId());
            Long parentId = department.getParentId();
            OrgTreeNode parent = parentId == null ? null : departments.get(parentId);
            if (parentId == null || parentId == 0L || parent == null) {
                roots.add(node);
            } else {
                parent.getChildren().add(node);
            }
        }

        for (User user : db.users().values()) {
            OrgTreeNode node = userNode(user);
            OrgTreeNode department = departments.get(user.getDeptId());
            if (department == null) {
                roots.add(node);
            } else {
                department.getChildren().add(node);
            }
        }
        return roots;
    }

    private OrgTreeNode departmentNode(Department department) {
        OrgTreeNode node = new OrgTreeNode();
        node.setId("dept-" + department.getId());
        node.setLabel(department.getDeptName());
        node.setType("dept");
        node.setDeptId(department.getId());
        return node;
    }

    private OrgTreeNode userNode(User user) {
        OrgTreeNode node = new OrgTreeNode();
        node.setId("user-" + user.getId());
        node.setLabel(user.getRealName());
        node.setType("user");
        node.setDeptId(user.getDeptId());
        node.setUserId(user.getId());
        node.setEmail(user.getEmail());
        return node;
    }
}
