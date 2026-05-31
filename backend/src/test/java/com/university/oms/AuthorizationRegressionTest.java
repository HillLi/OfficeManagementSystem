package com.university.oms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.university.oms.model.User;
import com.university.oms.repository.InMemoryDatabase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
@AutoConfigureMockMvc
class AuthorizationRegressionTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InMemoryDatabase db;

    @Test
    void financeCannotSubmitAnotherUsersDocument() throws Exception {
        String userToken = login("user");
        String financeToken = login("finance");
        long documentId = createSecretDocument(userToken);

        mockMvc.perform(post("/api/documents/" + documentId + "/submit")
                        .header("Authorization", bearer(financeToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void financeCannotReadAnotherUsersSecretAttachment() throws Exception {
        String userToken = login("user");
        String financeToken = login("finance");
        long documentId = createSecretDocument(userToken);
        postJson("/api/workflow/attachments",
                "{\"bizType\":\"document\",\"bizId\":" + documentId
                        + ",\"fileName\":\"restricted.pdf\",\"fileUrl\":\"/secure/restricted.pdf\",\"secrecyLevel\":\"秘密\"}",
                userToken);

        mockMvc.perform(get("/api/workflow/attachments?bizType=document&bizId=" + documentId)
                        .header("Authorization", bearer(financeToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void financeCannotReadAnotherUsersApprovalHistory() throws Exception {
        String userToken = login("user");
        String financeToken = login("finance");
        long documentId = createSecretDocument(userToken);

        mockMvc.perform(get("/api/approvals?bizType=document&bizId=" + documentId)
                        .header("Authorization", bearer(financeToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void otherDepartmentHeadCannotSeeOrApproveApplicantDepartmentTask() throws Exception {
        ensureOtherDepartmentHead();
        String userToken = login("user");
        String otherHeadToken = login("financeHead");
        long documentId = createSecretDocument(userToken);

        postJson("/api/documents/" + documentId + "/submit", "{}", userToken);

        JsonNode tasks = getJson("/api/workflow/tasks?onlyMine=true", otherHeadToken).get("data");
        assertFalse(contains(tasks, "bizType", "document", "bizId", documentId));

        mockMvc.perform(post("/api/approvals/document/" + documentId)
                        .header("Authorization", bearer(otherHeadToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\":\"approve\",\"opinion\":\"跨部门审批\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void loggedOutTokenCannotAccessProtectedApi() throws Exception {
        String token = login("user");

        mockMvc.perform(post("/api/auth/logout").header("Authorization", bearer(token)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/documents").header("Authorization", bearer(token)))
                .andExpect(status().isUnauthorized());
    }

    private long createSecretDocument(String userToken) throws Exception {
        JsonNode response = postJson("/api/documents",
                "{\"title\":\"Restricted document\",\"docType\":\"通知\",\"secrecyLevel\":\"秘密\","
                        + "\"knowledgeScope\":\"课题组\",\"content\":\"controlled content\",\"applicantId\":2}",
                userToken);
        return response.get("data").get("id").asLong();
    }

    private void ensureOtherDepartmentHead() {
        if (db.users().containsKey(991L)) {
            return;
        }
        User user = new User();
        db.fill(user, 991L);
        user.setUsername("financeHead");
        user.setPassword("123456");
        user.setRealName("财务部门负责人");
        user.setDeptId(2L);
        user.setDeptName(db.departments().get(2L).getDeptName());
        user.getRoleKeys().add("dept_head");
        db.users().put(user.getId(), user);
    }

    private boolean contains(JsonNode rows, String key1, String value1, String key2, long value2) {
        for (JsonNode row : rows) {
            if (value1.equals(row.get(key1).asText()) && value2 == row.get(key2).asLong()) {
                return true;
            }
        }
        return false;
    }

    private JsonNode getJson(String url, String token) throws Exception {
        String body = mockMvc.perform(get(url).header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body);
    }

    private String login(String username) throws Exception {
        JsonNode response = postJson("/api/auth/login",
                "{\"username\":\"" + username + "\",\"password\":\"123456\"}", null);
        return response.get("data").get("token").asText();
    }

    private JsonNode postJson(String url, String json, String token) throws Exception {
        org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request = post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);
        if (token != null) {
            request.header("Authorization", bearer(token));
        }
        String body = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
