package com.university.oms.service;

import com.university.oms.dto.OrgTreeNode;
import com.university.oms.model.Department;
import com.university.oms.model.User;
import com.university.oms.repository.OmsRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 组织架构树服务，构建部门-用户的树形结构
 */
@Service
public class OrgTreeService {
    private static final Comparator<Department> DEPARTMENT_ORDER = Comparator
            .comparing(Department::getDeptName, Comparator.nullsFirst(Comparator.naturalOrder()))
            .thenComparing(Department::getId, Comparator.nullsFirst(Comparator.naturalOrder()));
    private static final Comparator<User> USER_ORDER = Comparator
            .comparing(User::getRealName, Comparator.nullsFirst(Comparator.naturalOrder()))
            .thenComparing(User::getId, Comparator.nullsFirst(Comparator.naturalOrder()));

    private final OmsRepository repo;

    public OrgTreeService(OmsRepository repo) {
        this.repo = repo;
    }

    /** 构建组织架构树（部门层级 + 部门下用户） */
    public List<OrgTreeNode> buildTree() {
        List<Department> sortedDepartments = new ArrayList<Department>(repo.findAllDepartments());
        sortedDepartments.sort(DEPARTMENT_ORDER);

        Map<Long, Department> departmentModels = new LinkedHashMap<Long, Department>();
        Map<Long, OrgTreeNode> departments = new LinkedHashMap<Long, OrgTreeNode>();
        for (Department department : sortedDepartments) {
            departmentModels.put(department.getId(), department);
            departments.put(department.getId(), departmentNode(department));
        }

        // 构建部门层级关系，检测循环引用
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

        // 将用户挂载到对应部门节点下
        List<User> sortedUsers = new ArrayList<User>(repo.findAllUsers());
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

    /** 检测部门父级链是否存在循环引用 */
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
