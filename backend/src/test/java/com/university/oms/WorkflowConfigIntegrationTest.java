package com.university.oms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 审批流程配置功能集成测试：验证管理员修改流程配置后，各业务的审批流转按新配置执行
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WorkflowConfigIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // 各流程的默认配置（用于测试后恢复）
    private static final String DOCUMENT_DEFAULT =
            "[{\"nodeKey\":\"pending_dept\",\"nodeLabel\":\"部门负责人审批\",\"roleKey\":\"dept_head\"},"
                    + "{\"nodeKey\":\"pending_office\",\"nodeLabel\":\"党办校办审核\",\"roleKey\":\"office_admin\"},"
                    + "{\"nodeKey\":\"pending_leader\",\"nodeLabel\":\"校级领导签发\",\"roleKey\":\"school_leader\"}]";
    private static final String TRAVEL_DEFAULT =
            "[{\"nodeKey\":\"pending_dept\",\"nodeLabel\":\"部门负责人审批\",\"roleKey\":\"dept_head\"},"
                    + "{\"nodeKey\":\"pending_finance\",\"nodeLabel\":\"财务处审批\",\"roleKey\":\"finance_staff\"}]";

    @AfterEach
    void restoreDefaults() throws Exception {
        String adminToken = login("admin");
        saveFlow("document", DOCUMENT_DEFAULT, adminToken);
        saveFlow("travel", TRAVEL_DEFAULT, adminToken);
    }

    @Test
    void adminCanQueryFlowKeysAndNodes() throws Exception {
        String adminToken = login("admin");
        JsonNode flowKeys = getJson("/api/admin/workflow/flow-keys", adminToken).get("data");
        assertTrue(flowKeys.has("document"), "应包含公文流程");
        assertTrue(flowKeys.has("travel"), "应包含差旅流程");
        assertTrue(flowKeys.has("meeting_large"), "应包含大型活动流程");

        JsonNode grouped = getJson("/api/admin/workflow/nodes", adminToken).get("data");
        JsonNode documentNodes = grouped.get("document");
        assertNotNull(documentNodes, "公文流程节点应存在");
        assertEquals(3, documentNodes.size(), "公文默认应有3个审批步骤");
        assertEquals("pending_dept", documentNodes.get(0).get("nodeKey").asText());
        assertEquals("school_leader", documentNodes.get(2).get("roleKey").asText());
    }

    @Test
    void nonAdminCannotConfigureFlow() throws Exception {
        String userToken = login("user");
        mockMvc.perform(put("/api/admin/workflow/flow-key/document")
                        .contentType(MediaType.APPLICATION_JSON).content(DOCUMENT_DEFAULT)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void documentFlowAddSecurityStepRequiresSecurityRole() throws Exception {
        String userToken = login("user");
        String headToken = login("head");
        String securityToken = login("security");
        String officeToken = login("office");
        String leaderToken = login("leader");
        String adminToken = login("admin");

        // 配置公文流程：部门 -> 党办 -> 保卫处 -> 校领导（在党办后插入保卫处）
        saveFlow("document",
                "[{\"nodeKey\":\"pending_dept\",\"nodeLabel\":\"部门负责人审批\",\"roleKey\":\"dept_head\"},"
                        + "{\"nodeKey\":\"pending_office\",\"nodeLabel\":\"党办校办审核\",\"roleKey\":\"office_admin\"},"
                        + "{\"nodeKey\":\"pending_security\",\"nodeLabel\":\"保卫处复核\",\"roleKey\":\"security_staff\"},"
                        + "{\"nodeKey\":\"pending_leader\",\"nodeLabel\":\"校级领导签发\",\"roleKey\":\"school_leader\"}]",
                adminToken);

        long docId = createDocument(userToken);
        submitDocument(docId, userToken);

        // 1. 部门负责人审批通过 -> 应进入 pending_office（从审批响应直接读状态）
        assertEquals("pending_office", approve("document", docId, headToken).get("status").asText());
        // 2. 党办校办审批通过 -> 应进入新增的 pending_security（而非原来的 pending_leader）
        assertEquals("pending_security", approve("document", docId, officeToken).get("status").asText());
        // 3. 保卫处审批通过 -> 进入 pending_leader
        assertEquals("pending_leader", approve("document", docId, securityToken).get("status").asText());
        // 4. 校领导审批通过 -> approved
        assertEquals("approved", approve("document", docId, leaderToken).get("status").asText());
    }

    @Test
    void documentFlowWrongRoleCannotApprove() throws Exception {
        String userToken = login("user");
        String adminToken = login("admin");

        // 把公文第一步 pending_dept 的审批角色改为 office_admin
        saveFlow("document",
                "[{\"nodeKey\":\"pending_dept\",\"nodeLabel\":\"部门负责人审批\",\"roleKey\":\"office_admin\"},"
                        + "{\"nodeKey\":\"pending_office\",\"nodeLabel\":\"党办校办审核\",\"roleKey\":\"office_admin\"},"
                        + "{\"nodeKey\":\"pending_leader\",\"nodeLabel\":\"校级领导签发\",\"roleKey\":\"school_leader\"}]",
                adminToken);

        long docId = createDocument(userToken);
        submitDocument(docId, userToken);

        // 部门负责人(head)此时无权审批（角色已改为office_admin）
        mockMvc.perform(post("/api/approvals/document/" + docId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\":\"approve\",\"opinion\":\"ok\"}")
                        .header("Authorization", "Bearer " + login("head")))
                .andExpect(status().isForbidden());
    }

    @Test
    void travelFlowReorderPutsFinanceBeforeDept() throws Exception {
        String userToken = login("user");
        String adminToken = login("admin");
        String financeToken = login("finance");

        // 差旅流程改为：财务 -> 部门
        saveFlow("travel",
                "[{\"nodeKey\":\"pending_finance\",\"nodeLabel\":\"财务处审批\",\"roleKey\":\"finance_staff\"},"
                        + "{\"nodeKey\":\"pending_dept\",\"nodeLabel\":\"部门负责人审批\",\"roleKey\":\"dept_head\"}]",
                adminToken);

        long travelId = createTravel(userToken);
        // 差旅首步固定为 pending_dept。配置改为 finance->dept 后，
        // 部门审批通过时 pending_dept 已是配置中的最后一步，直接进入 approved
        JsonNode approved = approve("travel", travelId, login("head"));
        assertEquals("approved", approved.get("status").asText());
    }

    @Test
    void invalidConfigsAreRejected() throws Exception {
        String adminToken = login("admin");

        // 1. 节点状态不以 pending_ 开头
        expectBusinessError(adminToken, "document",
                "[{\"nodeKey\":\"dept_review\",\"nodeLabel\":\"x\",\"roleKey\":\"dept_head\"}]");

        // 2. 不支持的角色
        expectBusinessError(adminToken, "document",
                "[{\"nodeKey\":\"pending_dept\",\"nodeLabel\":\"x\",\"roleKey\":\"unknown_role\"}]");

        // 3. 节点状态重复
        expectBusinessError(adminToken, "document",
                "[{\"nodeKey\":\"pending_dept\",\"nodeLabel\":\"x\",\"roleKey\":\"dept_head\"},"
                        + "{\"nodeKey\":\"pending_dept\",\"nodeLabel\":\"y\",\"roleKey\":\"office_admin\"}]");

        // 4. 空步骤
        expectBusinessError(adminToken, "document", "[]");

        // 5. 不支持的流程Key
        expectBusinessError(adminToken, "nonexistent_flow", DOCUMENT_DEFAULT);
    }

    private void expectBusinessError(String adminToken, String flowKey, String body) throws Exception {
        // 业务校验错误由 GlobalExceptionHandler 转为 400 + success:false
        mockMvc.perform(put("/api/admin/workflow/flow-key/" + flowKey)
                        .contentType(MediaType.APPLICATION_JSON).content(body)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false));
    }

    private void saveFlow(String flowKey, String body, String adminToken) throws Exception {
        mockMvc.perform(put("/api/admin/workflow/flow-key/" + flowKey)
                        .contentType(MediaType.APPLICATION_JSON).content(body)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true));
    }

    private long createDocument(String token) throws Exception {
        JsonNode data = postJson("/api/documents",
                "{\"title\":\"配置测试公文\",\"docType\":\"通知\",\"content\":\"content\",\"applicantId\":2}", token)
                .get("data");
        return data.get("id").asLong();
    }

    private void submitDocument(long id, String token) throws Exception {
        postJson("/api/documents/" + id + "/submit", "{}", token);
    }

    private long createTravel(String token) throws Exception {
        JsonNode data = postJson("/api/travels",
                "{\"applicantId\":2,\"destination\":\"上海\",\"startDate\":\"2026-09-01\","
                        + "\"endDate\":\"2026-09-03\",\"reason\":\"出差\",\"staffLevel\":\"三类\","
                        + "\"travelType\":\"其他业务\",\"transport\":\"火车硬座\",\"budget\":500}", token)
                .get("data");
        return data.get("id").asLong();
    }

    private JsonNode approve(String bizType, long bizId, String token) throws Exception {
        return postJson("/api/approvals/" + bizType + "/" + bizId,
                "{\"action\":\"approve\",\"opinion\":\"ok\"}", token).get("data");
    }

    private String login(String username) throws Exception {
        return postJson("/api/auth/login",
                "{\"username\":\"" + username + "\",\"password\":\"123456\"}", null)
                .get("data").get("token").asText();
    }

    private JsonNode postJson(String url, String json, String token) throws Exception {
        org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder builder = post(url)
                .contentType(MediaType.APPLICATION_JSON).content(json);
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        String body = mockMvc.perform(builder).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body);
    }

    private JsonNode getJson(String url, String token) throws Exception {
        String body = mockMvc.perform(get(url).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body);
    }
}
