package com.university.oms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ClosureAndAiSecurityTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void financeCannotArchiveAnotherUsersApprovedMeeting() throws Exception {
        String userToken = login("user");
        String headToken = login("head");
        String financeToken = login("finance");

        JsonNode meeting = postJson("/api/meetings",
                "{\"title\":\"闭环权限会议\",\"roomId\":1,\"startTime\":\"2026-08-01T09:00:00\","
                        + "\"endTime\":\"2026-08-01T10:00:00\",\"organizerId\":2,\"expectedCount\":10,"
                        + "\"venueType\":\"室内\"}", userToken);
        long id = meeting.get("data").get("id").asLong();
        postJson("/api/approvals/meeting/" + id, "{\"action\":\"approve\",\"opinion\":\"同意\"}", headToken);

        mockMvc.perform(post("/api/meetings/" + id + "/minutes")
                        .header("Authorization", bearer(financeToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"minutes\":\"无权归档内容\",\"signInCount\":8}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void financeCannotReplyToAnotherUsersApprovedReport() throws Exception {
        String userToken = login("user");
        String officeToken = login("office");
        String headToken = login("head");
        String financeToken = login("finance");

        JsonNode report = postJson("/api/reports",
                "{\"title\":\"内部请示\",\"type\":\"请示\",\"secrecyLevel\":\"内部\","
                        + "\"content\":\"需要办理的内部事项\",\"applicantId\":2}", userToken);
        long id = report.get("data").get("id").asLong();
        postJson("/api/approvals/report/" + id, "{\"action\":\"approve\",\"opinion\":\"通过\"}", officeToken);
        postJson("/api/approvals/report/" + id, "{\"action\":\"approve\",\"opinion\":\"通过\"}", headToken);

        mockMvc.perform(post("/api/reports/" + id + "/reply")
                        .header("Authorization", bearer(financeToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reply\":\"无权批复\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void financeCannotReimburseAnotherUsersApprovedTravel() throws Exception {
        String userToken = login("user");
        String headToken = login("head");
        String financeToken = login("finance");

        JsonNode travel = postJson("/api/travels",
                "{\"applicantId\":2,\"destination\":\"上海\",\"startDate\":\"2026-08-03\","
                        + "\"endDate\":\"2026-08-04\",\"reason\":\"会议\",\"staffLevel\":\"三类\","
                        + "\"travelType\":\"教学科研业务\",\"transport\":\"高铁二等座\",\"budget\":1200}", userToken);
        long id = travel.get("data").get("id").asLong();
        postJson("/api/approvals/travel/" + id, "{\"action\":\"approve\",\"opinion\":\"同意\"}", headToken);
        postJson("/api/approvals/travel/" + id, "{\"action\":\"approve\",\"opinion\":\"同意\"}", financeToken);

        mockMvc.perform(post("/api/travels/" + id + "/reimburse")
                        .header("Authorization", bearer(financeToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"actualExpense\":880,\"receiptUrl\":\"/receipts/illegal.pdf\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void internalDocumentAiReviewIsBlockedAndPublicReviewIsAudited() throws Exception {
        String userToken = login("user");
        String adminToken = login("admin");

        JsonNode internal = postJson("/api/documents",
                "{\"title\":\"关于内部材料的通知\",\"docType\":\"通知\",\"secrecyLevel\":\"内部\","
                        + "\"content\":\"这是需要在限定知悉范围内处理的内部材料正文，禁止直接提供给外部AI服务。\","
                        + "\"applicantId\":2}", userToken);
        long internalId = internal.get("data").get("id").asLong();
        JsonNode blocked = postJson("/api/documents/" + internalId + "/ai-review", "{}", userToken).get("data");
        assertFalse(blocked.get("passed").asBoolean());

        JsonNode importedInternal = postJson("/api/documents",
                "{\"title\":\"关于导入历史资料的通知\",\"docType\":\"通知\",\"secrecyLevel\":\"internal\","
                        + "\"content\":\"这是由历史系统导入的内部资料正文内容，必须保持与中文密级数据相同的外部智能服务访问限制。\","
                        + "\"applicantId\":2}", userToken);
        long importedInternalId = importedInternal.get("data").get("id").asLong();
        JsonNode importedBlocked = postJson("/api/documents/" + importedInternalId + "/ai-review", "{}", userToken).get("data");
        assertFalse(importedBlocked.get("passed").asBoolean());

        JsonNode publicDocument = postJson("/api/documents",
                "{\"title\":\"关于公开通知发布的通知\",\"docType\":\"通知\",\"secrecyLevel\":\"公开\","
                        + "\"content\":\"这是一份公开发布的常规通知正文，用于验证流程操作审计记录是否能够正确生成并查询。\","
                        + "\"applicantId\":2}", userToken);
        long publicId = publicDocument.get("data").get("id").asLong();
        postJson("/api/documents/" + publicId + "/ai-review", "{}", userToken);

        JsonNode logs = getJson("/api/workflow/audit-logs?bizType=document&bizId=" + publicId, adminToken).get("data");
        assertTrue(contains(logs, "action", "ai_review", "bizId", publicId));
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
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);
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
