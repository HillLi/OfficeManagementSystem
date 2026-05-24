# Compliance Remediation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 修复审查报告中的权限、制度、持久化和前端办理缺陷，使系统具备可验收的高校办公业务闭环。

**Architecture:** 保留当前 Vue 3、Spring Boot 2.5.6、Spring JDBC 和 PBKDF2 基线，在 Service 层集中执行数据范围与业务操作授权，通过现有 `InMemoryDatabase` / `DataPersistence` 扩展闭环实体和 MySQL 表。前端调用新增后端能力完成办理操作，所有权限判定仍以后端为准。

**Tech Stack:** Java 8, Spring Boot 2.5.6, Spring MVC Test/JUnit 5, Spring JDBC, MySQL 8, Vue 3, Element Plus, Vite

---

## File Structure

| Responsibility | Files |
| --- | --- |
| Security and scoped visibility | `backend/src/main/java/com/university/oms/security/AuthTokenService.java`, `backend/src/main/java/com/university/oms/controller/AuthController.java`, `backend/src/main/java/com/university/oms/service/AuthService.java`, `backend/src/main/java/com/university/oms/service/BusinessAccessService.java`, `backend/src/main/java/com/university/oms/service/WorkflowService.java`, `backend/src/main/java/com/university/oms/service/ApprovalService.java` |
| Document closure and AI safeguards | `backend/src/main/java/com/university/oms/model/Document.java`, `backend/src/main/java/com/university/oms/model/DocumentDistribution.java`, `backend/src/main/java/com/university/oms/dto/DocumentDistributionRequest.java`, `backend/src/main/java/com/university/oms/controller/DocumentController.java`, `backend/src/main/java/com/university/oms/service/DocumentService.java` |
| Rule-compliant meeting, seal and travel flows | `backend/src/main/java/com/university/oms/model/SealTransfer.java`, `backend/src/main/java/com/university/oms/dto/SealTransferRequest.java`, `backend/src/main/java/com/university/oms/dto/MeetingRequest.java`, `backend/src/main/java/com/university/oms/dto/TravelReimburseRequest.java`, services/controllers for seals, meetings, travels and reports |
| Persistence and seeds | `backend/src/main/java/com/university/oms/repository/InMemoryDatabase.java`, `DataPersistence.java`, `JdbcDataPersistence.java`, `MysqlDataLoader.java`, `backend/sql/schema.sql`, `backend/sql/data.sql`, `backend/scripts/init-mysql.ps1` |
| Frontend closure | `frontend/src/api.js`, `frontend/src/App.vue`, `frontend/src/router/index.js`, `frontend/src/views/Approvals.vue`, `Documents.vue`, `Seals.vue`, `Meetings.vue`, `Travels.vue`, `Dashboard.vue`, new `frontend/src/views/Statistics.vue` |
| Tests and documents | new integration tests under `backend/src/test/java/com/university/oms/`, `doc/设计.md`, `doc/数据库初始化.md`, `doc/接口补充-流程闭环.md`, `doc/2026-05-25.md` |

### Task 1: Secure Sessions And Business Visibility

**Files:**
- Create: `backend/src/main/java/com/university/oms/service/BusinessAccessService.java`
- Create: `backend/src/test/java/com/university/oms/AuthorizationRegressionTest.java`
- Modify: `backend/src/main/java/com/university/oms/security/AuthTokenService.java`
- Modify: `backend/src/main/java/com/university/oms/service/AuthService.java`
- Modify: `backend/src/main/java/com/university/oms/controller/AuthController.java`
- Modify: `backend/src/main/java/com/university/oms/service/DocumentService.java`
- Modify: `backend/src/main/java/com/university/oms/service/WorkflowService.java`
- Modify: `backend/src/main/java/com/university/oms/service/ApprovalService.java`

- [ ] **Step 1: Write failing authorization and logout tests**

Add `AuthorizationRegressionTest` with `MockMvc` tests that create a secret document as `user`, then assert:

```java
mockMvc.perform(post("/api/documents/" + documentId + "/submit")
        .header("Authorization", bearer(financeToken)))
        .andExpect(status().isForbidden());
mockMvc.perform(get("/api/workflow/attachments?bizType=document&bizId=" + documentId)
        .header("Authorization", bearer(financeToken)))
        .andExpect(status().isForbidden());
mockMvc.perform(get("/api/approvals?bizType=document&bizId=" + documentId)
        .header("Authorization", bearer(financeToken)))
        .andExpect(status().isForbidden());
mockMvc.perform(post("/api/auth/logout")
        .header("Authorization", bearer(userToken)))
        .andExpect(status().isOk());
mockMvc.perform(get("/api/documents")
        .header("Authorization", bearer(userToken)))
        .andExpect(status().isUnauthorized());
```

- [ ] **Step 2: Run the new test and verify RED**

Run:

```powershell
& 'D:\dev\maven\apache-maven-3.9.14\bin\mvn.cmd' -Dtest=AuthorizationRegressionTest test
```

Expected: FAIL because finance currently receives successful responses and `/api/auth/logout` does not exist.

- [ ] **Step 3: Implement minimal scoped access and token revocation**

Implement `BusinessAccessService` with explicit methods:

```java
public void requireDocumentSubmit(Document document);
public void requireDocumentArchive(Document document);
public void requireBusinessRead(String bizType, Long bizId);
public boolean canReadBusiness(String bizType, Long bizId);
```

The methods read `AuthContext.currentUser()`, allow the applicant, current/participating approver and narrowly defined manager roles, and throw `BusinessException("无权访问该业务数据")` for unrelated users. Invoke them from `DocumentService.submit/archive`, `WorkflowService.addAttachment/attachments`, and `ApprovalService.list`.

Change `AuthTokenService` to store an expiry-bearing token record and add:

```java
public void revoke(String token);
public void revokeCurrent(String token);
```

Add `POST /api/auth/logout` to revoke the presented bearer token.

- [ ] **Step 4: Map authorization failures to HTTP 403 and verify GREEN**

Introduce an access-denied exception or distinguish forbidden business exceptions in `GlobalExceptionHandler`, so rejected authenticated actions return `403`, then rerun:

```powershell
& 'D:\dev\maven\apache-maven-3.9.14\bin\mvn.cmd' -Dtest=AuthorizationRegressionTest,SecurityIntegrationTest,WorkflowIntegrationTest test
```

Expected: PASS with unrelated roles rejected, legitimate workflow tests retained, and logout invalidating the token.

- [ ] **Step 5: Commit the secure access increment**

```powershell
git add backend/src/main/java/com/university/oms backend/src/test/java/com/university/oms/AuthorizationRegressionTest.java
git commit -m "fix: enforce business access and session revocation"
```

### Task 2: Protect Closures And AI Usage

**Files:**
- Create: `backend/src/test/java/com/university/oms/ClosureAndAiSecurityTest.java`
- Modify: `backend/src/main/java/com/university/oms/service/MeetingService.java`
- Modify: `backend/src/main/java/com/university/oms/service/ReportService.java`
- Modify: `backend/src/main/java/com/university/oms/service/TravelService.java`
- Modify: `backend/src/main/java/com/university/oms/service/DocumentService.java`
- Modify: `backend/src/main/java/com/university/oms/design/SecrecyCheckDecorator.java`
- Modify: `backend/src/main/java/com/university/oms/service/WorkflowService.java`

- [ ] **Step 1: Write failing closure and AI tests**

Create tests asserting that finance cannot archive an approved meeting or reply to an approved internal report; finance cannot reimburse another user's approved travel; internal/secret document AI review is rejected; public AI review writes an audit action:

```java
mockMvc.perform(post("/api/meetings/" + meetingId + "/minutes")
        .header("Authorization", bearer(financeToken))
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"minutes\":\"illegal\",\"signInCount\":1}"))
        .andExpect(status().isForbidden());
mockMvc.perform(post("/api/travels/" + travelId + "/reimburse")
        .header("Authorization", bearer(financeToken))
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"actualExpense\":880,\"receiptUrl\":\"/r.pdf\"}"))
        .andExpect(status().isForbidden());
```

- [ ] **Step 2: Run and verify RED**

Run:

```powershell
& 'D:\dev\maven\apache-maven-3.9.14\bin\mvn.cmd' -Dtest=ClosureAndAiSecurityTest test
```

Expected: FAIL because these operations currently succeed or do not audit public AI use.

- [ ] **Step 3: Enforce operators and AI auditing**

Use `BusinessAccessService` in the closure service methods:

```java
access.requireMeetingMinutesArchive(meeting);
access.requireReportReply(report);
access.requireTravelReimburse(travel);
```

Make `内部`, `秘密`, `机密`, `绝密` all blocked from AI review/draft input, and write `ai_review` audit entries for allowed public review actions through `WorkflowService.audit(...)`.

- [ ] **Step 4: Run GREEN regression**

Run:

```powershell
& 'D:\dev\maven\apache-maven-3.9.14\bin\mvn.cmd' -Dtest=ClosureAndAiSecurityTest,OfficeManagementSystemIntegrationTest test
```

Expected: PASS, including existing secret AI protection and the new operator prohibitions.

- [ ] **Step 5: Commit the closure security increment**

```powershell
git add backend/src/main/java/com/university/oms backend/src/test/java/com/university/oms/ClosureAndAiSecurityTest.java
git commit -m "fix: secure business closures and ai review"
```

### Task 3: Implement Document Distribution, Receipt And Reminders

**Files:**
- Create: `backend/src/main/java/com/university/oms/model/DocumentDistribution.java`
- Create: `backend/src/main/java/com/university/oms/dto/DocumentDistributionRequest.java`
- Create: `backend/src/test/java/com/university/oms/DocumentClosureTest.java`
- Modify: `backend/src/main/java/com/university/oms/model/Document.java`
- Modify: `backend/src/main/java/com/university/oms/controller/DocumentController.java`
- Modify: `backend/src/main/java/com/university/oms/service/DocumentService.java`
- Modify: `backend/src/main/java/com/university/oms/repository/InMemoryDatabase.java`
- Modify: `backend/src/main/java/com/university/oms/repository/DataPersistence.java`
- Modify: `backend/src/main/java/com/university/oms/repository/NoopDataPersistence.java`

- [ ] **Step 1: Write failing document closure tests**

Add a test completing approval and asserting:

```java
JsonNode distributed = postJson("/api/documents/" + id + "/distributions",
        "{\"receiverId\":2,\"receiverDeptId\":4}", officeToken).get("data");
assertEquals("distributed", distributed.get("status").asText());
postJson("/api/documents/" + id + "/distributions/" + distributionId + "/receipt", "{}", userToken);
postJson("/api/documents/" + id + "/distributions/" + distributionId + "/remind", "{}", officeToken);
assertTrue(getJson("/api/workflow/notifications?unreadOnly=false", userToken).get("data").size() > 0);
```

Also assert a rejected/resubmitted document increments its `version` and preserves rejection history.

- [ ] **Step 2: Run and verify RED**

```powershell
& 'D:\dev\maven\apache-maven-3.9.14\bin\mvn.cmd' -Dtest=DocumentClosureTest test
```

Expected: FAIL because distribution endpoints and version fields do not exist.

- [ ] **Step 3: Implement minimum document lifecycle additions**

Add `version`, `distributionStatus` and getters/setters to `Document`; add `DocumentDistribution` with `documentId`, `receiverId`, `receiverDeptId`, `status`, `distributedAt`, `receivedAt`, `remindedAt`; keep a distribution map in `InMemoryDatabase`.

Expose:

```java
@PostMapping("/{id}/distributions")
@GetMapping("/{id}/distributions")
@PostMapping("/{id}/distributions/{distributionId}/receipt")
@PostMapping("/{id}/distributions/{distributionId}/remind")
```

`office_admin` or `admin` distributes approved/archived documents; the target receiver signs; authorized office staff reminds unsigned recipients. Use existing notification and audit infrastructure.

- [ ] **Step 4: Verify document closure GREEN**

```powershell
& 'D:\dev\maven\apache-maven-3.9.14\bin\mvn.cmd' -Dtest=DocumentClosureTest,WorkflowIntegrationTest test
```

Expected: PASS for approval, distribution, receipt, reminder and version tracking.

- [ ] **Step 5: Commit document closure**

```powershell
git add backend/src/main/java/com/university/oms backend/src/test/java/com/university/oms/DocumentClosureTest.java
git commit -m "feat: complete document distribution and receipt flow"
```

### Task 4: Implement Regulatory Rules For Meetings, Seals And Travel

**Files:**
- Create: `backend/src/main/java/com/university/oms/model/SealTransfer.java`
- Create: `backend/src/main/java/com/university/oms/dto/SealTransferRequest.java`
- Create: `backend/src/test/java/com/university/oms/RegulatoryRulesTest.java`
- Modify: `backend/src/main/java/com/university/oms/model/SealApplication.java`
- Modify: `backend/src/main/java/com/university/oms/model/Meeting.java`
- Modify: `backend/src/main/java/com/university/oms/model/Travel.java`
- Modify: `backend/src/main/java/com/university/oms/dto/SealApplyRequest.java`
- Modify: `backend/src/main/java/com/university/oms/dto/MeetingRequest.java`
- Modify: `backend/src/main/java/com/university/oms/dto/TravelReimburseRequest.java`
- Modify: controllers/services for seals, meetings and travels
- Modify: `backend/src/main/java/com/university/oms/design/ApprovalFlowConfig.java`

- [ ] **Step 1: Write failing regulatory tests**

Add tests asserting:

```java
assertBadRequest(createLargeMeetingStartingTomorrow(), "至少提前15个工作日");
assertEquals("pending_office", createDepartmentSeal("重大事项").get("status").asText());
assertBadRequest(reimburseApprovedTravel(null, null), "票据");
assertBadRequest(reimburseOverLimit("/receipt.pdf", null), "超标准说明");
```

Add transfer tests that an unauthorized user is forbidden and a keeper-created transfer can be queried.

- [ ] **Step 2: Run and verify RED**

```powershell
& 'D:\dev\maven\apache-maven-3.9.14\bin\mvn.cmd' -Dtest=RegulatoryRulesTest test
```

Expected: FAIL on missing lead-time, routing, receipt/explanation and transfer support.

- [ ] **Step 3: Implement rule fields and flows**

Implement:

```java
private long workingDaysBetween(LocalDate start, LocalDate eventDate);
private String sealFlowKey(Seal seal, String matterLevel);
```

Meeting creation rejects large activities with fewer than fifteen weekdays before `startTime.toLocalDate()`. Extend meeting request/model for `accommodationFee`, `mealFee`, `venueFee`, `otherFee` and validate their sum against `budget` and total standard.

Extend seal requests with `takeOutReason`, `takeOutLocation`, `supervisorId`, `expectedReturnTime`; require them for external use. Route department general/major and university major matters through additional configured steps. Store and expose `SealTransfer`.

Extend travel reimbursement with `receiptUrl` and `overLimitReason`, require receipt, require explanation when actual expense exceeds `checkResult.getStandardAmount()`, and set successful financial completion to `archived`.

- [ ] **Step 4: Run and verify GREEN**

```powershell
& 'D:\dev\maven\apache-maven-3.9.14\bin\mvn.cmd' -Dtest=RegulatoryRulesTest,OfficeManagementSystemIntegrationTest test
```

Expected: PASS for required regulatory positive and negative cases.

- [ ] **Step 5: Commit regulatory implementation**

```powershell
git add backend/src/main/java/com/university/oms backend/src/test/java/com/university/oms/RegulatoryRulesTest.java
git commit -m "feat: enforce meeting seal and travel rules"
```

### Task 5: Persist New Workflow Data And Restore MySQL Startup

**Files:**
- Create: `backend/src/test/java/com/university/oms/MysqlSchemaContractTest.java`
- Modify: `backend/src/main/java/com/university/oms/repository/DataPersistence.java`
- Modify: `backend/src/main/java/com/university/oms/repository/NoopDataPersistence.java`
- Modify: `backend/src/main/java/com/university/oms/repository/JdbcDataPersistence.java`
- Modify: `backend/src/main/java/com/university/oms/repository/MysqlDataLoader.java`
- Modify: `backend/sql/schema.sql`
- Modify: `backend/sql/data.sql`
- Modify: `backend/scripts/init-mysql.ps1`
- Modify: `backend/src/main/resources/application-mysql.properties`

- [ ] **Step 1: Add schema contract tests**

Add a test that reads `schema.sql` and requires table/column statements used by runtime:

```java
assertTrue(schema.contains("CREATE TABLE IF NOT EXISTS sys_attachment"));
assertTrue(schema.contains("CREATE TABLE IF NOT EXISTS oa_seal_transfer"));
assertTrue(schema.contains("CREATE TABLE IF NOT EXISTS oa_document_distribution"));
assertTrue(schema.contains("secret_until"));
assertTrue(schema.contains("over_limit_reason"));
```

- [ ] **Step 2: Run contract RED**

```powershell
& 'D:\dev\maven\apache-maven-3.9.14\bin\mvn.cmd' -Dtest=MysqlSchemaContractTest test
```

Expected: FAIL because new closure tables/columns are absent.

- [ ] **Step 3: Implement schema and JDBC persistence**

Update schema with idempotent table creation and `ALTER TABLE ... ADD COLUMN` migration statements for existing databases. Implement `saveSealTransfer` and `saveDocumentDistribution` in persistence classes, and load both entities in `MysqlDataLoader`.

Align initialization default:

```powershell
[string]$Password = "123456"
```

Keep parameter override support for alternate environments.

- [ ] **Step 4: Run Java tests and MySQL runtime verification**

Run:

```powershell
& 'D:\dev\maven\apache-maven-3.9.14\bin\mvn.cmd' -Dtest=MysqlSchemaContractTest test
powershell -ExecutionPolicy Bypass -File .\scripts\init-mysql.ps1 -Password '123456'
& 'D:\dev\maven\apache-maven-3.9.14\bin\mvn.cmd' spring-boot:run -Dspring-boot.run.profiles=mysql
```

Expected: contract PASS; schema initialization succeeds; `mysql` profile starts without missing-table errors. Perform API create/read and MySQL row query before stopping the process.

- [ ] **Step 5: Commit persistence recovery**

```powershell
git add backend/src/main/java/com/university/oms/repository backend/sql backend/scripts backend/src/main/resources backend/src/test/java/com/university/oms/MysqlSchemaContractTest.java
git commit -m "fix: migrate mysql schema for workflow persistence"
```

### Task 6: Scope Dashboards And Add Report Export

**Files:**
- Create: `backend/src/test/java/com/university/oms/DashboardScopeTest.java`
- Create: `backend/src/main/java/com/university/oms/controller/StatisticsController.java`
- Create: `backend/src/main/java/com/university/oms/service/StatisticsService.java`
- Modify: `backend/src/main/java/com/university/oms/design/DashboardFacade.java`
- Modify: `backend/src/main/java/com/university/oms/service/DashboardService.java`

- [ ] **Step 1: Write failing data-scope tests**

Create another user's items and assert ordinary `user` dashboard/statistics do not expose unrelated totals while `admin` sees aggregate totals:

```java
assertEquals(ownDocumentCount, getDashboard(userToken).get("documentCount").asInt());
assertTrue(getDashboard(adminToken).get("documentCount").asInt() > ownDocumentCount);
mockMvc.perform(get("/api/statistics/export").header("Authorization", bearer(userToken)))
        .andExpect(status().isOk())
        .andExpect(content().string(not(containsString("other-secret-title"))));
```

- [ ] **Step 2: Run and verify RED**

```powershell
& 'D:\dev\maven\apache-maven-3.9.14\bin\mvn.cmd' -Dtest=DashboardScopeTest test
```

Expected: FAIL because dashboard is global and statistics export does not exist.

- [ ] **Step 3: Implement scoped summaries and CSV export**

Reuse `BusinessAccessService.canReadBusiness(...)` while aggregating each module. Add `/api/statistics` JSON summary and `/api/statistics/export` UTF-8 CSV response containing only permitted, non-body summary fields.

- [ ] **Step 4: Verify data scope GREEN**

```powershell
& 'D:\dev\maven\apache-maven-3.9.14\bin\mvn.cmd' -Dtest=DashboardScopeTest test
```

Expected: PASS with scoped dashboard and export results.

- [ ] **Step 5: Commit reporting scope**

```powershell
git add backend/src/main/java/com/university/oms backend/src/test/java/com/university/oms/DashboardScopeTest.java
git commit -m "feat: scope dashboards and statistics exports"
```

### Task 7: Expose Complete Frontend Operations

**Files:**
- Create: `frontend/src/views/Statistics.vue`
- Modify: `frontend/src/api.js`
- Modify: `frontend/src/App.vue`
- Modify: `frontend/src/router/index.js`
- Modify: `frontend/src/views/Approvals.vue`
- Modify: `frontend/src/views/Documents.vue`
- Modify: `frontend/src/views/Seals.vue`
- Modify: `frontend/src/views/Meetings.vue`
- Modify: `frontend/src/views/Travels.vue`

- [ ] **Step 1: Add frontend API surfaces and user-facing actions**

Implement API methods:

```javascript
logout: () => http.post('/auth/logout'),
distributeDocument: (id, data) => http.post(`/documents/${id}/distributions`, data),
receiveDocument: (id, distributionId) => http.post(`/documents/${id}/distributions/${distributionId}/receipt`),
remindDocument: (id, distributionId) => http.post(`/documents/${id}/distributions/${distributionId}/remind`),
markSealUsed: (id) => http.post(`/seals/applications/${id}/used`),
returnSeal: (id) => http.post(`/seals/applications/${id}/returned`),
createSealTransfer: (data) => http.post('/seals/transfers', data),
statistics: () => http.get('/statistics')
```

Add icon/text action buttons and modal forms to existing compact panels: approvals allow agree/return; documents allow distribution/receipt/reminder; seals allow use/return/transfer; meeting/travel forms collect newly required fields; statistics view displays/export summaries.

- [ ] **Step 2: Build and inspect initial frontend result**

Run:

```powershell
npm run build
```

Expected: PASS after Vue templates and API calls compile; fix only errors introduced by this task.

- [ ] **Step 3: Verify UI flow against running backend**

Start backend and frontend locally, then verify the following path through Browser plugin when available:

```text
login -> create document -> approve -> distribute -> receive/remind
login -> approve seal -> record used -> return/transfer
login -> travel reimbursement with required receipt -> statistics export
logout -> protected page returns to login
```

Expected: visible actions update state without console errors and prohibited roles receive understandable failure feedback.

- [ ] **Step 4: Commit frontend closure**

```powershell
git add frontend/src
git commit -m "feat: expose complete office workflow actions"
```

### Task 8: Synchronize Documents And Run Full Verification

**Files:**
- Modify: `doc/设计.md`
- Modify: `doc/数据库初始化.md`
- Modify: `doc/接口补充-流程闭环.md`
- Modify: `doc/2026-05-25.md`

- [ ] **Step 1: Update documents to match delivered implementation**

Record the effective technology stack and rule sources in `doc/设计.md`; add explicit initialization/migration and MySQL-profile startup commands to `doc/数据库初始化.md`; document new API endpoints and permissions; append a retest section to the date-named report preserving initial findings and listing resolution evidence.

- [ ] **Step 2: Run full backend and frontend verification**

Run:

```powershell
& 'D:\dev\maven\apache-maven-3.9.14\bin\mvn.cmd' test
npm run build
```

Expected: all backend tests PASS and frontend build exits `0`.

- [ ] **Step 3: Run persistence and browser smoke verification**

Initialize the agreed local MySQL test database, start the `mysql` profile, execute one end-to-end persisted workflow and query the resulting rows. Run Browser smoke checks for desktop workflow pages; if Browser remains unavailable, explicitly document that remaining environment blocker rather than claiming browser success.

- [ ] **Step 4: Review git scope and commit final documentation**

```powershell
git status --short
git add doc/设计.md doc/数据库初始化.md doc/接口补充-流程闭环.md doc/2026-05-25.md docs/superpowers/plans/2026-05-25-compliance-remediation.md
git commit -m "docs: record compliance remediation verification"
```

Expected: generated logs/screenshots not staged unless intentionally required as user-visible evidence.
