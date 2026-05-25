# Seal Material Management Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make seal applications show the actual seal name and provide audited, permission-controlled real-file material management before an application enters approval.

**Architecture:** Extend the existing `Attachment` and workflow infrastructure rather than adding a separate seal-file subsystem. A seal application is first saved as `draft`, files are stored by a focused storage service and represented by enriched attachment metadata, and an explicit submit endpoint enters the existing approval chain only when active material exists.

**Tech Stack:** Java 8, Spring Boot 2.5.6 MVC multipart support, Spring JDBC/MySQL, JUnit 5 + MockMvc, Vue 3 + Element Plus + Axios, Vite.

---

## File Map

| Area | Files | Responsibility |
|------|-------|----------------|
| Seal application projection and lifecycle | `backend/src/main/java/com/university/oms/model/SealApplication.java`, `backend/src/main/java/com/university/oms/service/SealService.java`, `backend/src/main/java/com/university/oms/controller/SealController.java` | Return `sealName`/`materialCount`, create drafts, and submit into the established approval chain only after material validation |
| Attachment metadata and requests | `backend/src/main/java/com/university/oms/model/Attachment.java`, `backend/src/main/java/com/university/oms/dto/AttachmentUpdateRequest.java`, `backend/src/main/java/com/university/oms/dto/AttachmentDeleteRequest.java` | Carry actual file metadata and safe edit/delete inputs |
| Physical file storage | `backend/src/main/java/com/university/oms/service/AttachmentStorageService.java`, `backend/src/main/resources/application.properties` | Validate/store/read uploaded file bytes without disclosing absolute server paths |
| Material APIs and permissions | `backend/src/main/java/com/university/oms/service/WorkflowService.java`, `backend/src/main/java/com/university/oms/service/BusinessAccessService.java`, `backend/src/main/java/com/university/oms/controller/WorkflowController.java` | Upload/download/list/edit/logically delete attachments with seal state and role rules |
| Persistence | `backend/sql/schema.sql`, `backend/src/main/java/com/university/oms/repository/JdbcDataPersistence.java`, `backend/src/main/java/com/university/oms/repository/MysqlDataLoader.java` | Store attachment metadata/deletion state and reload it in MySQL mode |
| Backend tests | `backend/src/test/java/com/university/oms/SealMaterialManagementTest.java`, `backend/src/test/java/com/university/oms/MysqlSchemaContractTest.java` | Lock down lifecycle, upload/download, permission, audit, and schema behavior |
| Frontend | `frontend/src/api.js`, `frontend/src/views/Seals.vue`, `frontend/src/style.css` | Present real seal names and a usable material-management dialog |
| Delivery docs | `doc/需求.md`, `doc/设计.md`, `doc/接口文档.md`, `doc/接口补充-流程闭环.md`, `doc/数据库初始化.md`, `doc/2026-05-25.md` | Align requirements, interfaces, database setup, and verification report with the implemented material flow |

### Task 1: Draft Seal Applications and Visible Seal Names

**Files:**
- Create: `backend/src/test/java/com/university/oms/SealMaterialManagementTest.java`
- Modify: `backend/src/main/java/com/university/oms/model/SealApplication.java`
- Modify: `backend/src/main/java/com/university/oms/service/SealService.java`
- Modify: `backend/src/main/java/com/university/oms/controller/SealController.java`

- [ ] **Step 1: Write failing lifecycle tests**

Create `SealMaterialManagementTest.java` with the Spring/MockMvc setup used by `RegulatoryRulesTest` and these first tests:

```java
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "oms.upload-dir=target/test-uploads/seal-material")
class SealMaterialManagementTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void creatingSealApplicationCreatesDraftWithReadableSealName() throws Exception {
        String token = login("user");
        JsonNode application = postJson("/api/seals/applications",
                "{\"sealId\":2,\"applicantId\":2,\"purpose\":\"合同用印\",\"copies\":1,"
                        + "\"takeOut\":false,\"matterLevel\":\"常规事项\"}", token).get("data");

        assertEquals("draft", application.get("status").asText());
        assertEquals("信息科学技术学院公章", application.get("sealName").asText());
        assertEquals(0, application.get("materialCount").asInt());
    }

@Test
void draftCannotBeSubmittedWithoutUploadedMaterial() throws Exception {
        String token = login("user");
        long id = postJson("/api/seals/applications",
                "{\"sealId\":2,\"applicantId\":2,\"purpose\":\"合同用印\",\"copies\":1}", token)
                .get("data").get("id").asLong();

        mockMvc.perform(post("/api/seals/applications/" + id + "/submit")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isBadRequest());
}

private String login(String username) throws Exception {
    return postJson("/api/auth/login",
            "{\"username\":\"" + username + "\",\"password\":\"123456\"}", null)
            .get("data").get("token").asText();
}

private JsonNode postJson(String url, String json, String token) throws Exception {
    MockHttpServletRequestBuilder request = post(url).contentType(MediaType.APPLICATION_JSON).content(json);
    if (token != null) request.header("Authorization", bearer(token));
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

- [ ] **Step 2: Run the focused test and verify RED**

Run:

```powershell
cd backend
& 'D:\dev\maven\apache-maven-3.9.14\bin\mvn.cmd' -Dtest=SealMaterialManagementTest test
```

Expected: FAIL because a newly created material-less seal application currently rejects the request or immediately enters approval, does not expose `sealName`/`materialCount`, and has no `/submit` endpoint.

- [ ] **Step 3: Add response projection fields and split create from submit**

In `SealApplication.java`, add non-persisted response fields:

```java
private String sealName;
private int materialCount;

public String getSealName() { return sealName; }
public void setSealName(String sealName) { this.sealName = sealName; }
public int getMaterialCount() { return materialCount; }
public void setMaterialCount(int materialCount) { this.materialCount = materialCount; }
```

In `SealService.java`:

```java
public List<SealApplication> applications() {
    // Keep current scope filtering, then call enrich(app) before returning each visible item.
}

public SealApplication apply(SealApplyRequest request) {
    Long applicantId = AuthContext.currentUserIdOr(request.getApplicantId());
    Seal seal = requireSeal(request.getSealId());
    validateTakeOut(request);
    SealApplication application = new SealApplication();
    db.fill(application, db.nextId());
    application.setSealId(request.getSealId());
    application.setApplicantId(applicantId);
    application.setPurpose(request.getPurpose());
    application.setMaterialUrl(request.getMaterialUrl());
    application.setCopies(request.getCopies());
    application.setTakeOut(request.isTakeOut());
    application.setMatterLevel(request.getMatterLevel());
    application.setTakeOutReason(request.getTakeOutReason());
    application.setTakeOutLocation(request.getTakeOutLocation());
    application.setSupervisorId(request.getSupervisorId());
    application.setReturnDeadline(request.isTakeOut() ? request.getExpectedReturnTime() : null);
    application.setStatus("draft");
    db.sealApplications().put(application.getId(), application);
    persistence.saveSealApplication(application);
    return enrich(application);
}

public SealApplication submit(Long id) {
    SealApplication application = find(id);
    requireApplicantOrAdmin(application);
    if (activeMaterialCount(id) == 0) {
        throw new BusinessException("请至少上传一份有效用印材料后再提交");
    }
    return beginApproval(application);
}

private SealApplication beginApproval(SealApplication application) {
    Seal seal = requireSeal(application.getSealId());
    application.setStatus(seal.getSealName().contains("北京大学") ? "pending_office" : "pending_dept");
    application.setUpdatedAt(LocalDateTime.now());
    persistence.saveSealApplication(application);
    approvalService.record("seal", application.getId(), application.getApplicantId(), "submit", "提交用印申请");
    workflowService.startFlow("seal", application.getId(), application.getStatus(), application.getApplicantId());
    return enrich(application);
}

private Seal requireSeal(Long sealId) {
    Seal seal = db.seals().get(sealId);
    if (seal == null) {
        throw new BusinessException("印章不存在");
    }
    return seal;
}

private void validateTakeOut(SealApplyRequest request) {
    if (request.isTakeOut() && (blank(request.getTakeOutReason()) || blank(request.getTakeOutLocation())
            || request.getSupervisorId() == null || request.getExpectedReturnTime() == null)) {
        throw new BusinessException("外带用印必须填写原因、地点、监督人和预计归还时间");
    }
}

private void requireApplicantOrAdmin(SealApplication application) {
    User user = AuthContext.requireUser();
    if (!application.getApplicantId().equals(user.getId()) && !user.getRoleKeys().contains("admin")) {
        throw new ForbiddenException("无权提交该用印申请");
    }
    if (!"draft".equals(application.getStatus())) {
        throw new BusinessException("只有草稿用印申请可以提交");
    }
}

private SealApplication enrich(SealApplication application) {
    Seal seal = db.seals().get(application.getSealId());
    application.setSealName(seal == null ? "" : seal.getSealName());
    application.setMaterialCount(activeMaterialCount(application.getId()));
    return application;
}

private int activeMaterialCount(Long applicationId) {
    int count = 0;
    for (Attachment attachment : db.attachments()) {
        if ("seal".equals(attachment.getBizType()) && applicationId.equals(attachment.getBizId())) {
            count++;
        }
    }
    return count;
}
```

Add `POST /api/seals/applications/{id}/submit` in `SealController.java`:

```java
@PostMapping("/applications/{id}/submit")
public ApiResponse<SealApplication> submit(@PathVariable Long id) {
    return ApiResponse.ok(service.submit(id));
}
```

- [ ] **Step 4: Run focused tests and verify GREEN**

Run:

```powershell
& 'D:\dev\maven\apache-maven-3.9.14\bin\mvn.cmd' -Dtest=SealMaterialManagementTest test
```

Expected: PASS for the focused draft/name tests; the material-upload success path and affected approval-chain regression update are added before the first feature commit in Task 2.

- [ ] **Step 5: Leave lifecycle changes uncommitted until Task 2 restores the existing seal approval regression through real upload**

Do not commit at this point. `RegulatoryRulesTest.departmentMajorSealAddsOfficeApproval()` still exercises the old path-based submission contract; Task 2 updates it to the real-file flow before committing.

### Task 2: Upload and Download Real Seal Material Files

**Files:**
- Modify: `backend/src/test/java/com/university/oms/SealMaterialManagementTest.java`
- Modify: `backend/src/main/java/com/university/oms/model/Attachment.java`
- Create: `backend/src/main/java/com/university/oms/service/AttachmentStorageService.java`
- Modify: `backend/src/main/java/com/university/oms/service/WorkflowService.java`
- Modify: `backend/src/main/java/com/university/oms/controller/WorkflowController.java`
- Modify: `backend/src/main/resources/application.properties`
- Modify: `backend/src/test/java/com/university/oms/RegulatoryRulesTest.java`

- [ ] **Step 1: Add failing multipart upload/download and submit tests**

Add to `SealMaterialManagementTest.java`:

```java
@Test
void uploadedMaterialCanBeDownloadedAndAllowsDraftSubmission() throws Exception {
    String token = login("user");
    long applicationId = createDraft(token);
    MockMultipartFile file = new MockMultipartFile(
            "file", "contract.pdf", MediaType.APPLICATION_PDF_VALUE, "seal-contract".getBytes(StandardCharsets.UTF_8));

    String uploadedBody = mockMvc.perform(multipart("/api/workflow/attachments/upload")
                    .file(file)
                    .param("bizType", "seal")
                    .param("bizId", String.valueOf(applicationId))
                    .param("secrecyLevel", "内部")
                    .header("Authorization", bearer(token)))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
    long attachmentId = objectMapper.readTree(uploadedBody).get("data").get("id").asLong();

    mockMvc.perform(get("/api/workflow/attachments/" + attachmentId + "/download")
                    .header("Authorization", bearer(token)))
            .andExpect(status().isOk())
            .andExpect(content().bytes("seal-contract".getBytes(StandardCharsets.UTF_8)));

    JsonNode submitted = postJson("/api/seals/applications/" + applicationId + "/submit", "{}", token).get("data");
    assertEquals("pending_dept", submitted.get("status").asText());
    assertEquals(1, submitted.get("materialCount").asInt());
}

@Test
void uploadRejectsDisallowedOrOversizeSealMaterial() throws Exception {
    String token = login("user");
    long applicationId = createDraft(token);
    MockMultipartFile executable = new MockMultipartFile("file", "script.exe",
            "application/octet-stream", "bad".getBytes(StandardCharsets.UTF_8));

    mockMvc.perform(multipart("/api/workflow/attachments/upload")
                    .file(executable).param("bizType", "seal")
                    .param("bizId", String.valueOf(applicationId))
                    .header("Authorization", bearer(token)))
            .andExpect(status().isBadRequest());

    MockMultipartFile oversized = new MockMultipartFile("file", "large.pdf",
            MediaType.APPLICATION_PDF_VALUE, new byte[20 * 1024 * 1024 + 1]);
    mockMvc.perform(multipart("/api/workflow/attachments/upload")
                    .file(oversized).param("bizType", "seal")
                    .param("bizId", String.valueOf(applicationId))
                    .header("Authorization", bearer(token)))
            .andExpect(status().isBadRequest());
}

private long createDraft(String token) throws Exception {
    return postJson("/api/seals/applications",
            "{\"sealId\":2,\"applicantId\":2,\"purpose\":\"材料管理测试\",\"copies\":1}", token)
            .get("data").get("id").asLong();
}

private long uploadPdf(long applicationId, String token) throws Exception {
    MockMultipartFile file = new MockMultipartFile(
            "file", "contract.pdf", MediaType.APPLICATION_PDF_VALUE,
            "seal-contract".getBytes(StandardCharsets.UTF_8));
    String body = mockMvc.perform(multipart("/api/workflow/attachments/upload")
                    .file(file)
                    .param("bizType", "seal")
                    .param("bizId", String.valueOf(applicationId))
                    .param("secrecyLevel", "内部")
                    .header("Authorization", bearer(token)))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
    return objectMapper.readTree(body).get("data").get("id").asLong();
}
```

Import `MockMultipartFile`, `StandardCharsets`, static `multipart`, and static `content`.

- [ ] **Step 2: Run focused test and verify RED**

Run:

```powershell
& 'D:\dev\maven\apache-maven-3.9.14\bin\mvn.cmd' -Dtest=SealMaterialManagementTest test
```

Expected: FAIL with missing `/api/workflow/attachments/upload` and `/download` routes.

- [ ] **Step 3: Expand attachment metadata**

In `Attachment.java`, add:

```java
private String originalName;
private String storagePath;
private Long fileSize;
private String contentType;
private boolean deleted;
private Long deletedBy;
private LocalDateTime deletedAt;
private String deleteReason;

public String getOriginalName() { return originalName; }
public void setOriginalName(String originalName) { this.originalName = originalName; }
public String getStoragePath() { return storagePath; }
public void setStoragePath(String storagePath) { this.storagePath = storagePath; }
public Long getFileSize() { return fileSize; }
public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
public String getContentType() { return contentType; }
public void setContentType(String contentType) { this.contentType = contentType; }
public boolean isDeleted() { return deleted; }
public void setDeleted(boolean deleted) { this.deleted = deleted; }
public Long getDeletedBy() { return deletedBy; }
public void setDeletedBy(Long deletedBy) { this.deletedBy = deletedBy; }
public LocalDateTime getDeletedAt() { return deletedAt; }
public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }
public String getDeleteReason() { return deleteReason; }
public void setDeleteReason(String deleteReason) { this.deleteReason = deleteReason; }
```

- [ ] **Step 4: Implement controlled file storage**

Create `AttachmentStorageService.java`:

```java
@Service
public class AttachmentStorageService {
    private static final long MAX_BYTES = 20L * 1024L * 1024L;
    private static final Set<String> EXTENSIONS =
            new HashSet<String>(Arrays.asList("pdf", "doc", "docx", "jpg", "jpeg", "png"));
    private final Path uploadRoot;

    public AttachmentStorageService(@Value("${oms.upload-dir:uploads}") String uploadDir) {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public String store(Long id, MultipartFile file) {
        validate(file);
        String extension = extension(file.getOriginalFilename());
        Path target = uploadRoot.resolve(id + "." + extension).normalize();
        if (!target.startsWith(uploadRoot)) {
            throw new BusinessException("非法文件路径");
        }
        try {
            Files.createDirectories(uploadRoot);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return target.toString();
        } catch (IOException e) {
            throw new BusinessException("材料上传失败");
        }
    }

    public Resource load(String storagePath) {
        Path path = Paths.get(storagePath).toAbsolutePath().normalize();
        if (!path.startsWith(uploadRoot) || !Files.exists(path)) {
            throw new BusinessException("材料文件不可用");
        }
        return new FileSystemResource(path.toFile());
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new BusinessException("请选择用印材料文件");
        if (file.getSize() > MAX_BYTES) throw new BusinessException("单个材料不得超过20 MB");
        if (!EXTENSIONS.contains(extension(file.getOriginalFilename()))) {
            throw new BusinessException("仅支持 PDF、DOC、DOCX、JPG、JPEG、PNG 文件");
        }
    }
}
```

Add to `application.properties`:

```properties
oms.upload-dir=${user.home}/.oms/uploads
spring.servlet.multipart.max-file-size=20MB
spring.servlet.multipart.max-request-size=20MB
```

- [ ] **Step 5: Add workflow upload/download operations**

Inject `AttachmentStorageService` into `WorkflowService` and add:

```java
public Attachment uploadAttachment(String bizType, Long bizId, String secrecyLevel, MultipartFile file) {
    User user = AuthContext.requireUser();
    accessService.requireAttachmentUpload(bizType, bizId);
    Attachment attachment = new Attachment();
    db.fill(attachment, db.nextId());
    attachment.setBizType(bizType);
    attachment.setBizId(bizId);
    attachment.setFileName(file.getOriginalFilename());
    attachment.setOriginalName(file.getOriginalFilename());
    attachment.setFileSize(file.getSize());
    attachment.setContentType(file.getContentType());
    attachment.setSecrecyLevel(secrecyLevel == null ? "公开" : secrecyLevel);
    attachment.setUploaderId(user.getId());
    attachment.setStoragePath(storage.store(attachment.getId(), file));
    attachment.setFileUrl("/api/workflow/attachments/" + attachment.getId() + "/download");
    db.attachments().add(attachment);
    persistence.saveAttachment(attachment);
    audit(bizType, "upload_attachment", bizType, bizId, attachment.getFileName());
    return attachment;
}

public Attachment activeAttachment(Long id) {
    for (Attachment attachment : db.attachments()) {
        if (id.equals(attachment.getId())) {
            if (attachment.isDeleted()) {
                throw new BusinessException("材料已删除");
            }
            return attachment;
        }
    }
    throw new BusinessException("材料不存在");
}

public Resource downloadAttachment(Long id) {
    Attachment attachment = activeAttachment(id);
    accessService.requireBusinessRead(attachment.getBizType(), attachment.getBizId());
    audit(attachment.getBizType(), "download_attachment", attachment.getBizType(), attachment.getBizId(),
            attachment.getFileName());
    return storage.load(attachment.getStoragePath());
}
```

Update `SealService.activeMaterialCount(...)` once `Attachment.deleted` exists:

```java
if ("seal".equals(attachment.getBizType()) && applicationId.equals(attachment.getBizId())
        && !attachment.isDeleted()) {
    count++;
}
```

Keep the existing metadata-only `POST /api/workflow/attachments` for document/travel compatibility, but reject `bizType=seal` there so seal submissions cannot bypass real file upload:

```java
if ("seal".equals(request.getBizType())) {
    throw new BusinessException("用印材料请通过文件上传接口提交");
}
```

In `WorkflowController.java`, add:

```java
@PostMapping(value = "/attachments/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public ApiResponse<Attachment> upload(@RequestParam String bizType, @RequestParam Long bizId,
                                      @RequestParam(defaultValue = "公开") String secrecyLevel,
                                      @RequestParam("file") MultipartFile file) {
    return ApiResponse.ok(service.uploadAttachment(bizType, bizId, secrecyLevel, file));
}

@GetMapping("/attachments/{id}/download")
public ResponseEntity<Resource> download(@PathVariable Long id) {
    Attachment attachment = service.activeAttachment(id);
    Resource resource = service.downloadAttachment(id);
    return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                    .filename(attachment.getOriginalName(), StandardCharsets.UTF_8).build().toString())
            .contentType(MediaType.parseMediaType(attachment.getContentType()))
            .body(resource);
}
```

- [ ] **Step 6: Add a regression assertion that seal metadata-only attachment creation is rejected**

Add:

```java
@Test
void sealMaterialCannotBeAddedAsAnUnmanagedFileUrl() throws Exception {
    String token = login("user");
    long applicationId = createDraft(token);
    mockMvc.perform(post("/api/workflow/attachments")
                    .header("Authorization", bearer(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"bizType\":\"seal\",\"bizId\":" + applicationId
                            + ",\"fileName\":\"shortcut.pdf\",\"fileUrl\":\"/unmanaged/shortcut.pdf\"}"))
            .andExpect(status().isBadRequest());
}
```

- [ ] **Step 7: Run focused tests and verify GREEN**

Run:

```powershell
& 'D:\dev\maven\apache-maven-3.9.14\bin\mvn.cmd' -Dtest=SealMaterialManagementTest,RegulatoryRulesTest test
```

Before running this command, update `RegulatoryRulesTest.departmentMajorSealAddsOfficeApproval()` to exercise the real-file path:

```java
JsonNode application = postJson("/api/seals/applications",
        "{\"sealId\":2,\"applicantId\":2,\"purpose\":\"重大合同用印\","
                + "\"matterLevel\":\"重大事项\"}", userToken).get("data");
long applicationId = application.get("id").asLong();
MockMultipartFile file = new MockMultipartFile("file", "contract.pdf", MediaType.APPLICATION_PDF_VALUE,
        "approved material".getBytes(StandardCharsets.UTF_8));
mockMvc.perform(multipart("/api/workflow/attachments/upload").file(file)
                .param("bizType", "seal").param("bizId", String.valueOf(applicationId))
                .header("Authorization", bearer(userToken)))
        .andExpect(status().isOk());
application = postJson("/api/seals/applications/" + applicationId + "/submit", "{}", userToken).get("data");
assertEquals("pending_dept", application.get("status").asText());
```

Expected: PASS for upload, download, validation, successful submit behavior, and the existing major-seal approval chain.

- [ ] **Step 8: Commit physical upload support**

```powershell
git add backend/src/main/java/com/university/oms/model/SealApplication.java backend/src/main/java/com/university/oms/service/SealService.java backend/src/main/java/com/university/oms/controller/SealController.java backend/src/main/java/com/university/oms/model/Attachment.java backend/src/main/java/com/university/oms/service/AttachmentStorageService.java backend/src/main/java/com/university/oms/service/WorkflowService.java backend/src/main/java/com/university/oms/controller/WorkflowController.java backend/src/main/resources/application.properties backend/src/test/java/com/university/oms/SealMaterialManagementTest.java backend/src/test/java/com/university/oms/RegulatoryRulesTest.java
git commit -m "feat: submit seal applications with uploaded materials"
```

### Task 3: Material Editing, Logical Deletion, Permissions, and Audit

**Files:**
- Create: `backend/src/main/java/com/university/oms/dto/AttachmentUpdateRequest.java`
- Create: `backend/src/main/java/com/university/oms/dto/AttachmentDeleteRequest.java`
- Modify: `backend/src/main/java/com/university/oms/service/BusinessAccessService.java`
- Modify: `backend/src/main/java/com/university/oms/service/WorkflowService.java`
- Modify: `backend/src/main/java/com/university/oms/controller/WorkflowController.java`
- Modify: `backend/src/test/java/com/university/oms/SealMaterialManagementTest.java`

- [ ] **Step 1: Write failing edit/delete/authorization tests**

Add tests:

```java
@Test
void draftOwnerCanEditAndLogicallyDeleteMaterialWithAuditTrail() throws Exception {
    String user = login("user");
    long applicationId = createDraft(user);
    long attachmentId = uploadPdf(applicationId, user);

    mockMvc.perform(put("/api/workflow/attachments/" + attachmentId)
                    .header("Authorization", bearer(user))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"fileName\":\"合同定稿.pdf\",\"secrecyLevel\":\"内部\"}"))
            .andExpect(status().isOk());

    mockMvc.perform(delete("/api/workflow/attachments/" + attachmentId)
                    .header("Authorization", bearer(user))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"reason\":\"上传版本错误\"}"))
            .andExpect(status().isOk());

    JsonNode visible = getJson("/api/workflow/attachments?bizType=seal&bizId=" + applicationId, user).get("data");
    assertEquals(0, visible.size());
    JsonNode logs = getJson("/api/workflow/audit-logs?bizType=seal&bizId=" + applicationId, login("admin")).get("data");
    assertTrue(containsAction(logs, "delete_attachment"));
}

@Test
void applicantCannotDeleteSubmittedMaterialAndUnrelatedUserCannotDownloadIt() throws Exception {
    String owner = login("user");
    String finance = login("finance");
    long applicationId = createDraft(owner);
    long attachmentId = uploadPdf(applicationId, owner);
    postJson("/api/seals/applications/" + applicationId + "/submit", "{}", owner);

    mockMvc.perform(delete("/api/workflow/attachments/" + attachmentId)
                    .header("Authorization", bearer(owner))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"reason\":\"试图删除审批依据\"}"))
            .andExpect(status().isForbidden());
    mockMvc.perform(get("/api/workflow/attachments/" + attachmentId + "/download")
                    .header("Authorization", bearer(finance)))
            .andExpect(status().isForbidden());
}

@Test
void administratorCanSeeDeletedMaterialMetadataButCannotDownloadRemovedFile() throws Exception {
    String owner = login("user");
    String admin = login("admin");
    long applicationId = createDraft(owner);
    long attachmentId = uploadPdf(applicationId, owner);

    mockMvc.perform(delete("/api/workflow/attachments/" + attachmentId)
                    .header("Authorization", bearer(admin))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"reason\":\"失效材料留档\"}"))
            .andExpect(status().isOk());
    JsonNode deleted = getJson("/api/workflow/attachments?bizType=seal&bizId=" + applicationId
            + "&includeDeleted=true", admin).get("data");
    assertTrue(deleted.get(0).get("deleted").asBoolean());
    mockMvc.perform(get("/api/workflow/attachments/" + attachmentId + "/download")
                    .header("Authorization", bearer(admin)))
            .andExpect(status().isBadRequest());
}

private boolean containsAction(JsonNode rows, String action) {
    for (JsonNode row : rows) {
        if (action.equals(row.get("action").asText())) {
            return true;
        }
    }
    return false;
}
```

- [ ] **Step 2: Run focused tests and verify RED**

Run:

```powershell
& 'D:\dev\maven\apache-maven-3.9.14\bin\mvn.cmd' -Dtest=SealMaterialManagementTest test
```

Expected: FAIL because update/delete routes and state-specific attachment permissions do not exist.

- [ ] **Step 3: Add DTOs and centralized permission gates**

Create DTOs:

```java
public class AttachmentUpdateRequest {
    @NotBlank(message = "材料名称不能为空")
    private String fileName;
    @NotBlank(message = "材料密级不能为空")
    private String secrecyLevel;
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getSecrecyLevel() { return secrecyLevel; }
    public void setSecrecyLevel(String secrecyLevel) { this.secrecyLevel = secrecyLevel; }
}

public class AttachmentDeleteRequest {
    @NotBlank(message = "删除原因不能为空")
    private String reason;
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
```

In `BusinessAccessService.java`, add state-aware methods:

```java
public void requireAttachmentUpload(String bizType, Long bizId) {
    requireBusinessRead(bizType, bizId);
    if ("seal".equals(bizType)) {
        SealApplication app = requireSealApplication(bizId);
        User user = AuthContext.requireUser();
        if (!app.getApplicantId().equals(user.getId()) && !hasRole(user, "admin")) {
            deny("无权上传该用印申请材料");
        }
        if (!"draft".equals(app.getStatus()) && !app.getStatus().startsWith("pending_")) {
            deny("当前用印状态不可补充材料");
        }
    }
}

public void requireAttachmentEdit(String bizType, Long bizId) {
    SealApplication app = requireSealApplication(bizId);
    User user = AuthContext.requireUser();
    if (!"draft".equals(app.getStatus()) || !app.getApplicantId().equals(user.getId())) {
        deny("只有申请人可在草稿阶段修改材料");
    }
}

public void requireAttachmentDelete(String bizType, Long bizId) {
    SealApplication app = requireSealApplication(bizId);
    User user = AuthContext.requireUser();
    if ("draft".equals(app.getStatus()) && app.getApplicantId().equals(user.getId())) return;
    if (hasRole(user, "office_admin") || hasRole(user, "admin")) return;
    deny("无权删除该用印材料");
}

public void requireViewDeletedAttachments() {
    User user = AuthContext.requireUser();
    if (!hasRole(user, "office_admin") && !hasRole(user, "admin")) {
        deny("无权查看已删除材料记录");
    }
}

private SealApplication requireSealApplication(Long bizId) {
    SealApplication application = db.sealApplications().get(bizId);
    if (application == null) {
        throw new ForbiddenException("无权访问该用印申请");
    }
    return application;
}
```

- [ ] **Step 4: Implement metadata update and logical deletion**

In `WorkflowService.java`:

```java
public Attachment updateAttachment(Long id, AttachmentUpdateRequest request) {
    Attachment attachment = activeAttachment(id);
    accessService.requireAttachmentEdit(attachment.getBizType(), attachment.getBizId());
    attachment.setFileName(request.getFileName());
    attachment.setSecrecyLevel(request.getSecrecyLevel());
    attachment.setUpdatedAt(LocalDateTime.now());
    persistence.saveAttachment(attachment);
    audit(attachment.getBizType(), "update_attachment", attachment.getBizType(), attachment.getBizId(),
            attachment.getFileName());
    return attachment;
}

public Attachment deleteAttachment(Long id, AttachmentDeleteRequest request) {
    Attachment attachment = activeAttachment(id);
    accessService.requireAttachmentDelete(attachment.getBizType(), attachment.getBizId());
    attachment.setDeleted(true);
    attachment.setDeletedBy(AuthContext.requireUser().getId());
    attachment.setDeletedAt(LocalDateTime.now());
    attachment.setDeleteReason(request.getReason());
    attachment.setUpdatedAt(LocalDateTime.now());
    persistence.saveAttachment(attachment);
    audit(attachment.getBizType(), "delete_attachment", attachment.getBizType(), attachment.getBizId(),
            attachment.getFileName() + "；原因：" + request.getReason());
    return attachment;
}
```

Change `attachments(...)` to accept `boolean includeDeleted`: always require business read; when `includeDeleted=true`, call `accessService.requireViewDeletedAttachments()` and return deletion metadata to management roles; otherwise filter all logically deleted entries.

```java
public List<Attachment> attachments(String bizType, Long bizId, boolean includeDeleted) {
    if (bizType != null && bizId != null) {
        accessService.requireBusinessRead(bizType, bizId);
    }
    if (includeDeleted) {
        accessService.requireViewDeletedAttachments();
    }
    return db.attachments().stream()
            .filter(a -> bizType == null || bizType.equals(a.getBizType()))
            .filter(a -> bizId == null || bizId.equals(a.getBizId()))
            .filter(a -> bizType != null && bizId != null || accessService.canReadBusiness(a.getBizType(), a.getBizId()))
            .filter(a -> includeDeleted || !a.isDeleted())
            .collect(Collectors.toList());
}
```

In `WorkflowController.java`, add:

```java
@PutMapping("/attachments/{id}")
public ApiResponse<Attachment> update(@PathVariable Long id, @Valid @RequestBody AttachmentUpdateRequest request) {
    return ApiResponse.ok(service.updateAttachment(id, request));
}

@DeleteMapping("/attachments/{id}")
public ApiResponse<Attachment> delete(@PathVariable Long id, @Valid @RequestBody AttachmentDeleteRequest request) {
    return ApiResponse.ok(service.deleteAttachment(id, request));
}
```

Change the existing list route signature:

```java
@GetMapping("/attachments")
public ApiResponse<List<Attachment>> attachments(@RequestParam(required = false) String bizType,
                                                 @RequestParam(required = false) Long bizId,
                                                 @RequestParam(defaultValue = "false") boolean includeDeleted) {
    return ApiResponse.ok(service.attachments(bizType, bizId, includeDeleted));
}
```

- [ ] **Step 5: Run tests and verify GREEN**

Run:

```powershell
& 'D:\dev\maven\apache-maven-3.9.14\bin\mvn.cmd' -Dtest=SealMaterialManagementTest test
```

Expected: PASS including state and authorization assertions.

- [ ] **Step 6: Commit CRUD/security slice**

```powershell
git add backend/src/main/java/com/university/oms/dto/AttachmentUpdateRequest.java backend/src/main/java/com/university/oms/dto/AttachmentDeleteRequest.java backend/src/main/java/com/university/oms/service/BusinessAccessService.java backend/src/main/java/com/university/oms/service/WorkflowService.java backend/src/main/java/com/university/oms/controller/WorkflowController.java backend/src/test/java/com/university/oms/SealMaterialManagementTest.java
git commit -m "feat: audit seal material editing and deletion"
```

### Task 4: MySQL Persistence for Material Metadata and Deletion

**Files:**
- Modify: `backend/src/test/java/com/university/oms/MysqlSchemaContractTest.java`
- Modify: `backend/sql/schema.sql`
- Modify: `backend/src/main/java/com/university/oms/repository/JdbcDataPersistence.java`
- Modify: `backend/src/main/java/com/university/oms/repository/MysqlDataLoader.java`

- [ ] **Step 1: Write failing schema contract assertions**

In `MysqlSchemaContractTest.java`, add:

```java
assertTrue(schema.contains("original_name"));
assertTrue(schema.contains("storage_path"));
assertTrue(schema.contains("file_size"));
assertTrue(schema.contains("content_type"));
assertTrue(schema.contains("deleted_by"));
assertTrue(schema.contains("deleted_at"));
assertTrue(schema.contains("delete_reason"));
assertTrue(schema.contains("updated_at"));
assertTrue(schema.contains("CALL add_column_if_missing('sys_attachment', 'storage_path'"));
```

- [ ] **Step 2: Run schema test and verify RED**

Run:

```powershell
& 'D:\dev\maven\apache-maven-3.9.14\bin\mvn.cmd' -Dtest=MysqlSchemaContractTest test
```

Expected: FAIL because `sys_attachment` does not contain physical-storage or logical-delete metadata.

- [ ] **Step 3: Upgrade schema and persistence mapping**

Extend `CREATE TABLE IF NOT EXISTS sys_attachment`:

```sql
  original_name VARCHAR(255),
  storage_path VARCHAR(1000),
  file_size BIGINT,
  content_type VARCHAR(120),
  deleted TINYINT DEFAULT 0,
  deleted_by BIGINT,
  deleted_at DATETIME,
  delete_reason VARCHAR(500),
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
```

Add repeatable migrations:

```sql
CALL add_column_if_missing('sys_attachment', 'original_name', 'VARCHAR(255)');
CALL add_column_if_missing('sys_attachment', 'storage_path', 'VARCHAR(1000)');
CALL add_column_if_missing('sys_attachment', 'file_size', 'BIGINT');
CALL add_column_if_missing('sys_attachment', 'content_type', 'VARCHAR(120)');
CALL add_column_if_missing('sys_attachment', 'deleted', 'TINYINT DEFAULT 0');
CALL add_column_if_missing('sys_attachment', 'deleted_by', 'BIGINT');
CALL add_column_if_missing('sys_attachment', 'deleted_at', 'DATETIME');
CALL add_column_if_missing('sys_attachment', 'delete_reason', 'VARCHAR(500)');
CALL add_column_if_missing('sys_attachment', 'updated_at', 'DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP');
```

Update `JdbcDataPersistence.saveAttachment(...)`:

```java
jdbcTemplate.update("REPLACE INTO sys_attachment " +
        "(id, biz_type, biz_id, file_name, original_name, file_url, storage_path, file_size, content_type, " +
        "secrecy_level, uploader_id, deleted, deleted_by, deleted_at, delete_reason, created_at, updated_at) " +
        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
        a.getId(), a.getBizType(), a.getBizId(), a.getFileName(), a.getOriginalName(), a.getFileUrl(),
        a.getStoragePath(), a.getFileSize(), a.getContentType(), a.getSecrecyLevel(), a.getUploaderId(),
        a.isDeleted() ? 1 : 0, a.getDeletedBy(), a.getDeletedAt(), a.getDeleteReason(),
        a.getCreatedAt(), a.getUpdatedAt());
```

Map all new fields in `MysqlDataLoader.loadAttachments()`, using `rs.getObject("deleted_by")` and `toLocalDateTime(rs, "deleted_at")`.

- [ ] **Step 4: Verify schema and backend regression**

Run:

```powershell
& 'D:\dev\maven\apache-maven-3.9.14\bin\mvn.cmd' -Dtest=MysqlSchemaContractTest,SealMaterialManagementTest test
```

Expected: PASS with zero failures.

- [ ] **Step 5: Commit persistence slice**

```powershell
git add backend/sql/schema.sql backend/src/main/java/com/university/oms/repository/JdbcDataPersistence.java backend/src/main/java/com/university/oms/repository/MysqlDataLoader.java backend/src/test/java/com/university/oms/MysqlSchemaContractTest.java
git commit -m "feat: persist uploaded seal material metadata"
```

### Task 5: Build the Seal Material Frontend Workflow

**Files:**
- Modify: `frontend/src/api.js`
- Modify: `frontend/src/views/Seals.vue`
- Modify: `frontend/src/style.css`

- [ ] **Step 1: Establish the failing UI acceptance case**

Start the current app before changing the frontend:

```powershell
cd frontend
npm run dev
```

Using the Browser plugin against `http://localhost:5173/seals`, confirm the current page fails the acceptance case:

- the list displays numeric `sealId` rather than `sealName`;
- there is no “材料管理” dialog;
- there is no local-file upload or explicit draft submit action.

Expected: current page cannot complete “保存草稿 -> 上传材料 -> 提交审批”.

- [ ] **Step 2: Add frontend API methods**

In `frontend/src/api.js`, extend the seal/attachment APIs:

```javascript
submitSealApp: (id) => http.post(`/seals/applications/${id}/submit`),
uploadAttachment: (data) => http.post('/workflow/attachments/upload', data, {
  headers: { 'Content-Type': 'multipart/form-data' }
}),
downloadAttachment: (id) => http.get(`/workflow/attachments/${id}/download`, { responseType: 'blob' }),
updateAttachment: (id, data) => http.put(`/workflow/attachments/${id}`, data),
deleteAttachment: (id, data) => http.delete(`/workflow/attachments/${id}`, { data }),
```

- [ ] **Step 3: Replace path input with a material workflow**

In `Seals.vue`:

- Remove the `form.materialUrl` input from new application entry.
- Change create button text to `保存草稿`; after `api.createSealApp(form)` returns, assign the returned row to `currentApplication` and open the material dialog.
- Replace `<el-table-column prop="sealId" ...>` with:

```vue
<el-table-column prop="sealName" label="印章" min-width="180" />
<el-table-column prop="materialCount" label="材料" width="75">
  <template #default="{ row }">{{ row.materialCount }} 份</template>
</el-table-column>
```

- Add list actions:

```vue
<el-button size="small" @click="openMaterials(row)">材料管理</el-button>
<el-button v-if="row.status === 'draft'" size="small" type="primary"
  :disabled="row.materialCount === 0" @click="submitApplication(row.id)">提交审批</el-button>
```

- Add an `el-dialog` named `材料管理` with:

```vue
<el-upload :auto-upload="false" :limit="1" :on-change="chooseMaterial">
  <el-button>选择材料</el-button>
</el-upload>
<el-select v-model="uploadForm.secrecyLevel">
  <el-option v-for="level in levels" :key="level" :value="level" />
</el-select>
<el-button type="primary" :disabled="!uploadForm.file" @click="uploadMaterial">上传</el-button>
<p class="rule-note">支持 PDF、Word、JPG、PNG，单个文件不超过 20 MB。</p>
<el-table :data="materials" border>
  <el-table-column prop="fileName" label="材料名称" min-width="190" />
  <el-table-column prop="contentType" label="类型" width="135" />
  <el-table-column label="大小" width="92">
    <template #default="{ row }">{{ formatSize(row.fileSize) }}</template>
  </el-table-column>
  <el-table-column prop="secrecyLevel" label="密级" width="78" />
  <el-table-column prop="createdAt" label="上传时间" width="172" />
  <el-table-column label="操作" min-width="205">
    <template #default="{ row }">
      <el-button size="small" @click="downloadMaterial(row)">下载</el-button>
      <el-button v-if="canEditMaterial" size="small" @click="editMaterial(row)">修改</el-button>
      <el-button v-if="canDeleteMaterial" size="small" type="danger" @click="deleteMaterial(row)">删除</el-button>
    </template>
  </el-table-column>
</el-table>
<el-alert v-if="materials.length === 0 && currentApplication?.materialUrl"
  type="info" :closable="false"
  :title="`历史材料地址：${currentApplication.materialUrl}`" />
```

Add handlers:

```javascript
const materialDialog = ref(false)
const currentApplication = ref(null)
const materials = ref([])
const uploadForm = reactive({ file: null, secrecyLevel: '公开' })
const includeDeleted = ref(false)
const canEditMaterial = computed(() => currentApplication.value?.status === 'draft'
  && currentApplication.value?.applicantId === currentUser.id)
const canDeleteMaterial = computed(() => canEditMaterial.value
  || currentUser.roleKeys?.some((role) => ['office_admin', 'admin'].includes(role)))
const canViewDeleted = computed(() => currentUser.roleKeys?.some((role) => ['office_admin', 'admin'].includes(role)))

const openMaterials = async (application) => {
  currentApplication.value = application
  materials.value = await api.attachments({
    bizType: 'seal',
    bizId: application.id,
    includeDeleted: canViewDeleted.value && includeDeleted.value
  })
  materialDialog.value = true
}
const uploadMaterial = async () => {
  const data = new FormData()
  data.append('file', uploadForm.file.raw)
  data.append('bizType', 'seal')
  data.append('bizId', currentApplication.value.id)
  data.append('secrecyLevel', uploadForm.secrecyLevel)
  await api.uploadAttachment(data)
  await openMaterials(currentApplication.value)
  await load()
}
const submitApplication = async (id) => {
  await api.submitSealApp(id)
  ElMessage.success('已提交审批')
  await load()
}
const downloadMaterial = async (row) => {
  const blob = await api.downloadAttachment(row.id)
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = row.originalName || row.fileName
  link.click()
  URL.revokeObjectURL(link.href)
}
const editMaterial = async (row) => {
  const { value } = await ElMessageBox.prompt('请输入材料显示名称', '修改材料', { inputValue: row.fileName })
  await api.updateAttachment(row.id, { fileName: value, secrecyLevel: row.secrecyLevel })
  await openMaterials(currentApplication.value)
}
const deleteMaterial = async (row) => {
  const { value } = await ElMessageBox.prompt('请输入删除原因', '删除材料')
  await api.deleteAttachment(row.id, { reason: value })
  await openMaterials(currentApplication.value)
  await load()
}
const formatSize = (size) => size < 1024 * 1024
  ? `${Math.ceil(size / 1024)} KB`
  : `${(size / (1024 * 1024)).toFixed(1)} MB`
```

Place this toggle above the material table so management users can see deletion metadata without exposing removed files to ordinary users:

```vue
<el-switch v-if="canViewDeleted" v-model="includeDeleted" active-text="显示已删除"
  @change="openMaterials(currentApplication)" />
<el-tag v-if="row.deleted" type="info">已删除</el-tag>
```

For edit/delete, use `ElMessageBox.prompt` to collect the display name/delete reason and call the new API methods; only render edit/delete actions for drafts owned by the current user or management deletion permissions.

- [ ] **Step 4: Add compact dialog/table styling**

In `frontend/src/style.css`, add only reusable layout helpers required by the dialog:

```css
.upload-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
  margin-bottom: 12px;
}

.material-name {
  max-width: 220px;
  overflow-wrap: anywhere;
}
```

- [ ] **Step 5: Build frontend and repeat browser acceptance**

Run:

```powershell
cd frontend
npm run build
```

Expected: Vite build exits `0`.

Using Browser (or Playwright fallback only if Browser initialization is blocked), verify:

- real seal names appear in the application list;
- creating a draft opens material management;
- PDF upload increments material count and enables submit;
- submitted application no longer offers applicant edit/delete controls;
- download returns the uploaded file;
- console has no application errors.

- [ ] **Step 6: Commit frontend slice**

```powershell
git add frontend/src/api.js frontend/src/views/Seals.vue frontend/src/style.css
git commit -m "feat: manage seal materials in the frontend"
```

### Task 6: Documentation, MySQL Round Trip, and Full Verification

**Files:**
- Modify: `doc/需求.md`
- Modify: `doc/设计.md`
- Modify: `doc/接口文档.md`
- Modify: `doc/接口补充-流程闭环.md`
- Modify: `doc/数据库初始化.md`
- Modify: `doc/2026-05-25.md`

- [ ] **Step 1: Update delivery documentation**

Document these implemented facts:

Add the following requirement/design statements in their matching seal and attachment sections:

```markdown
- 用印申请以草稿保存，上传至少一份有效材料后方可提交审批。
- 用印材料支持真实文件上传、授权下载、元数据修改与保留审计记录的逻辑删除。
- 用印登记列表展示印章名称，不以材料路径或印章编号代替印章身份。
```

Add the implemented endpoint rows to `doc/接口补充-流程闭环.md` and `doc/接口文档.md`:

```markdown
| POST | `/api/seals/applications/{id}/submit` | 校验有效材料后提交用印审批 |
| POST | `/api/workflow/attachments/upload` | 上传并绑定真实业务材料文件 |
| GET | `/api/workflow/attachments/{id}/download` | 权限校验后下载文件 |
| PUT | `/api/workflow/attachments/{id}` | 修改材料名称和密级 |
| DELETE | `/api/workflow/attachments/{id}` | 携带删除原因执行逻辑删除 |
```

Add the new attachment persistence fields and `oms.upload-dir` explanation to `doc/数据库初始化.md`; add concrete backend/MySQL/browser results to `doc/2026-05-25.md` after verification output is available.

- [ ] **Step 2: Run full backend and frontend verification**

Run:

```powershell
cd backend
& 'D:\dev\maven\apache-maven-3.9.14\bin\mvn.cmd' test
cd ..\frontend
npm run build
```

Expected: Maven reports `Failures: 0, Errors: 0`; Vite exits `0`.

- [ ] **Step 3: Run MySQL initialization and live persistence verification**

Run from `backend/`:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\init-mysql.ps1 -Password '123456'
powershell -ExecutionPolicy Bypass -File .\scripts\init-mysql.ps1 -Password '123456'
& 'D:\dev\maven\apache-maven-3.9.14\bin\mvn.cmd' spring-boot:run -Dspring-boot.run.profiles=mysql
```

Through API calls:

1. create a seal draft;
2. upload a small PDF;
3. submit the draft;
4. update/delete a separate draft material with reason;
5. query MySQL `sys_attachment` for `storage_path`, `deleted`, `deleted_by`, `deleted_at`, and `delete_reason`;
6. restart the backend and read the application/material list again.

Expected: the active material reloads with the correct seal name and count; logically deleted material remains stored in MySQL but is absent from ordinary material listing.

- [ ] **Step 4: Complete browser smoke test**

Open `http://localhost:5173/` and verify the applicant, approver, and administrator views:

- applicant recognizes seal name, uploads/downloads material, and submits only after upload;
- approver can read/download submitted material without edit/delete actions;
- administrator can logically delete a material with a required reason;
- operation history records upload/download/update/delete events;
- no console errors are introduced.

- [ ] **Step 5: Commit docs and verification record**

```powershell
git add doc/需求.md doc/设计.md doc/接口文档.md doc/接口补充-流程闭环.md doc/数据库初始化.md doc/2026-05-25.md
git commit -m "docs: document audited seal material management"
```

- [ ] **Step 6: Push verified feature commits**

```powershell
git status --short --branch
git push origin codex/compliance-remediation-exec
```

Expected: working tree is clean and GitHub branch advances to the final verified commit.
