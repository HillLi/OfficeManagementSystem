package com.university.oms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.university.oms.repository.InMemoryDatabase;
import com.university.oms.security.PasswordService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InMemoryDatabase db;

    @Autowired
    private PasswordService passwordService;

    @Test
    void apiRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginReturnsTokenWithoutPassword() throws Exception {
        JsonNode response = postJson("/api/auth/login", "{\"username\":\"user\",\"password\":\"123456\"}", null);

        assertTrue(response.get("success").asBoolean());
        assertTrue(response.get("data").get("token").asText().length() > 20);
        assertFalse(response.get("data").get("user").has("password"));
    }

    @Test
    void seededUsersStoreHashedPasswords() {
        for (long id = 1L; id <= 8L; id++) {
            String password = db.users().get(id).getPassword();
            assertFalse("123456".equals(password));
            assertFalse(passwordService.needsUpgrade(password));
        }
    }

    @Test
    void repeatedLoginFailuresAreRateLimited() throws Exception {
        for (int i = 0; i < 5; i++) {
            postJsonExpectingBadRequest("/api/auth/login",
                    "{\"username\":\"missing-account\",\"password\":\"wrong\"}");
        }

        JsonNode response = postJsonExpectingBadRequest("/api/auth/login",
                "{\"username\":\"missing-account\",\"password\":\"wrong\"}");

        assertEquals("登录失败次数过多，请稍后再试", response.get("message").asText());
    }

    @Test
    void nonAdminCannotAccessAdminApi() throws Exception {
        String token = login("user");

        mockMvc.perform(get("/api/admin/users").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void ordinaryUserCanReadSafeUserOptionsWithoutAccountFields() throws Exception {
        String token = login("user");

        mockMvc.perform(get("/api/auth/user-options").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").exists())
                .andExpect(jsonPath("$.data[0].realName").exists())
                .andExpect(jsonPath("$.data[0].username").doesNotExist())
                .andExpect(jsonPath("$.data[0].roleKeys").doesNotExist());
    }

    @Test
    void ordinaryUserCanReadDepartmentOptionsForNameBasedSelections() throws Exception {
        String token = login("user");

        mockMvc.perform(get("/api/auth/dept-options").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").exists())
                .andExpect(jsonPath("$.data[0].deptName").exists());
    }

    @Test
    void approvalOperatorComesFromToken() throws Exception {
        String userToken = login("user");
        String headToken = login("head");

        JsonNode documentResponse = postJson("/api/documents",
                "{\"title\":\"关于安全测试的通知\",\"docType\":\"通知\",\"content\":\"各单位：请开展测试。\",\"applicantId\":999}",
                userToken);
        long documentId = documentResponse.get("data").get("id").asLong();

        postJson("/api/documents/" + documentId + "/submit", "{}", userToken);
        postJson("/api/approvals/document/" + documentId, "{\"operatorId\":4,\"action\":\"approve\",\"opinion\":\"同意\"}", headToken);

        JsonNode approvalResponse = getJson("/api/approvals?bizType=document&bizId=" + documentId, userToken);
        JsonNode approvals = approvalResponse.get("data");
        JsonNode last = approvals.get(approvals.size() - 1);

        assertEquals(3L, last.get("operatorId").asLong());
    }

    private String login(String username) throws Exception {
        JsonNode response = postJson("/api/auth/login",
                "{\"username\":\"" + username + "\",\"password\":\"123456\"}", null);
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

    private JsonNode postJsonExpectingBadRequest(String url, String json) throws Exception {
        String body = mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body);
    }

    private JsonNode getJson(String url, String token) throws Exception {
        String body = mockMvc.perform(get(url).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body);
    }
}
