package com.university.oms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.university.oms.model.Department;
import com.university.oms.model.User;
import com.university.oms.repository.InMemoryDatabase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MailIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InMemoryDatabase db;

    @Test
    void adminCreateUserRequiresEmail() throws Exception {
        String token = loginAdmin();

        mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"missingemail\",\"password\":\"123456\",\"realName\":\"Missing Email\",\"deptId\":4,\"roleKeys\":\"office_user\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void adminCreateUserRejectsInvalidEmail() throws Exception {
        String token = loginAdmin();

        mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"invalidemail\",\"password\":\"123456\",\"realName\":\"Invalid Email\",\"deptId\":4,\"roleKeys\":\"office_user\",\"email\":\"not-an-email\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void adminCreateUserStoresEmail() throws Exception {
        String token = loginAdmin();

        JsonNode created = postJson("/api/admin/users",
                "{\"username\":\"mailuser\",\"password\":\"123456\",\"realName\":\"Mail User\",\"deptId\":4,\"roleKeys\":\"office_user\",\"email\":\"mailuser@example.com\"}",
                token);
        long id = created.get("data").get("id").asLong();

        assertEquals("mailuser@example.com", db.users().get(id).getEmail());

        mockMvc.perform(get("/api/admin/users/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("mailuser@example.com"));
    }

    @Test
    void adminUpdateUserRejectsInvalidEmail() throws Exception {
        String token = loginAdmin();

        mockMvc.perform(put("/api/admin/users/2")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"realName\":\"普通办公人员\",\"deptId\":4,\"roleKeys\":\"office_user\",\"email\":\"bad-email\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void adminUpdateUserRejectsBlankEmail() throws Exception {
        String token = loginAdmin();

        mockMvc.perform(put("/api/admin/users/2")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"realName\":\"Office User\",\"deptId\":4,\"roleKeys\":\"office_user\",\"email\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void adminUpdateUserStoresEmail() throws Exception {
        String token = loginAdmin();

        mockMvc.perform(put("/api/admin/users/2")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"realName\":\"普通办公人员\",\"deptId\":4,\"roleKeys\":\"office_user\",\"email\":\"new-user@example.com\"}"))
                .andExpect(status().isOk());

        assertEquals("new-user@example.com", db.users().get(2L).getEmail());
    }

    @Test
    void organizationTreeContainsDepartmentsAndUsers() throws Exception {
        String token = loginAdmin();
        Department parent = addDepartment(900001L, "Tree Parent", 0L);
        Department child = addDepartment(900002L, "Tree Child", parent.getId());
        User user = addUser(900003L, "treeuser", "Tree User", child.getId());

        try {
            JsonNode tree = getOrganizationTree(token);
            JsonNode parentNode = findDirectNode(tree, "dept-" + parent.getId());
            assertNotNull(parentNode);
            JsonNode childNode = findDirectNode(parentNode.get("children"), "dept-" + child.getId());
            assertNotNull(childNode);
            JsonNode userNode = findDirectNode(childNode.get("children"), "user-" + user.getId());

            assertEquals("dept", parentNode.get("type").asText());
            assertEquals("dept", childNode.get("type").asText());
            assertNotNull(userNode);
            assertEquals("user", userNode.get("type").asText());
            assertEquals(user.getId().longValue(), userNode.get("userId").asLong());
            assertEquals(user.getEmail(), userNode.get("email").asText());
        } finally {
            db.users().remove(user.getId());
            db.departments().remove(child.getId());
            db.departments().remove(parent.getId());
        }
    }

    @Test
    void organizationTreeFallsBackToRootsForMalformedAndCyclicDepartments() throws Exception {
        String token = loginAdmin();
        Department missingParent = addDepartment(900011L, "Missing Parent", 999999L);
        Department selfParent = addDepartment(900012L, "Self Parent", 900012L);
        Department cycleA = addDepartment(900013L, "Cycle A", 900014L);
        Department cycleB = addDepartment(900014L, "Cycle B", 900013L);
        Department cycleChild = addDepartment(900015L, "Cycle Child", 900013L);

        try {
            JsonNode tree = getOrganizationTree(token);

            assertRootNode(tree, missingParent);
            assertRootNode(tree, selfParent);
            assertRootNode(tree, cycleA);
            assertRootNode(tree, cycleB);
            assertRootNode(tree, cycleChild);
            assertEquals(1, countNodes(tree, "dept-" + missingParent.getId()));
            assertEquals(1, countNodes(tree, "dept-" + selfParent.getId()));
            assertEquals(1, countNodes(tree, "dept-" + cycleA.getId()));
            assertEquals(1, countNodes(tree, "dept-" + cycleB.getId()));
            assertEquals(1, countNodes(tree, "dept-" + cycleChild.getId()));
        } finally {
            db.departments().remove(cycleChild.getId());
            db.departments().remove(cycleB.getId());
            db.departments().remove(cycleA.getId());
            db.departments().remove(selfParent.getId());
            db.departments().remove(missingParent.getId());
        }
    }

    @Test
    void organizationTreeSortsDepartmentsAndUsersByLabelThenId() throws Exception {
        String token = loginAdmin();
        Department zDepartment = addDepartment(900021L, "Z Department", 0L);
        Department aDepartment = addDepartment(900022L, "A Department", 0L);
        User zUser = addUser(900023L, "zuser", "Z User", aDepartment.getId());
        User aUser = addUser(900024L, "auser", "A User", aDepartment.getId());

        try {
            JsonNode tree = getOrganizationTree(token);
            JsonNode aDepartmentNode = findDirectNode(tree, "dept-" + aDepartment.getId());
            assertNotNull(aDepartmentNode);

            assertBefore(tree, "dept-" + aDepartment.getId(), "dept-" + zDepartment.getId());
            assertBefore(aDepartmentNode.get("children"), "user-" + aUser.getId(), "user-" + zUser.getId());
        } finally {
            db.users().remove(aUser.getId());
            db.users().remove(zUser.getId());
            db.departments().remove(aDepartment.getId());
            db.departments().remove(zDepartment.getId());
        }
    }

    private JsonNode getOrganizationTree(String token) throws Exception {
        String body = mockMvc.perform(get("/api/org/tree")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("data");
    }

    private Department addDepartment(Long id, String name, Long parentId) {
        Department department = new Department();
        db.fill(department, id);
        department.setDeptName(name);
        department.setParentId(parentId);
        db.departments().put(id, department);
        return department;
    }

    private User addUser(Long id, String username, String realName, Long deptId) {
        User user = new User();
        db.fill(user, id);
        user.setUsername(username);
        user.setRealName(realName);
        user.setEmail(username + "@example.com");
        user.setDeptId(deptId);
        user.setDeptName(db.departments().get(deptId).getDeptName());
        db.users().put(id, user);
        return user;
    }

    private void assertRootNode(JsonNode tree, Department department) {
        assertNotNull(findDirectNode(tree, "dept-" + department.getId()));
    }

    private JsonNode findDirectNode(JsonNode nodes, String id) {
        if (nodes == null || !nodes.isArray()) {
            return null;
        }
        for (JsonNode node : nodes) {
            if (id.equals(node.path("id").asText())) {
                return node;
            }
        }
        return null;
    }

    private int countNodes(JsonNode nodes, String id) {
        if (nodes == null || !nodes.isArray()) {
            return 0;
        }
        int count = 0;
        for (JsonNode node : nodes) {
            if (id.equals(node.path("id").asText())) {
                count++;
            }
            count += countNodes(node.get("children"), id);
        }
        return count;
    }

    private int indexOfNode(JsonNode nodes, String id) {
        if (nodes == null || !nodes.isArray()) {
            return -1;
        }
        for (int i = 0; i < nodes.size(); i++) {
            if (id.equals(nodes.get(i).path("id").asText())) {
                return i;
            }
        }
        return -1;
    }

    private void assertBefore(JsonNode nodes, String firstId, String secondId) {
        int firstIndex = indexOfNode(nodes, firstId);
        int secondIndex = indexOfNode(nodes, secondId);
        assertTrue(firstIndex >= 0);
        assertTrue(secondIndex >= 0);
        assertTrue(firstIndex < secondIndex);
    }

    private String loginAdmin() throws Exception {
        JsonNode response = postJson("/api/auth/login",
                "{\"username\":\"admin\",\"password\":\"123456\"}", null);
        return response.get("data").get("token").asText();
    }

    private JsonNode postJson(String url, String json, String token) throws Exception {
        org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder builder = post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        String body = mockMvc.perform(builder)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body);
    }
}
