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
        Department department = db.departments().values().iterator().next();
        User user = db.users().values().iterator().next();

        String body = mockMvc.perform(get("/api/org/tree")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode tree = objectMapper.readTree(body).get("data");

        JsonNode departmentNode = findNode(tree, "dept-" + department.getId());
        JsonNode userNode = findNode(tree, "user-" + user.getId());

        assertNotNull(departmentNode);
        assertEquals("dept", departmentNode.get("type").asText());
        assertNotNull(userNode);
        assertEquals("user", userNode.get("type").asText());
        assertEquals(user.getId().longValue(), userNode.get("userId").asLong());
        assertEquals(user.getEmail(), userNode.get("email").asText());
    }

    private JsonNode findNode(JsonNode nodes, String id) {
        if (nodes == null || !nodes.isArray()) {
            return null;
        }
        for (JsonNode node : nodes) {
            if (id.equals(node.path("id").asText())) {
                return node;
            }
            JsonNode found = findNode(node.get("children"), id);
            if (found != null) {
                return found;
            }
        }
        return null;
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
