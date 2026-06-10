package com.university.oms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DocumentUpdateTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldUpdateDraftDocument() throws Exception {
        String userToken = login("user");

        JsonNode created = postJson("/api/documents",
                "{\"title\":\"draft doc\",\"docType\":\"通知\",\"secrecyLevel\":\"公开\","
                        + "\"content\":\"original content\",\"applicantId\":2}", userToken).get("data");
        long id = created.get("id").asLong();
        assertEquals("draft", created.get("status").asText());

        JsonNode updated = putJson("/api/documents/" + id,
                "{\"title\":\"updated draft\",\"docType\":\"决定\",\"urgency\":\"加急\","
                        + "\"secrecyLevel\":\"内部\",\"knowledgeScope\":\"院系\",\"content\":\"new content\","
                        + "\"applicantId\":2}", userToken).get("data");
        assertEquals("draft", updated.get("status").asText());
        assertEquals("updated draft", updated.get("title").asText());
        assertEquals("决定", updated.get("docType").asText());
        assertEquals("加急", updated.get("urgency").asText());
        assertEquals("内部", updated.get("secrecyLevel").asText());
        assertEquals("院系", updated.get("knowledgeScope").asText());
        assertEquals("new content", updated.get("content").asText());
    }

    @Test
    void shouldUpdateRejectedDocument() throws Exception {
        String userToken = login("user");
        String headToken = login("head");

        JsonNode created = postJson("/api/documents",
                "{\"title\":\"rejection test\",\"docType\":\"通知\",\"secrecyLevel\":\"公开\","
                        + "\"content\":\"original\",\"applicantId\":2}", userToken).get("data");
        long id = created.get("id").asLong();
        postJson("/api/documents/" + id + "/submit", "{}", userToken);
        postJson("/api/approvals/document/" + id,
                "{\"action\":\"reject\",\"opinion\":\"请修改\"}", headToken);

        JsonNode updated = putJson("/api/documents/" + id,
                "{\"title\":\"fixed title\",\"docType\":\"通知\",\"secrecyLevel\":\"公开\","
                        + "\"content\":\"fixed content\",\"applicantId\":2}", userToken).get("data");
        assertEquals("rejected", updated.get("status").asText());
        assertEquals("fixed title", updated.get("title").asText());
        assertEquals("fixed content", updated.get("content").asText());
    }

    @Test
    void shouldRejectUpdateOfApprovedDocument() throws Exception {
        String userToken = login("user");
        String headToken = login("head");
        String officeToken = login("office");
        String leaderToken = login("leader");

        long id = approvedDocument(userToken, headToken, officeToken, leaderToken);

        mockMvc.perform(put("/api/documents/" + id)
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8.name())
                        .content(("{\"title\":\"hacked\",\"docType\":\"通知\",\"secrecyLevel\":\"公开\","
                                + "\"content\":\"nope\",\"applicantId\":2}").getBytes(StandardCharsets.UTF_8)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectUpdateOfPendingDocument() throws Exception {
        String userToken = login("user");

        JsonNode created = postJson("/api/documents",
                "{\"title\":\"pending doc\",\"docType\":\"通知\",\"secrecyLevel\":\"公开\","
                        + "\"content\":\"content\",\"applicantId\":2}", userToken).get("data");
        long id = created.get("id").asLong();
        postJson("/api/documents/" + id + "/submit", "{}", userToken);

        mockMvc.perform(put("/api/documents/" + id)
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8.name())
                        .content(("{\"title\":\"hacked\",\"docType\":\"通知\",\"secrecyLevel\":\"公开\","
                                + "\"content\":\"nope\",\"applicantId\":2}").getBytes(StandardCharsets.UTF_8)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectUpdateWithDisabledDocType() throws Exception {
        String userToken = login("user");
        String adminToken = login("admin");

        JsonNode created = postJson("/api/documents",
                "{\"title\":\"dict test\",\"docType\":\"通知\",\"secrecyLevel\":\"公开\","
                        + "\"content\":\"content\",\"applicantId\":2}", userToken).get("data");
        long id = created.get("id").asLong();

        putJson("/api/admin/dictionaries/types/document_type/items/通知",
                "{\"dictLabel\":\"通知\",\"dictCode\":\"通知\",\"enabled\":false}", adminToken);

        try {
            mockMvc.perform(put("/api/documents/" + id)
                            .header("Authorization", bearer(userToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8.name())
                            .content(("{\"title\":\"title\",\"docType\":\"通知\",\"secrecyLevel\":\"公开\","
                                    + "\"content\":\"content\",\"applicantId\":2}").getBytes(StandardCharsets.UTF_8)))
                    .andExpect(status().isBadRequest());
        } finally {
            putJson("/api/admin/dictionaries/types/document_type/items/通知",
                    "{\"dictLabel\":\"通知\",\"dictCode\":\"通知\",\"enabled\":true}", adminToken);
        }
    }

    @Test
    void shouldRejectUpdateWithInvalidSecrecyLevel() throws Exception {
        String userToken = login("user");

        JsonNode created = postJson("/api/documents",
                "{\"title\":\"secrecy test\",\"docType\":\"通知\",\"secrecyLevel\":\"公开\","
                        + "\"content\":\"content\",\"applicantId\":2}", userToken).get("data");
        long id = created.get("id").asLong();

        mockMvc.perform(put("/api/documents/" + id)
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8.name())
                        .content(("{\"title\":\"title\",\"docType\":\"通知\",\"secrecyLevel\":\"不存在的密级\","
                                + "\"content\":\"content\",\"applicantId\":2}").getBytes(StandardCharsets.UTF_8)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldPersistAllUpdatedFields() throws Exception {
        String userToken = login("user");

        JsonNode created = postJson("/api/documents",
                "{\"title\":\"original\",\"docType\":\"通知\",\"secrecyLevel\":\"公开\","
                        + "\"content\":\"original content\",\"applicantId\":2}", userToken).get("data");
        long id = created.get("id").asLong();

        putJson("/api/documents/" + id,
                "{\"title\":\"new title\",\"docType\":\"函\",\"urgency\":\"特急\","
                        + "\"secrecyLevel\":\"秘密\",\"knowledgeScope\":\"保密范围\","
                        + "\"content\":\"new body\",\"applicantId\":2}", userToken);

        JsonNode reloaded = getJson("/api/documents", userToken).get("data");
        JsonNode doc = findById(reloaded, id);
        assertEquals("new title", doc.get("title").asText());
        assertEquals("函", doc.get("docType").asText());
        assertEquals("特急", doc.get("urgency").asText());
        assertEquals("秘密", doc.get("secrecyLevel").asText());
        assertEquals("保密范围", doc.get("knowledgeScope").asText());
        assertEquals("new body", doc.get("content").asText());
    }

    @Test
    void shouldRecordApprovalHistoryWithUpdateAction() throws Exception {
        String userToken = login("user");

        JsonNode created = postJson("/api/documents",
                "{\"title\":\"history test\",\"docType\":\"通知\",\"secrecyLevel\":\"公开\","
                        + "\"content\":\"content\",\"applicantId\":2}", userToken).get("data");
        long id = created.get("id").asLong();

        putJson("/api/documents/" + id,
                "{\"title\":\"updated\",\"docType\":\"通知\",\"secrecyLevel\":\"公开\","
                        + "\"content\":\"updated content\",\"applicantId\":2}", userToken);

        JsonNode history = getJson("/api/approvals?bizType=document&bizId=" + id, userToken).get("data");
        boolean hasCreate = false;
        boolean hasUpdate = false;
        for (JsonNode row : history) {
            String action = row.get("action").asText();
            if ("create".equals(action)) hasCreate = true;
            if ("update".equals(action)) hasUpdate = true;
        }
        assertTrue(hasCreate, "approval history should contain the initial create action");
        assertTrue(hasUpdate, "approval history should contain the update action");
    }

    @Test
    void shouldCreateAuditLogOnUpdate() throws Exception {
        String userToken = login("user");
        String adminToken = login("admin");

        JsonNode created = postJson("/api/documents",
                "{\"title\":\"audit test\",\"docType\":\"通知\",\"secrecyLevel\":\"公开\","
                        + "\"content\":\"content\",\"applicantId\":2}", userToken).get("data");
        long id = created.get("id").asLong();

        putJson("/api/documents/" + id,
                "{\"title\":\"audited title\",\"docType\":\"通知\",\"secrecyLevel\":\"公开\","
                        + "\"content\":\"updated\",\"applicantId\":2}", userToken);

        JsonNode logs = getJson("/api/workflow/audit-logs?bizType=document&bizId=" + id, adminToken).get("data");
        boolean found = false;
        for (JsonNode log : logs) {
            if ("update".equals(log.get("action").asText())
                    && "document".equals(log.get("module").asText())) {
                found = true;
            }
        }
        assertTrue(found, "audit log should contain an update entry for the document");
    }

    private long approvedDocument(String userToken, String headToken, String officeToken, String leaderToken)
            throws Exception {
        JsonNode document = postJson("/api/documents",
                "{\"title\":\"update approval test\",\"docType\":\"通知\",\"secrecyLevel\":\"公开\","
                        + "\"content\":\"approval test content\",\"applicantId\":2}", userToken).get("data");
        long id = document.get("id").asLong();
        postJson("/api/documents/" + id + "/submit", "{}", userToken);
        postJson("/api/approvals/document/" + id, "{\"action\":\"approve\",\"opinion\":\"同意\"}", headToken);
        postJson("/api/approvals/document/" + id, "{\"action\":\"approve\",\"opinion\":\"同意\"}", officeToken);
        postJson("/api/approvals/document/" + id, "{\"action\":\"approve\",\"opinion\":\"同意\"}", leaderToken);
        return id;
    }

    private JsonNode findById(JsonNode array, long id) {
        for (JsonNode node : array) {
            if (node.get("id").asLong() == id) {
                return node;
            }
        }
        throw new AssertionError("document with id " + id + " not found");
    }

    private String login(String username) throws Exception {
        return postJson("/api/auth/login",
                "{\"username\":\"" + username + "\",\"password\":\"123456\"}", null)
                .get("data").get("token").asText();
    }

    private JsonNode postJson(String url, String json, String token) throws Exception {
        org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request = post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8.name())
                .content(json.getBytes(StandardCharsets.UTF_8));
        if (token != null) {
            request.header("Authorization", bearer(token));
        }
        String body = mockMvc.perform(request).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readTree(body);
    }

    private JsonNode putJson(String url, String json, String token) throws Exception {
        org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request = put(url)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8.name())
                .content(json.getBytes(StandardCharsets.UTF_8));
        if (token != null) {
            request.header("Authorization", bearer(token));
        }
        String body = mockMvc.perform(request).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readTree(body);
    }

    private JsonNode getJson(String url, String token) throws Exception {
        String body = mockMvc.perform(get(url).header("Authorization", bearer(token)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readTree(body);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
