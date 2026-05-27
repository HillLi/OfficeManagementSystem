# Workflow Guide Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a reusable workflow guide so every approval-related business page and the approval center can show the full path, current node, approval history, and follow-up business closure state.

**Architecture:** Add a backend workflow guide aggregation service behind `GET /api/workflow/guide`, using existing business read permissions before returning node details. Add a frontend `FlowGuide` component backed by a small pure utility module, then wire the component into the five business pages and the approval center.

**Tech Stack:** Java Spring Boot, MockMvc/JUnit, Vue 3 Composition API, Element Plus, Axios, Vitest.

---

## File Structure

- Create: `backend/src/main/java/com/university/oms/dto/WorkflowGuideResponse.java`
  - Holds the guide response and nested step DTO.
- Create: `backend/src/main/java/com/university/oms/service/WorkflowGuideService.java`
  - Builds expected paths and merges flow instances, tasks, approvals, audit/business closure data.
- Modify: `backend/src/main/java/com/university/oms/controller/WorkflowController.java`
  - Injects `WorkflowGuideService` and exposes `/api/workflow/guide`.
- Test: `backend/src/test/java/com/university/oms/WorkflowGuideIntegrationTest.java`
  - Covers document guide, seal route variants, travel reimbursement route, rejection/resubmission history, and forbidden access.
- Modify: `frontend/src/api.js`
  - Adds `workflowGuide`.
- Create: `frontend/src/utils/flowGuide.js`
  - Pure rendering helpers: status class, status label, default selected node.
- Test: `frontend/src/utils/flowGuide.spec.js`
  - Vitest coverage for node classes and details selection.
- Create: `frontend/src/components/FlowGuide.vue`
  - Fetches and renders workflow guide in an Element Plus dialog.
- Modify: `frontend/src/views/Documents.vue`
- Modify: `frontend/src/views/Seals.vue`
- Modify: `frontend/src/views/Meetings.vue`
- Modify: `frontend/src/views/Reports.vue`
- Modify: `frontend/src/views/Travels.vue`
- Modify: `frontend/src/views/Approvals.vue`
  - Adds “流程导览” buttons and shared dialog component.
- Modify: `frontend/src/style.css`
  - Adds responsive workflow guide styles.

## Task 1: Backend DTO, Controller Route, And Permission Gate

**Files:**
- Create: `backend/src/main/java/com/university/oms/dto/WorkflowGuideResponse.java`
- Create: `backend/src/main/java/com/university/oms/service/WorkflowGuideService.java`
- Modify: `backend/src/main/java/com/university/oms/controller/WorkflowController.java`
- Test: `backend/src/test/java/com/university/oms/WorkflowGuideIntegrationTest.java`

- [ ] **Step 1: Write the failing permission and shape test**

Add this test file:

```java
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
class WorkflowGuideIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
```

- [ ] **Step 2: Run the test and verify RED**

Run:

```powershell
cd backend
mvn test -Dtest=WorkflowGuideIntegrationTest
```

Expected: FAIL with `No endpoint GET /api/workflow/guide` or `404`.

- [ ] **Step 3: Add response DTO**

Create `backend/src/main/java/com/university/oms/dto/WorkflowGuideResponse.java`:

```java
package com.university.oms.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class WorkflowGuideResponse {
    private String bizType;
    private Long bizId;
    private String title;
    private String currentNodeKey;
    private String status;
    private List<Step> steps = new ArrayList<Step>();

    public String getBizType() { return bizType; }
    public void setBizType(String bizType) { this.bizType = bizType; }
    public Long getBizId() { return bizId; }
    public void setBizId(Long bizId) { this.bizId = bizId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCurrentNodeKey() { return currentNodeKey; }
    public void setCurrentNodeKey(String currentNodeKey) { this.currentNodeKey = currentNodeKey; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<Step> getSteps() { return steps; }
    public void setSteps(List<Step> steps) { this.steps = steps; }

    public static class Step {
        private String key;
        private String label;
        private String type;
        private String status;
        private String roleKey;
        private String roleLabel;
        private Long operatorId;
        private String operatorName;
        private String opinion;
        private LocalDateTime time;
        private LocalDateTime dueTime;

        public Step() { }

        public Step(String key, String label, String type, String status) {
            this.key = key;
            this.label = label;
            this.type = type;
            this.status = status;
        }

        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getRoleKey() { return roleKey; }
        public void setRoleKey(String roleKey) { this.roleKey = roleKey; }
        public String getRoleLabel() { return roleLabel; }
        public void setRoleLabel(String roleLabel) { this.roleLabel = roleLabel; }
        public Long getOperatorId() { return operatorId; }
        public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }
        public String getOperatorName() { return operatorName; }
        public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
        public String getOpinion() { return opinion; }
        public void setOpinion(String opinion) { this.opinion = opinion; }
        public LocalDateTime getTime() { return time; }
        public void setTime(LocalDateTime time) { this.time = time; }
        public LocalDateTime getDueTime() { return dueTime; }
        public void setDueTime(LocalDateTime dueTime) { this.dueTime = dueTime; }
    }
}
```

- [ ] **Step 4: Add minimal guide service for document drafts**

Create `backend/src/main/java/com/university/oms/service/WorkflowGuideService.java`:

```java
package com.university.oms.service;

import com.university.oms.common.BusinessException;
import com.university.oms.dto.WorkflowGuideResponse;
import com.university.oms.model.ApprovalRecord;
import com.university.oms.model.Document;
import com.university.oms.repository.InMemoryDatabase;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class WorkflowGuideService {
    private final InMemoryDatabase db;
    private final BusinessAccessService accessService;

    public WorkflowGuideService(InMemoryDatabase db, BusinessAccessService accessService) {
        this.db = db;
        this.accessService = accessService;
    }

    public WorkflowGuideResponse guide(String bizType, Long bizId) {
        accessService.requireBusinessRead(bizType, bizId);
        if ("document".equals(bizType)) {
            return documentGuide(bizId);
        }
        throw new BusinessException("暂不支持该业务流程导览");
    }

    private WorkflowGuideResponse documentGuide(Long id) {
        Document document = db.documents().get(id);
        if (document == null) {
            throw new BusinessException("公文不存在");
        }
        WorkflowGuideResponse guide = new WorkflowGuideResponse();
        guide.setBizType("document");
        guide.setBizId(id);
        guide.setTitle(document.getTitle());
        guide.setCurrentNodeKey(document.getStatus());
        guide.setStatus(document.getStatus());
        guide.setSteps(documentSteps(id, document.getStatus()));
        return guide;
    }

    private List<WorkflowGuideResponse.Step> documentSteps(Long id, String status) {
        List<WorkflowGuideResponse.Step> steps = new ArrayList<WorkflowGuideResponse.Step>();
        WorkflowGuideResponse.Step create = step("create", "公文起草", "business", "done");
        applyRecord(create, id, "create");
        steps.add(create);
        steps.add(step("ai_review", "AI格式校验", "system", "optional"));
        steps.add(step("submit", "提交审批", "business", submitted(status) ? "done" : "waiting"));
        steps.add(step("pending_dept", "部门负责人审批", "approval", "waiting"));
        steps.add(step("pending_office", "党办校办审核", "approval", "waiting"));
        steps.add(step("pending_leader", "校级领导签发", "approval", "waiting"));
        steps.add(step("distribute", "公文分发", "business", "waiting"));
        steps.add(step("receipt", "接收人签收", "business", "waiting"));
        steps.add(step("archive", "公文归档", "business", "waiting"));
        return steps;
    }

    private boolean submitted(String status) {
        return status != null && !"draft".equals(status);
    }

    private WorkflowGuideResponse.Step step(String key, String label, String type, String status) {
        return new WorkflowGuideResponse.Step(key, label, type, status);
    }

    private void applyRecord(WorkflowGuideResponse.Step step, Long id, String action) {
        for (ApprovalRecord record : db.approvals()) {
            if ("document".equals(record.getBizType()) && id.equals(record.getBizId())
                    && action.equals(record.getAction())) {
                step.setOperatorId(record.getOperatorId());
                step.setOperatorName(userName(record.getOperatorId()));
                step.setOpinion(record.getOpinion());
                step.setTime(record.getCreatedAt());
            }
        }
    }

    private String userName(Long userId) {
        if (userId == null || db.users().get(userId) == null) {
            return null;
        }
        return db.users().get(userId).getRealName();
    }
}
```

- [ ] **Step 5: Expose the controller endpoint**

Modify `backend/src/main/java/com/university/oms/controller/WorkflowController.java`:

```java
private final WorkflowService service;
private final WorkflowGuideService guideService;

public WorkflowController(WorkflowService service, WorkflowGuideService guideService) {
    this.service = service;
    this.guideService = guideService;
}

@GetMapping("/guide")
public ApiResponse<WorkflowGuideResponse> guide(@RequestParam String bizType,
                                                @RequestParam Long bizId) {
    return ApiResponse.ok(guideService.guide(bizType, bizId));
}
```

Add imports:

```java
import com.university.oms.dto.WorkflowGuideResponse;
import com.university.oms.service.WorkflowGuideService;
```

- [ ] **Step 6: Run the test and verify GREEN**

Run:

```powershell
cd backend
mvn test -Dtest=WorkflowGuideIntegrationTest
```

Expected: PASS for the two tests in `WorkflowGuideIntegrationTest`.

- [ ] **Step 7: Commit**

```powershell
git add backend/src/main/java/com/university/oms/dto/WorkflowGuideResponse.java `
  backend/src/main/java/com/university/oms/service/WorkflowGuideService.java `
  backend/src/main/java/com/university/oms/controller/WorkflowController.java `
  backend/src/test/java/com/university/oms/WorkflowGuideIntegrationTest.java
git commit -m "feat: expose workflow guide endpoint"
```

## Task 2: Document Guide Completion

**Files:**
- Modify: `backend/src/main/java/com/university/oms/service/WorkflowGuideService.java`
- Test: `backend/src/test/java/com/university/oms/WorkflowGuideIntegrationTest.java`

- [ ] **Step 1: Add failing document lifecycle guide test**

Append to `WorkflowGuideIntegrationTest`:

```java
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

private boolean stepContainsOpinion(JsonNode steps, String key, String opinion) {
    for (JsonNode step : steps) {
        if (key.equals(step.get("key").asText()) && step.has("opinion")
                && opinion.equals(step.get("opinion").asText())) {
            return true;
        }
    }
    return false;
}
```

- [ ] **Step 2: Run the test and verify RED**

Run:

```powershell
cd backend
mvn test -Dtest=WorkflowGuideIntegrationTest#documentGuideShowsDesignPathWithDistributionReceiptAndArchive
```

Expected: FAIL because AI, approval, distribution, receipt, and archive nodes are not fully marked `done`.

- [ ] **Step 3: Implement document path status merging**

In `WorkflowGuideService`, update `documentSteps` to:

```java
private List<WorkflowGuideResponse.Step> documentSteps(Long id, String businessStatus) {
    List<WorkflowGuideResponse.Step> steps = new ArrayList<WorkflowGuideResponse.Step>();
    steps.add(recordedStep(id, "create", "公文起草", "business", "create"));
    steps.add(auditStep("document", id, "ai_review", "AI格式校验", "system", "ai_review", "optional"));
    steps.add(recordedStep(id, "submit", "提交审批", "business", "submit"));
    steps.add(approvalStep("document", id, "pending_dept", "部门负责人审批"));
    steps.add(approvalStep("document", id, "pending_office", "党办校办审核"));
    steps.add(approvalStep("document", id, "pending_leader", "校级领导签发"));
    steps.add(documentDistributionStep(id));
    steps.add(documentReceiptStep(id));
    steps.add(recordedStep(id, "archive", "公文归档", "business", "archive"));
    markDocumentCurrentAndWaiting(steps, businessStatus);
    return steps;
}
```

Add these helpers:

```java
private WorkflowGuideResponse.Step recordedStep(Long id, String key, String label, String type, String action) {
    WorkflowGuideResponse.Step step = step(key, label, type, "waiting");
    applyRecord(step, id, action);
    if (step.getTime() != null) {
        step.setStatus("done");
    }
    return step;
}

private WorkflowGuideResponse.Step approvalStep(String bizType, Long id, String key, String label) {
    WorkflowGuideResponse.Step step = step(key, label, "approval", "waiting");
    for (ApprovalRecord record : db.approvals()) {
        if (bizType.equals(record.getBizType()) && id.equals(record.getBizId())
                && ("approve".equals(record.getAction()) || "reject".equals(record.getAction()))) {
            // Approval records are stored in order; assign them to the first pending approval step.
            if (matchesApprovalIndex(bizType, id, key, record)) {
                step.setOperatorId(record.getOperatorId());
                step.setOperatorName(userName(record.getOperatorId()));
                step.setOpinion(record.getOpinion());
                step.setTime(record.getCreatedAt());
                step.setStatus("reject".equals(record.getAction()) ? "rejected" : "done");
            }
        }
    }
    return step;
}

private boolean matchesApprovalIndex(String bizType, Long id, String key, ApprovalRecord target) {
    List<String> keys = new ArrayList<String>();
    if ("document".equals(bizType)) {
        keys.add("pending_dept");
        keys.add("pending_office");
        keys.add("pending_leader");
    }
    int approvalIndex = 0;
    for (ApprovalRecord record : db.approvals()) {
        if (bizType.equals(record.getBizType()) && id.equals(record.getBizId())
                && ("approve".equals(record.getAction()) || "reject".equals(record.getAction()))) {
            if (record == target) {
                return approvalIndex < keys.size() && key.equals(keys.get(approvalIndex));
            }
            approvalIndex++;
        }
    }
    return false;
}

private WorkflowGuideResponse.Step auditStep(String bizType, Long id, String key, String label,
                                             String type, String action, String emptyStatus) {
    WorkflowGuideResponse.Step step = step(key, label, type, emptyStatus);
    for (AuditLog log : db.auditLogs()) {
        if (bizType.equals(log.getBizType()) && id.equals(log.getBizId()) && action.equals(log.getAction())) {
            step.setStatus("done");
            step.setOperatorId(log.getOperatorId());
            step.setOperatorName(userName(log.getOperatorId()));
            step.setOpinion(log.getDetail());
            step.setTime(log.getCreatedAt());
        }
    }
    return step;
}
```

Add import:

```java
import com.university.oms.model.AuditLog;
```

Implement `documentDistributionStep`, `documentReceiptStep`, and `markDocumentCurrentAndWaiting` using `db.documentDistributions().values()`:

```java
private WorkflowGuideResponse.Step documentDistributionStep(Long id) {
    WorkflowGuideResponse.Step step = step("distribute", "公文分发", "business", "waiting");
    for (DocumentDistribution distribution : db.documentDistributions().values()) {
        if (id.equals(distribution.getDocumentId())) {
            step.setStatus("done");
            step.setTime(distribution.getDistributedAt());
            step.setOpinion("已分发至用户#" + distribution.getReceiverId());
            return step;
        }
    }
    return step;
}

private WorkflowGuideResponse.Step documentReceiptStep(Long id) {
    WorkflowGuideResponse.Step step = step("receipt", "接收人签收", "business", "waiting");
    boolean found = false;
    boolean allReceived = true;
    for (DocumentDistribution distribution : db.documentDistributions().values()) {
        if (id.equals(distribution.getDocumentId())) {
            found = true;
            if (!"received".equals(distribution.getStatus())) {
                allReceived = false;
            } else {
                step.setTime(distribution.getReceivedAt());
            }
        }
    }
    if (found) {
        step.setStatus(allReceived ? "done" : "current");
        step.setOpinion(allReceived ? "接收人已签收" : "存在待签收记录");
    }
    return step;
}
```

Add import:

```java
import com.university.oms.model.DocumentDistribution;
```

- [ ] **Step 4: Run the test and verify GREEN**

Run:

```powershell
cd backend
mvn test -Dtest=WorkflowGuideIntegrationTest
```

Expected: PASS for all current workflow guide tests.

- [ ] **Step 5: Commit**

```powershell
git add backend/src/main/java/com/university/oms/service/WorkflowGuideService.java `
  backend/src/test/java/com/university/oms/WorkflowGuideIntegrationTest.java
git commit -m "feat: complete document workflow guide"
```

## Task 3: Seal, Meeting, Report, And Travel Guide Paths

**Files:**
- Modify: `backend/src/main/java/com/university/oms/service/WorkflowGuideService.java`
- Test: `backend/src/test/java/com/university/oms/WorkflowGuideIntegrationTest.java`

- [ ] **Step 1: Add failing path tests for the remaining business types**

Append tests for seal variants and travel reimbursement:

```java
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
}
```

- [ ] **Step 2: Run the tests and verify RED**

Run:

```powershell
cd backend
mvn test -Dtest=WorkflowGuideIntegrationTest#sealGuideUsesMajorAndSchoolSealRoutes,WorkflowGuideIntegrationTest#travelGuideSeparatesApprovalFromReimbursementReview
```

Expected: FAIL because guide only supports documents.

- [ ] **Step 3: Implement remaining path builders**

In `WorkflowGuideService.guide`, add branches:

```java
if ("seal".equals(bizType)) {
    return sealGuide(bizId);
}
if ("meeting".equals(bizType)) {
    return meetingGuide(bizId);
}
if ("report".equals(bizType)) {
    return reportGuide(bizId);
}
if ("travel".equals(bizType)) {
    return travelGuide(bizId);
}
```

Implement each guide with focused helpers:

```java
private WorkflowGuideResponse sealGuide(Long id) {
    SealApplication app = db.sealApplications().get(id);
    if (app == null) throw new BusinessException("用印申请不存在");
    WorkflowGuideResponse guide = base("seal", id, "用印申请 #" + id, app.getStatus());
    List<WorkflowGuideResponse.Step> steps = new ArrayList<WorkflowGuideResponse.Step>();
    steps.add(step("draft", "保存草稿", "business", "done"));
    steps.add(step("materials", "上传材料", "business", activeMaterialCount("seal", id) > 0 ? "done" : "waiting"));
    steps.add(recordedBusinessStep("seal", id, "submit", "提交申请", "submit"));
    steps.addAll(sealApprovalSteps(app));
    steps.add(step("approved", "审批通过", "approval", reached(app.getStatus(), "approved") ? "done" : "waiting"));
    steps.add(recordedBusinessStep("seal", id, "use", "用印登记", "use"));
    steps.add(recordedBusinessStep("seal", id, "return", "归还确认", "return"));
    markCurrentByBusinessStatus(steps, app.getStatus());
    guide.setSteps(steps);
    return guide;
}

private List<WorkflowGuideResponse.Step> sealApprovalSteps(SealApplication app) {
    List<WorkflowGuideResponse.Step> steps = new ArrayList<WorkflowGuideResponse.Step>();
    Seal seal = db.seals().get(app.getSealId());
    boolean schoolSeal = seal != null && seal.getSealName().contains("北京大学");
    boolean major = "重大事项".equals(app.getMatterLevel());
    if (!schoolSeal) {
        steps.add(approvalStep("seal", app.getId(), "pending_dept", "部门负责人审批"));
    }
    if (schoolSeal || major) {
        steps.add(approvalStep("seal", app.getId(), "pending_office", "党办校办审核"));
    }
    if (schoolSeal && major) {
        steps.add(approvalStep("seal", app.getId(), "pending_leader", "校级领导审批"));
    }
    return steps;
}
```

Add imports:

```java
import com.university.oms.model.Seal;
import com.university.oms.model.SealApplication;
import com.university.oms.model.Meeting;
import com.university.oms.model.Report;
import com.university.oms.model.Travel;
```

Add shared helpers for all business guides:

```java
private WorkflowGuideResponse base(String bizType, Long id, String title, String status) {
    WorkflowGuideResponse guide = new WorkflowGuideResponse();
    guide.setBizType(bizType);
    guide.setBizId(id);
    guide.setTitle(title);
    guide.setCurrentNodeKey(status);
    guide.setStatus(status);
    return guide;
}

private WorkflowGuideResponse.Step recordedBusinessStep(String bizType, Long id,
                                                        String key, String label, String action) {
    WorkflowGuideResponse.Step step = step(key, label, "business", "waiting");
    for (ApprovalRecord record : db.approvals()) {
        if (bizType.equals(record.getBizType()) && id.equals(record.getBizId())
                && action.equals(record.getAction())) {
            step.setStatus("done");
            step.setOperatorId(record.getOperatorId());
            step.setOperatorName(userName(record.getOperatorId()));
            step.setOpinion(record.getOpinion());
            step.setTime(record.getCreatedAt());
        }
    }
    return step;
}

private int activeMaterialCount(String bizType, Long bizId) {
    int count = 0;
    for (Attachment attachment : db.attachments()) {
        if (bizType.equals(attachment.getBizType()) && bizId.equals(attachment.getBizId())
                && !attachment.isDeleted()) {
            count++;
        }
    }
    return count;
}

private boolean reached(String currentStatus, String targetStatus) {
    if (targetStatus.equals(currentStatus)) {
        return true;
    }
    if ("approved".equals(targetStatus)) {
        return "used".equals(currentStatus) || "returned".equals(currentStatus)
                || "archived".equals(currentStatus);
    }
    return false;
}

private void markCurrentByBusinessStatus(List<WorkflowGuideResponse.Step> steps, String currentStatus) {
    for (WorkflowGuideResponse.Step step : steps) {
        if (currentStatus != null && currentStatus.equals(step.getKey()) && "waiting".equals(step.getStatus())) {
            step.setStatus("current");
        }
    }
}
```

Add import:

```java
import com.university.oms.model.Attachment;
```

Implement `meetingGuide`:

```java
private WorkflowGuideResponse meetingGuide(Long id) {
    Meeting meeting = db.meetings().get(id);
    if (meeting == null) throw new BusinessException("会议不存在");
    WorkflowGuideResponse guide = base("meeting", id, meeting.getTitle(), meeting.getStatus());
    List<WorkflowGuideResponse.Step> steps = new ArrayList<WorkflowGuideResponse.Step>();
    steps.add(recordedBusinessStep("meeting", id, "submit", "提交会议", "submit"));
    if (meeting.isLargeActivity()) {
        steps.add(approvalStep("meeting", id, "pending_security", "保卫部门审核"));
    }
    steps.add(approvalStep("meeting", id, "pending_dept", "部门负责人审批"));
    steps.add(step("approved", "审批通过", "approval", reached(meeting.getStatus(), "approved") ? "done" : "waiting"));
    WorkflowGuideResponse.Step archive = recordedBusinessStep("meeting", id,
            "archive_minutes", "纪要归档", "archive_minutes");
    if ("archived".equals(meeting.getStatus())) {
        archive.setStatus("done");
    }
    steps.add(archive);
    markCurrentByBusinessStatus(steps, meeting.getStatus());
    guide.setSteps(steps);
    return guide;
}
```

Implement `reportGuide`:

```java
private WorkflowGuideResponse reportGuide(Long id) {
    Report report = db.reports().get(id);
    if (report == null) throw new BusinessException("请示报告不存在");
    WorkflowGuideResponse guide = base("report", id, report.getTitle(), report.getStatus());
    List<WorkflowGuideResponse.Step> steps = new ArrayList<WorkflowGuideResponse.Step>();
    steps.add(recordedBusinessStep("report", id, "submit", "提交请示报告", "submit"));
    steps.add(approvalStep("report", id, "pending_secret_review", "保密审查"));
    steps.add(approvalStep("report", id, "pending_dept", "部门负责人审批"));
    steps.add(step("approved", "审批通过", "approval", reached(report.getStatus(), "approved") ? "done" : "waiting"));
    WorkflowGuideResponse.Step reply = recordedBusinessStep("report", id, "reply", "批复归档", "reply");
    if ("archived".equals(report.getStatus())) {
        reply.setStatus("done");
    }
    steps.add(reply);
    markCurrentByBusinessStatus(steps, report.getStatus());
    guide.setSteps(steps);
    return guide;
}
```

Implement `travelGuide`:

```java
private WorkflowGuideResponse travelGuide(Long id) {
    Travel travel = db.travels().get(id);
    if (travel == null) throw new BusinessException("差旅申请不存在");
    WorkflowGuideResponse guide = base("travel", id, "差旅申请 #" + id, travel.getStatus());
    List<WorkflowGuideResponse.Step> steps = new ArrayList<WorkflowGuideResponse.Step>();
    steps.add(recordedBusinessStep("travel", id, "submit", "提交差旅", "submit"));
    steps.add(approvalStep("travel", id, "pending_dept", "部门负责人审批"));
    steps.add(approvalStep("travel", id, "pending_finance", "财务审核"));
    steps.add(step("approved", "审批通过", "approval", reached(travel.getStatus(), "approved") ? "done" : "waiting"));
    WorkflowGuideResponse.Step reimburse = recordedBusinessStep("travel", id,
            "submit_reimbursement", "提交报销", "reimburse");
    steps.add(reimburse);
    WorkflowGuideResponse.Step recheck = step("finance_recheck", "财务复核", "approval",
            "pending_finance".equals(travel.getStatus()) && travel.isReimbursementSubmitted() ? "current" : "waiting");
    steps.add(recheck);
    steps.add(step("archive", "归档", "business", "archived".equals(travel.getStatus()) ? "done" : "waiting"));
    guide.setSteps(steps);
    return guide;
}
```

- [ ] **Step 4: Run tests and verify GREEN**

Run:

```powershell
cd backend
mvn test -Dtest=WorkflowGuideIntegrationTest
```

Expected: PASS for all workflow guide tests.

- [ ] **Step 5: Commit**

```powershell
git add backend/src/main/java/com/university/oms/service/WorkflowGuideService.java `
  backend/src/test/java/com/university/oms/WorkflowGuideIntegrationTest.java
git commit -m "feat: add business workflow guide paths"
```

## Task 4: Rejection, Current Tasks, And Node Details

**Files:**
- Modify: `backend/src/main/java/com/university/oms/service/WorkflowGuideService.java`
- Test: `backend/src/test/java/com/university/oms/WorkflowGuideIntegrationTest.java`

- [ ] **Step 1: Add failing rejection and current task tests**

Append:

```java
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
```

- [ ] **Step 2: Run test and verify RED**

Run:

```powershell
cd backend
mvn test -Dtest=WorkflowGuideIntegrationTest#rejectedDocumentGuideKeepsOpinionAndMarksRejectedNode
```

Expected: FAIL if the rejected node is not marked correctly or the opinion is missing.

- [ ] **Step 3: Add task metadata and robust action-to-node assignment**

Update guide service so every approval node looks at flow task history first, then approval records. Add helper:

```java
private void applyTaskDetails(WorkflowGuideResponse.Step step, String bizType, Long bizId) {
    for (FlowTask task : db.flowTasks()) {
        if (bizType.equals(task.getBizType()) && bizId.equals(task.getBizId())
                && step.getKey().equals(task.getNodeKey())) {
            step.setRoleKey(task.getApproverRole());
            step.setRoleLabel(roleLabel(task.getApproverRole()));
            step.setDueTime(task.getDueTime());
            if ("pending".equals(task.getStatus())) {
                step.setStatus("current");
            }
            if (task.getApproverId() != null) {
                step.setOperatorId(task.getApproverId());
                step.setOperatorName(userName(task.getApproverId()));
            }
        }
    }
}

private String roleLabel(String roleKey) {
    if ("dept_head".equals(roleKey)) return "部门负责人";
    if ("office_admin".equals(roleKey)) return "党办校办人员";
    if ("school_leader".equals(roleKey)) return "校级领导";
    if ("finance_staff".equals(roleKey)) return "财务人员";
    if ("security_staff".equals(roleKey)) return "保卫人员";
    if ("seal_keeper".equals(roleKey)) return "印章保管人";
    return roleKey;
}
```

Add import:

```java
import com.university.oms.model.FlowTask;
```

- [ ] **Step 4: Run tests and verify GREEN**

Run:

```powershell
cd backend
mvn test -Dtest=WorkflowGuideIntegrationTest
```

Expected: PASS.

- [ ] **Step 5: Commit**

```powershell
git add backend/src/main/java/com/university/oms/service/WorkflowGuideService.java `
  backend/src/test/java/com/university/oms/WorkflowGuideIntegrationTest.java
git commit -m "feat: enrich workflow guide node details"
```

## Task 5: Frontend Guide Utilities And Component

**Files:**
- Modify: `frontend/src/api.js`
- Create: `frontend/src/utils/flowGuide.js`
- Test: `frontend/src/utils/flowGuide.spec.js`
- Create: `frontend/src/components/FlowGuide.vue`
- Modify: `frontend/src/style.css`

- [ ] **Step 1: Write failing utility tests**

Create `frontend/src/utils/flowGuide.spec.js`:

```javascript
import { describe, expect, it } from 'vitest'
import { guideStatusClass, initialGuideStep, stepDetailLines } from './flowGuide'

describe('flow guide helpers', () => {
  it('maps statuses to stable css classes', () => {
    expect(guideStatusClass({ status: 'done' })).toBe('flow-step done')
    expect(guideStatusClass({ status: 'current' })).toBe('flow-step current')
    expect(guideStatusClass({ status: 'rejected' })).toBe('flow-step rejected')
    expect(guideStatusClass({ status: 'waiting' })).toBe('flow-step waiting')
  })

  it('selects the current step first and falls back to the first step', () => {
    const steps = [{ key: 'a', status: 'done' }, { key: 'b', status: 'current' }]
    expect(initialGuideStep(steps).key).toBe('b')
    expect(initialGuideStep([{ key: 'a', status: 'done' }]).key).toBe('a')
  })

  it('formats detail lines with role, operator, opinion and time', () => {
    const lines = stepDetailLines({
      roleLabel: '部门负责人',
      operatorName: '张三',
      opinion: '同意',
      time: '2026-05-28T10:00:00',
      dueTime: '2026-05-31T10:00:00'
    })
    expect(lines).toContain('处理角色：部门负责人')
    expect(lines).toContain('实际处理人：张三')
    expect(lines).toContain('意见/说明：同意')
    expect(lines).toContain('办理时间：2026-05-28T10:00:00')
    expect(lines).toContain('截止时间：2026-05-31T10:00:00')
  })
})
```

- [ ] **Step 2: Run utility test and verify RED**

Run:

```powershell
cd frontend
npm test -- src/utils/flowGuide.spec.js
```

Expected: FAIL because `flowGuide.js` does not exist.

- [ ] **Step 3: Implement utility module**

Create `frontend/src/utils/flowGuide.js`:

```javascript
export const guideStatusClass = (step) => `flow-step ${step?.status || 'waiting'}`

export const initialGuideStep = (steps = []) => {
  return steps.find((step) => step.status === 'current' || step.status === 'rejected') || steps[0] || null
}

export const stepDetailLines = (step) => {
  if (!step) return []
  return [
    step.roleLabel && `处理角色：${step.roleLabel}`,
    step.operatorName && `实际处理人：${step.operatorName}`,
    step.opinion && `意见/说明：${step.opinion}`,
    step.time && `办理时间：${step.time}`,
    step.dueTime && `截止时间：${step.dueTime}`
  ].filter(Boolean)
}
```

- [ ] **Step 4: Add API method**

Modify `frontend/src/api.js`:

```javascript
workflowGuide: (params) => http.get('/workflow/guide', { params }),
```

Place it next to `flowInstances` and `flowTasks`.

- [ ] **Step 5: Create FlowGuide component**

Create `frontend/src/components/FlowGuide.vue`:

```vue
<template>
  <el-dialog v-model="visible" title="流程导览" width="min(920px, calc(100vw - 24px))" @closed="reset">
    <el-empty v-if="!guide && !loading" description="暂无流程数据" />
    <div v-loading="loading" v-if="guide" class="flow-guide">
      <div class="flow-summary">
        <strong>{{ guide.title || `${guide.bizType} #${guide.bizId}` }}</strong>
        <span>当前状态：{{ labelOf('business_status', guide.status) }}</span>
      </div>
      <div class="flow-steps">
        <button
          v-for="step in guide.steps"
          :key="step.key"
          type="button"
          :class="guideStatusClass(step)"
          @click="selected = step"
        >
          <span class="flow-dot"></span>
          <span class="flow-label">{{ step.label }}</span>
          <small>{{ statusText(step.status) }}</small>
        </button>
      </div>
      <el-card v-if="selected" class="flow-detail" shadow="never">
        <template #header>{{ selected.label }}</template>
        <p v-for="line in detailLines" :key="line">{{ line }}</p>
        <p v-if="detailLines.length === 0">该节点暂无办理详情。</p>
      </el-card>
    </div>
  </el-dialog>
</template>

<script setup>
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '../api'
import { guideStatusClass, initialGuideStep, stepDetailLines } from '../utils/flowGuide'
import { useDictionaryStore } from '../stores/dictionary'

const dictionaryStore = useDictionaryStore()
const labelOf = dictionaryStore.labelOf
const visible = ref(false)
const loading = ref(false)
const guide = ref(null)
const selected = ref(null)
const detailLines = computed(() => stepDetailLines(selected.value))

const statusText = (status) => ({
  done: '已完成',
  current: '当前',
  waiting: '待办理',
  rejected: '已退回',
  skipped: '跳过',
  optional: '可选'
}[status] || status)

const open = async (bizType, bizId) => {
  visible.value = true
  loading.value = true
  try {
    guide.value = await api.workflowGuide({ bizType, bizId })
    selected.value = initialGuideStep(guide.value.steps)
  } catch (error) {
    ElMessage.error(error.message || '流程导览加载失败')
  } finally {
    loading.value = false
  }
}

const reset = () => {
  guide.value = null
  selected.value = null
}

defineExpose({ open })
</script>
```

- [ ] **Step 6: Add CSS**

Append to `frontend/src/style.css`:

```css
.flow-guide {
  display: grid;
  gap: 14px;
}

.flow-summary {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
  color: #44546a;
}

.flow-steps {
  display: flex;
  gap: 10px;
  overflow-x: auto;
  padding-bottom: 6px;
}

.flow-step {
  min-width: 132px;
  border: 1px solid #d8e0ea;
  border-radius: 10px;
  background: #fff;
  padding: 10px;
  color: #44546a;
  text-align: left;
  cursor: pointer;
}

.flow-step.done {
  border-color: #67c23a;
  background: #f0f9eb;
}

.flow-step.current {
  border-color: #1f5f8b;
  background: #eef6ff;
  color: #1f5f8b;
}

.flow-step.rejected {
  border-color: #f56c6c;
  background: #fef0f0;
  color: #c45656;
}

.flow-step.waiting,
.flow-step.optional,
.flow-step.skipped {
  background: #f8fafc;
}

.flow-dot {
  display: block;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: currentColor;
  margin-bottom: 6px;
}

.flow-label {
  display: block;
  font-weight: 700;
}

.flow-detail p {
  margin: 6px 0;
}

@media (max-width: 700px) {
  .flow-steps {
    flex-direction: column;
    overflow-x: visible;
  }

  .flow-step {
    width: 100%;
  }
}
```

- [ ] **Step 7: Run frontend tests**

Run:

```powershell
cd frontend
npm test -- src/utils/flowGuide.spec.js
```

Expected: PASS.

- [ ] **Step 8: Commit**

```powershell
git add frontend/src/api.js frontend/src/utils/flowGuide.js frontend/src/utils/flowGuide.spec.js `
  frontend/src/components/FlowGuide.vue frontend/src/style.css
git commit -m "feat: add workflow guide component"
```

## Task 6: Wire Guide Into Business Pages And Approval Center

**Files:**
- Modify: `frontend/src/views/Documents.vue`
- Modify: `frontend/src/views/Seals.vue`
- Modify: `frontend/src/views/Meetings.vue`
- Modify: `frontend/src/views/Reports.vue`
- Modify: `frontend/src/views/Travels.vue`
- Modify: `frontend/src/views/Approvals.vue`

- [ ] **Step 1: Add FlowGuide to Documents page**

In `Documents.vue`, import:

```javascript
import FlowGuide from '../components/FlowGuide.vue'
```

Add ref:

```javascript
const flowGuide = ref(null)
const openFlowGuide = (row) => flowGuide.value?.open('document', row.id)
```

Add button in the table actions:

```vue
<el-button size="small" @click="openFlowGuide(row)">流程导览</el-button>
```

Add component near dialogs:

```vue
<FlowGuide ref="flowGuide" />
```

- [ ] **Step 2: Repeat exact page-specific openers**

Use these mappings:

```javascript
// Seals.vue
const openFlowGuide = (row) => flowGuide.value?.open('seal', row.id)

// Meetings.vue
const openFlowGuide = (row) => flowGuide.value?.open('meeting', row.id)

// Reports.vue
const openFlowGuide = (row) => flowGuide.value?.open('report', row.id)

// Travels.vue
const openFlowGuide = (row) => flowGuide.value?.open('travel', row.id)

// Approvals.vue for tasks/instances/records
const openFlowGuide = (row) => flowGuide.value?.open(row.bizType, row.bizId)
```

For each file:

1. Import `FlowGuide`.
2. Add `const flowGuide = ref(null)`.
3. Add `openFlowGuide`.
4. Add an `<el-button size="small" @click="openFlowGuide(row)">流程导览</el-button>` in the relevant action column.
5. Add `<FlowGuide ref="flowGuide" />` once at the end of the template.

- [ ] **Step 3: Run frontend build**

Run:

```powershell
cd frontend
npm run build
```

Expected: build succeeds. Existing Vite chunk-size warnings are acceptable if there are no errors.

- [ ] **Step 4: Commit**

```powershell
git add frontend/src/views/Documents.vue frontend/src/views/Seals.vue frontend/src/views/Meetings.vue `
  frontend/src/views/Reports.vue frontend/src/views/Travels.vue frontend/src/views/Approvals.vue
git commit -m "feat: show workflow guide across approval pages"
```

## Task 7: Final Verification And Documentation Touch-Up

**Files:**
- Modify: `doc/接口补充-流程闭环.md`
- Modify: `doc/接口文档.md`
- Modify: `doc/2026-05-28.md`

- [ ] **Step 1: Update interface docs**

Add to `doc/接口补充-流程闭环.md` section 1:

```markdown
| GET | `/api/workflow/guide?bizType=seal&bizId=1001` | 查询当前用户可见业务的流程导览图，返回完整路径、当前节点、办理角色、处理人、意见和时间 |
```

Add to `doc/接口文档.md` under 通用审批:

```markdown
| GET | `/api/workflow/guide?bizType=document&bizId=1001` | 查询流程导览图 |
```

- [ ] **Step 2: Add dated change report**

Create `doc/2026-05-28.md`:

```markdown
# 2026-05-28 流程导览图交付记录

本次新增通用流程导览能力，覆盖公文、用印、会议、请示报告、差旅以及审批中心。

## 变更摘要

- 新增 `/api/workflow/guide`，按业务权限返回完整流程路径、当前节点、处理角色、实际处理人、意见和时间。
- 新增前端 `FlowGuide` 组件，并接入所有需要审批的业务页面和审批中心。
- 公文导览按设计文档展示“起草、AI格式校验、审批、签发、分发、签收、归档”主线，但不新增强制归档限制。
- 用印导览按部门/校级、常规/重大动态展示审批路径，并覆盖材料、用印登记和归还确认。

## 验证

- `cd backend && mvn test`
- `cd frontend && npm test`
- `cd frontend && npm run build`
```

- [ ] **Step 3: Run full backend tests**

Run:

```powershell
cd backend
mvn test
```

Expected: BUILD SUCCESS.

- [ ] **Step 4: Run frontend tests**

Run:

```powershell
cd frontend
npm test
```

Expected: all Vitest tests pass.

- [ ] **Step 5: Run frontend build**

Run:

```powershell
cd frontend
npm run build
```

Expected: build succeeds.

- [ ] **Step 6: Commit docs and final verification**

```powershell
git add doc/接口补充-流程闭环.md doc/接口文档.md doc/2026-05-28.md
git commit -m "docs: document workflow guide endpoint"
```

## Self-Review Checklist

- Spec coverage: Tasks cover the backend interface, all five business paths, approval center entry, frontend component, permissions, tests, and docs.
- Placeholder scan: No `TBD`, `TODO`, or “implement later” language is present.
- Type consistency: Backend response uses `WorkflowGuideResponse` with `Step`; frontend expects `steps`, `status`, `roleLabel`, `operatorName`, `opinion`, `time`, and `dueTime`, matching the DTO.
- Boundary check: The plan does not change approval rules or force a new公文签收-before-归档 backend constraint.
