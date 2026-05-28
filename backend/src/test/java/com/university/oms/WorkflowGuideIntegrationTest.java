package com.university.oms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class WorkflowGuideIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // --- Task 1: Permission gate and basic shape ---

    @Test
    void forbiddenUserCannotViewWorkflowGuideForUnrelatedBusiness() throws Exception {
        String userToken = login("user");
        String financeToken = login("finance");

        JsonNode document = postJson("/api/documents",
                "{\"title\":\"流程导览权限测试\",\"docType\":\"通知\",\"secrecyLevel\":\"公开\","
                        + "\"content\":\"用于验证无关角色不能查看流程导览。\",\"applicantId\":2}",
                userToken).get("data");

        mockMvc.perform(get("/api/workflow/guide?bizType=document&bizId=" + document.get("id").asLong())
                        .header("Authorization", bearer(financeToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void draftDocumentGuideReturnsBasicShapeBeforeFlowStarts() throws Exception {
        String userToken = login("user");

        JsonNode document = postJson("/api/documents",
                "{\"title\":\"流程导览草稿测试\",\"docType\":\"通知\",\"secrecyLevel\":\"公开\","
                        + "\"content\":\"用于验证草稿状态也能看到基础路径。\",\"applicantId\":2}",
                userToken).get("data");

        JsonNode guide = getJson("/api/workflow/guide?bizType=document&bizId="
                + document.get("id").asLong(), userToken).get("data");

        assertEquals("document", guide.get("bizType").asText());
        assertEquals(document.get("id").asLong(), guide.get("bizId").asLong());
        assertTrue(guide.get("title").asText().contains("流程导览草稿测试"));
        assertTrue(containsStep(guide.get("steps"), "create", "公文起草", "done"));
        assertTrue(containsStep(guide.get("steps"), "ai_review", "AI格式校验", "optional"));
        assertTrue(containsStep(guide.get("steps"), "submit", "提交审批", "waiting"));
    }

    // --- Task 2: Document lifecycle completion ---

    @Test
    void documentGuideShowsDesignPathWithDistributionReceiptAndArchive() throws Exception {
        String userToken = login("user");
        String headToken = login("head");
        String officeToken = login("office");
        String leaderToken = login("leader");

        JsonNode document = postJson("/api/documents",
                "{\"title\":\"流程导览公文闭环\",\"docType\":\"通知\",\"secrecyLevel\":\"公开\","
                        + "\"content\":\"用于验证公文导览路径。\",\"applicantId\":2}",
                userToken).get("data");
        long id = document.get("id").asLong();

        postJson("/api/documents/" + id + "/ai-review", "{}", userToken);
        postJson("/api/documents/" + id + "/submit", "{}", userToken);
        postJson("/api/approvals/document/" + id, "{\"action\":\"approve\",\"opinion\":\"部门同意\"}", headToken);
        postJson("/api/approvals/document/" + id, "{\"action\":\"approve\",\"opinion\":\"校办同意\"}", officeToken);
        postJson("/api/approvals/document/" + id, "{\"action\":\"approve\",\"opinion\":\"签发\"}", leaderToken);

        JsonNode approvedGuide = getJson("/api/workflow/guide?bizType=document&bizId=" + id, userToken).get("data");
        assertEquals("distribute", approvedGuide.get("currentNodeKey").asText());
        assertTrue(containsStep(approvedGuide.get("steps"), "distribute", "公文分发", "current"));

        JsonNode distribution = postJson("/api/documents/" + id + "/distributions",
                "{\"receiverId\":2,\"receiverDeptId\":4}", officeToken).get("data");
        postJson("/api/documents/" + id + "/distributions/" + distribution.get("id").asLong() + "/receipt",
                "{}", userToken);
        postJson("/api/documents/" + id + "/archive", "{}", officeToken);

        JsonNode guide = getJson("/api/workflow/guide?bizType=document&bizId=" + id, userToken).get("data");

        assertTrue(containsStep(guide.get("steps"), "ai_review", "AI格式校验", "done"));
        assertTrue(containsStep(guide.get("steps"), "pending_dept", "部门负责人审批", "done"));
        assertTrue(containsStep(guide.get("steps"), "pending_office", "党办校办审核", "done"));
        assertTrue(containsStep(guide.get("steps"), "pending_leader", "校级领导签发", "done"));
        assertTrue(containsStep(guide.get("steps"), "distribute", "公文分发", "done"));
        assertTrue(containsStep(guide.get("steps"), "receipt", "接收人签收", "done"));
        assertTrue(containsStep(guide.get("steps"), "archive", "公文归档", "done"));
        assertTrue(stepContainsOpinion(guide.get("steps"), "pending_office", "校办同意"));
    }

    // --- Task 3: Seal and Travel guide paths ---

    @Test
    void sealGuideUsesMajorAndSchoolSealRoutes() throws Exception {
        String userToken = login("user");

        JsonNode deptMajor = postJson("/api/seals/applications",
                "{\"sealId\":2,\"applicantId\":2,\"purpose\":\"部门重大事项\",\"copies\":1,"
                        + "\"takeOut\":false,\"matterLevel\":\"重大事项\"}",
                userToken).get("data");
        JsonNode schoolMajor = postJson("/api/seals/applications",
                "{\"sealId\":1,\"applicantId\":2,\"purpose\":\"校级重大事项\",\"copies\":1,"
                        + "\"takeOut\":false,\"matterLevel\":\"重大事项\"}",
                userToken).get("data");

        JsonNode deptGuide = getJson("/api/workflow/guide?bizType=seal&bizId="
                + deptMajor.get("id").asLong(), userToken).get("data");
        JsonNode schoolGuide = getJson("/api/workflow/guide?bizType=seal&bizId="
                + schoolMajor.get("id").asLong(), userToken).get("data");

        assertTrue(containsStep(deptGuide.get("steps"), "pending_dept", "部门负责人审批", "waiting"));
        assertTrue(containsStep(deptGuide.get("steps"), "pending_office", "党办校办审核", "waiting"));
        assertTrue(containsStep(schoolGuide.get("steps"), "pending_office", "党办校办审核", "waiting"));
        assertTrue(containsStep(schoolGuide.get("steps"), "pending_leader", "校级领导审批", "waiting"));
    }

    @Test
    void travelGuideSeparatesApprovalFromReimbursementReview() throws Exception {
        String userToken = login("user");
        String headToken = login("head");
        String financeToken = login("finance");

        JsonNode travel = postJson("/api/travels",
                "{\"applicantId\":2,\"destination\":\"上海\",\"startDate\":\"2026-06-01\","
                        + "\"endDate\":\"2026-06-03\",\"reason\":\"参加会议\","
                        + "\"staffLevel\":\"三类\",\"travelType\":\"教学科研业务\","
                        + "\"transport\":\"高铁二等座\",\"budget\":2600}",
                userToken).get("data");
        long id = travel.get("id").asLong();
        postJson("/api/approvals/travel/" + id, "{\"action\":\"approve\",\"opinion\":\"同意出差\"}", headToken);
        postJson("/api/approvals/travel/" + id, "{\"action\":\"approve\",\"opinion\":\"预算通过\"}", financeToken);
        postJson("/api/travels/" + id + "/reimburse",
                "{\"actualExpense\":1200,\"receiptUrl\":\"/uploads/ticket.pdf\",\"overLimitReason\":\"\"}",
                userToken);

        JsonNode guide = getJson("/api/workflow/guide?bizType=travel&bizId=" + id, userToken).get("data");

        assertTrue(containsStep(guide.get("steps"), "submit_reimbursement", "提交报销", "done"));
        assertTrue(containsStep(guide.get("steps"), "finance_recheck", "财务复核", "current"));

        postJson("/api/approvals/travel/" + id, "{\"action\":\"approve\",\"opinion\":\"复核通过\"}", financeToken);
        JsonNode archivedGuide = getJson("/api/workflow/guide?bizType=travel&bizId=" + id, userToken).get("data");
        assertTrue(containsStep(archivedGuide.get("steps"), "finance_recheck", "财务复核", "done"));
        assertTrue(stepContainsOpinion(archivedGuide.get("steps"), "finance_recheck", "复核通过"));
    }

    // --- Task 4: Rejection and node details ---

    @Test
    void rejectedDocumentGuideKeepsOpinionAndMarksRejectedNode() throws Exception {
        String userToken = login("user");
        String headToken = login("head");

        JsonNode document = postJson("/api/documents",
                "{\"title\":\"流程导览退回测试\",\"docType\":\"通知\",\"secrecyLevel\":\"公开\","
                        + "\"content\":\"用于验证退回意见。\",\"applicantId\":2}",
                userToken).get("data");
        long id = document.get("id").asLong();
        postJson("/api/documents/" + id + "/submit", "{}", userToken);
        postJson("/api/approvals/document/" + id,
                "{\"action\":\"reject\",\"opinion\":\"请补充附件后重报\"}", headToken);

        JsonNode guide = getJson("/api/workflow/guide?bizType=document&bizId=" + id, userToken).get("data");

        assertTrue(containsStep(guide.get("steps"), "pending_dept", "部门负责人审批", "rejected"));
        assertTrue(stepContainsOpinion(guide.get("steps"), "pending_dept", "请补充附件后重报"));
    }

    // --- Helpers ---

    private boolean containsStep(JsonNode steps, String key, String label, String status) {
        for (JsonNode step : steps) {
            if (key.equals(step.get("key").asText())
                    && label.equals(step.get("label").asText())
                    && status.equals(step.get("status").asText())) {
                return true;
            }
        }
        return false;
    }

    private boolean stepContainsOpinion(JsonNode steps, String key, String opinion) {
        for (JsonNode step : steps) {
            if (key.equals(step.get("key").asText()) && step.has("opinion")
                    && opinion.equals(step.get("opinion").asText())) {
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
