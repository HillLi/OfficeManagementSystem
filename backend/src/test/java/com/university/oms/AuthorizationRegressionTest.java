package com.university.oms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthorizationRegressionTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
