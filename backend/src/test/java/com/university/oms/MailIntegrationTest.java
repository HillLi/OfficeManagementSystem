package com.university.oms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.university.oms.repository.InMemoryDatabase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
