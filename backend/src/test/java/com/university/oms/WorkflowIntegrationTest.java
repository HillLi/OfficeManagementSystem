package com.university.oms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class WorkflowIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void documentLifecycleCreatesTasksNotificationsAttachmentsAuditAndArchive() throws Exception {
        String userToken = login("user");
        String headToken = login("head");
        String officeToken = login("office");
        String leaderToken = login("leader");
        String adminToken = login("admin");

        JsonNode created = postJson("/api/documents",
                "{\"title\":\"Workflow Test\",\"docType\":\"通知\",\"content\":\"content\",\"applicantId\":2}",
                userToken);
        long documentId = created.get("data").get("id").asLong();

        postJson("/api/documents/" + documentId + "/submit", "{}", userToken);

        JsonNode headTasks = getJson("/api/workflow/tasks?onlyMine=true", headToken).get("data");
        assertTrue(contains(headTasks, "bizType", "document", "bizId", documentId, "nodeKey", "pending_dept"));

        JsonNode headNotifications = getJson("/api/workflow/notifications?unreadOnly=false", headToken).get("data");
        assertTrue(contains(headNotifications, "bizType", "document", "bizId", documentId));

        postJson("/api/workflow/attachments",
                "{\"bizType\":\"document\",\"bizId\":" + documentId
                        + ",\"fileName\":\"draft.pdf\",\"fileUrl\":\"/uploads/draft.pdf\",\"secrecyLevel\":\"内部\"}",
                userToken);
        JsonNode attachments = getJson("/api/workflow/attachments?bizType=document&bizId=" + documentId, userToken)
                .get("data");
        assertTrue(contains(attachments, "fileName", "draft.pdf", "bizId", documentId));

        mockMvc.perform(get("/api/workflow/audit-logs?bizType=document&bizId=" + documentId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
        JsonNode auditLogs = getJson("/api/workflow/audit-logs?bizType=document&bizId=" + documentId, adminToken)
                .get("data");
        assertTrue(contains(auditLogs, "action", "upload_attachment", "bizId", documentId));

        postJson("/api/approvals/document/" + documentId, "{\"action\":\"approve\",\"opinion\":\"ok\"}", headToken);
        JsonNode officeTasks = getJson("/api/workflow/tasks?onlyMine=true", officeToken).get("data");
        assertTrue(contains(officeTasks, "bizType", "document", "bizId", documentId, "nodeKey", "pending_office"));

        postJson("/api/approvals/document/" + documentId, "{\"action\":\"approve\",\"opinion\":\"ok\"}", officeToken);
        postJson("/api/approvals/document/" + documentId, "{\"action\":\"approve\",\"opinion\":\"ok\"}", leaderToken);
        JsonNode archived = postJson("/api/documents/" + documentId + "/archive", "{}", officeToken).get("data");
        assertEquals("archived", archived.get("status").asText());

        JsonNode instances = getJson("/api/workflow/instances", adminToken).get("data");
        assertTrue(contains(instances, "bizType", "document", "bizId", documentId, "status", "archived"));
    }

    private boolean contains(JsonNode rows, String key1, String value1, String key2, long value2) {
        for (JsonNode row : rows) {
            if (value1.equals(row.get(key1).asText()) && value2 == row.get(key2).asLong()) {
                return true;
            }
        }
        return false;
    }

    private boolean contains(JsonNode rows, String key1, String value1, String key2, long value2,
                             String key3, String value3) {
        for (JsonNode row : rows) {
            if (value1.equals(row.get(key1).asText()) && value2 == row.get(key2).asLong()
                    && value3.equals(row.get(key3).asText())) {
                return true;
            }
        }
        return false;
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

    private JsonNode getJson(String url, String token) throws Exception {
        String body = mockMvc.perform(get(url).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body);
    }
}
