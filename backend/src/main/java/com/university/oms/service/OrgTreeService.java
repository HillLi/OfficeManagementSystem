package com.university.oms.service;

import com.university.oms.dto.OrgTreeNode;
import com.university.oms.model.Department;
import com.university.oms.model.User;
import com.university.oms.repository.InMemoryDatabase;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class OrgTreeService {
    private static final Comparator<Department> DEPARTMENT_ORDER = Comparator
            .comparing(Department::getDeptName, Comparator.nullsFirst(Comparator.naturalOrder()))
            .thenComparing(Department::getId, Comparator.nullsFirst(Comparator.naturalOrder()));
    private static final Comparator<User> USER_ORDER = Comparator
            .comparing(User::getRealName, Comparator.nullsFirst(Comparator.naturalOrder()))
            .thenComparing(User::getId, Comparator.nullsFirst(Comparator.naturalOrder()));

    private final InMemoryDatabase db;

    public OrgTreeService(InMemoryDatabase db) {
        this.db = db;
    }

    public List<OrgTreeNode> buildTree() {
        List<Department> sortedDepartments = new ArrayList<Department>(db.departments().values());
        sortedDepartments.sort(DEPARTMENT_ORDER);

        Map<Long, Department> departmentModels = new LinkedHashMap<Long, Department>();
        Map<Long, OrgTreeNode> departments = new LinkedHashMap<Long, OrgTreeNode>();
        for (Department department : sortedDepartments) {
            departmentModels.put(department.getId(), department);
            departments.put(department.getId(), departmentNode(department));
        }

        List<OrgTreeNode> roots = new ArrayList<OrgTreeNode>();
        for (Department department : sortedDepartments) {
            OrgTreeNode node = departments.get(department.getId());
            Long parentId = department.getParentId();
            OrgTreeNode parent = parentId == null ? null : departments.get(parentId);
            if (parentId == null || parentId == 0L || parent == null
                    || hasCyclicParentChain(department, departmentModels)) {
                roots.add(node);
            } else {
                parent.getChildren().add(node);
            }
        }

        List<User> sortedUsers = new ArrayList<User>(db.users().values());
        sortedUsers.sort(USER_ORDER);
        for (User user : sortedUsers) {
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

    private boolean hasCyclicParentChain(Department department, Map<Long, Department> departments) {
        Set<Long> visited = new HashSet<Long>();
        Department current = department;
        while (current != null) {
            if (!visited.add(current.getId())) {
                return true;
            }
            Long parentId = current.getParentId();
            if (parentId == null || parentId == 0L) {
                return false;
            }
            current = departments.get(parentId);
        }
        return false;
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
