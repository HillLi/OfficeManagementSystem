package com.university.oms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DocumentClosureTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void approvedDocumentCanBeDistributedReceivedAndReminded() throws Exception {
        String userToken = login("user");
        String headToken = login("head");
        String officeToken = login("office");
        String leaderToken = login("leader");

        long documentId = approvedDocument(userToken, headToken, officeToken, leaderToken);
        JsonNode distribution = postJson("/api/documents/" + documentId + "/distributions",
                "{\"receiverId\":2,\"receiverDeptId\":4}", officeToken).get("data");
        long distributionId = distribution.get("id").asLong();
        assertEquals("distributed", distribution.get("status").asText());

        JsonNode unsigned = getJson("/api/documents/" + documentId + "/distributions", userToken).get("data");
        assertTrue(contains(unsigned, "status", "distributed", "id", distributionId));

        postJson("/api/documents/" + documentId + "/distributions/" + distributionId + "/remind", "{}", officeToken);
        JsonNode notifications = getJson("/api/workflow/notifications?unreadOnly=false", userToken).get("data");
        assertTrue(contains(notifications, "bizType", "document", "bizId", documentId));

        JsonNode received = postJson("/api/documents/" + documentId + "/distributions/" + distributionId + "/receipt",
                "{}", userToken).get("data");
        assertEquals("received", received.get("status").asText());
    }

    @Test
    void assignedReceiverCanSeeAndReceiptDistributedDocument() throws Exception {
        String userToken = login("user");
        String headToken = login("head");
        String officeToken = login("office");
        String leaderToken = login("leader");
        String keeperToken = login("keeper");

        long documentId = approvedDocument(userToken, headToken, officeToken, leaderToken);
        JsonNode distribution = postJson("/api/documents/" + documentId + "/distributions",
                "{\"receiverId\":8,\"receiverDeptId\":1}", officeToken).get("data");
        JsonNode documents = getJson("/api/documents", keeperToken).get("data");
        JsonNode distributions = getJson("/api/documents/" + documentId + "/distributions", keeperToken).get("data");

        assertTrue(contains(documents, "status", "approved", "id", documentId));
        assertTrue(contains(distributions, "status", "distributed", "id", distribution.get("id").asLong()));
        JsonNode received = postJson("/api/documents/" + documentId + "/distributions/"
                + distribution.get("id").asLong() + "/receipt", "{}", keeperToken).get("data");
        assertEquals("received", received.get("status").asText());
    }

    @Test
    void rejectedDocumentResubmissionIncrementsVersionAndKeepsRejectionHistory() throws Exception {
        String userToken = login("user");
        String headToken = login("head");

        JsonNode document = postJson("/api/documents",
                "{\"title\":\"关于版本追踪的通知\",\"docType\":\"通知\",\"secrecyLevel\":\"公开\","
                        + "\"content\":\"版本追踪测试正文内容，用于确认退回后重新提交生成新的业务版本记录。\","
                        + "\"applicantId\":2}", userToken).get("data");
        long id = document.get("id").asLong();
        postJson("/api/documents/" + id + "/submit", "{}", userToken);
        postJson("/api/approvals/document/" + id, "{\"action\":\"reject\",\"opinion\":\"请修改后重报\"}", headToken);

        JsonNode resubmitted = postJson("/api/documents/" + id + "/submit", "{}", userToken).get("data");
        assertTrue(resubmitted.has("version"));
        assertEquals(2, resubmitted.get("version").asInt());

        JsonNode history = getJson("/api/approvals?bizType=document&bizId=" + id, userToken).get("data");
        assertTrue(contains(history, "action", "reject", "bizId", id));
    }

    private long approvedDocument(String userToken, String headToken, String officeToken, String leaderToken)
            throws Exception {
        JsonNode document = postJson("/api/documents",
                "{\"title\":\"关于分发签收的通知\",\"docType\":\"通知\",\"secrecyLevel\":\"公开\","
                        + "\"content\":\"公开公文分发签收测试正文内容，审批完成后由党办校办人员发起分发。\","
                        + "\"applicantId\":2}", userToken).get("data");
        long id = document.get("id").asLong();
        postJson("/api/documents/" + id + "/submit", "{}", userToken);
        postJson("/api/approvals/document/" + id, "{\"action\":\"approve\",\"opinion\":\"同意\"}", headToken);
        postJson("/api/approvals/document/" + id, "{\"action\":\"approve\",\"opinion\":\"同意\"}", officeToken);
        postJson("/api/approvals/document/" + id, "{\"action\":\"approve\",\"opinion\":\"同意\"}", leaderToken);
        return id;
    }

    private boolean contains(JsonNode rows, String key1, String value1, String key2, long value2) {
        for (JsonNode row : rows) {
            if (value1.equals(row.get(key1).asText()) && value2 == row.get(key2).asLong()) {
                return true;
            }
        }
        return false;
    }

    private String login(String username) throws Exception {
        return postJson("/api/auth/login",
                "{\"username\":\"" + username + "\",\"password\":\"123456\"}", null)
                .get("data").get("token").asText();
    }

    private JsonNode postJson(String url, String json, String token) throws Exception {
        org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request = post(url)
                .contentType(MediaType.APPLICATION_JSON).content(json);
        if (token != null) {
            request.header("Authorization", bearer(token));
        }
        String body = mockMvc.perform(request).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body);
    }

    private JsonNode getJson(String url, String token) throws Exception {
        String body = mockMvc.perform(get(url).header("Authorization", bearer(token)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
