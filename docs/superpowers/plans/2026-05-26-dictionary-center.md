# Dictionary Center Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a database-backed dictionary center so all user-facing business enumerations are selected and displayed with maintained Chinese descriptions while workflow and permission logic retains stable codes.

**Architecture:** Add dictionary type/item domain objects to the existing in-memory repository abstraction and MySQL persistence path, expose authenticated read APIs plus admin maintenance APIs, and inject a focused dictionary validator into business creation/update paths. On the frontend, a Pinia dictionary store caches the versioned catalog in `localStorage`; all pages render labels and select options through shared helpers, while a new admin page maintains ordinary dictionary values and safely edits protected labels.

**Tech Stack:** Java 8, Spring Boot 2.5.6 MVC, Spring JDBC/MySQL, JUnit 5 + MockMvc, Vue 3, Pinia, Element Plus, Axios, Vite, Vitest.

---

## File Map

| Area | Files | Responsibility |
| --- | --- | --- |
| Dictionary domain | `backend/src/main/java/com/university/oms/model/DictionaryType.java`, `DictionaryItem.java`; `backend/src/main/java/com/university/oms/dto/DictionaryTypeRequest.java`, `DictionaryItemRequest.java`, `DictionaryCatalogResponse.java` | Define persisted dictionary records, safe admin inputs, and versioned catalog response |
| In-memory and MySQL persistence | `backend/src/main/java/com/university/oms/repository/InMemoryDatabase.java`, `DataPersistence.java`, `NoopDataPersistence.java`, `JdbcDataPersistence.java`, `MysqlDataLoader.java`; `backend/sql/schema.sql`, `backend/sql/data.sql` | Seed, persist, reload, and migrate `sys_dict_type` / `sys_dict_item` records |
| Dictionary APIs and validation | `backend/src/main/java/com/university/oms/service/DictionaryService.java`, `backend/src/main/java/com/university/oms/controller/DictionaryController.java` | Query versioned catalog, administer types/items, protect system codes, audit changes, validate enabled selectable values |
| Business integration | `backend/src/main/java/com/university/oms/service/DocumentService.java`, `SealService.java`, `MeetingService.java`, `ReportService.java`, `TravelService.java`, `WorkflowService.java` | Validate new business choices through the dictionary service without changing workflow status rules |
| Backend verification | `backend/src/test/java/com/university/oms/DictionaryCenterIntegrationTest.java`, `DictionaryBusinessValidationTest.java`, `MysqlSchemaContractTest.java` | Lock down APIs, protection, disabled-option rejection, audit, and SQL seed/schema contract |
| Frontend dictionary platform | `frontend/package.json`, `frontend/src/api.js`, `frontend/src/stores/dictionary.js`, `frontend/src/utils/dictionaries.js`, `frontend/src/utils/dictionaries.spec.js`, `frontend/src/main.js`, `frontend/src/views/Login.vue`, `frontend/src/App.vue`, `frontend/src/router/index.js` | Fetch/cache catalogs, label/option helpers, catalog bootstrapping, route/navigation wiring |
| Frontend page adoption | `frontend/src/views/Documents.vue`, `Seals.vue`, `Meetings.vue`, `Reports.vue`, `Travels.vue`, `Approvals.vue`, `Dashboard.vue`, `Statistics.vue`, `UserManage.vue`, `DictionaryManage.vue`, `frontend/src/style.css` | Replace exposed codes and hard-coded option arrays; supply admin maintenance UI and name selectors |
| Delivery docs | `doc/需求.md`, `doc/设计.md`, `doc/接口文档.md`, `doc/接口补充-流程闭环.md`, `doc/数据库初始化.md`, `doc/2026-05-26.md` | Align deliverable documents and record final test evidence |

## Dictionary Seed Contract

These initial dictionary values are inserted in both the memory seed and `backend/sql/data.sql`; codes are sent to the backend and labels are presented to users.

| Type | Items (`code=label`) | Protected |
| --- | --- | --- |
| `business_status` | `draft=草稿`, `pending_dept=部门负责人审批中`, `pending_office=党办校办审核中`, `pending_leader=校级领导审批中`, `pending_security=保卫部门审核中`, `pending_finance=财务审核中`, `pending_secret_review=保密审查中`, `approved=审批通过`, `archived=已归档`, `rejected=已退回`, `used=已用印`, `returned=已归还`, `running=办理中`, `completed=已完成`, `pending=待办理` | yes |
| `distribution_status` | `not_distributed=未分发`, `distributed=待签收`, `partially_received=部分签收`, `received=已签收` | yes |
| `biz_type` | `document=公文`, `seal=用印`, `meeting=会议`, `travel=差旅`, `report=请示报告` | yes |
| `flow_node` | `pending_dept=部门负责人审批`, `pending_office=党办校办审核`, `pending_leader=校级领导审批`, `pending_security=保卫部门审核`, `pending_finance=财务审核`, `pending_secret_review=保密审查`, `approved=审批完成`, `archived=归档完成` | yes |
| `role_key` | `admin=系统管理员`, `office_user=普通办公人员`, `dept_head=部门负责人`, `school_leader=校级领导`, `office_admin=党办校办人员`, `finance_staff=财务人员`, `security_staff=保卫人员`, `seal_keeper=印章保管人` | yes |
| `secrecy_level` | `公开=公开`, `内部=内部`, `秘密=秘密`, `机密=机密`, `绝密=绝密` | yes |
| `document_type` | `通知=通知`, `决定=决定`, `请示=请示`, `批复=批复`, `报告=报告`, `函=函`, `公告=公告` | no |
| `matter_level` | `常规事项=常规事项`, `一般事项=一般事项`, `重大事项=重大事项` | no |
| `seal_type` | `行政印章=行政印章`, `部门印章=部门印章`, `专用章=专用章`, `名章=名章` | no |
| `seal_status` | `in_store=在库`, `in_use=使用中`, `lent=外带中`, `retired=已停用` | no |
| `meeting_type` | `国内管理会议=国内管理会议`, `国内业务会议=国内业务会议`, `在华举办的国际会议=在华举办的国际会议` | no |
| `venue_type` | `室内=室内`, `室外=室外` | no |
| `report_type` | `请示=请示`, `报告=报告` | no |
| `staff_level` | `一类=一类人员`, `二类=二类人员`, `三类=三类人员` | no |
| `travel_type` | `教学科研业务=教学科研业务`, `行政管理业务=行政管理业务`, `学术交流=学术交流`, `其他业务=其他业务` | no |
| `transport_type` | `飞机=飞机`, `高铁一等座=高铁一等座`, `高铁二等座=高铁二等座`, `火车软卧=火车软卧`, `火车硬卧=火车硬卧`, `火车硬座=火车硬座` | no |

### Task 1: Persisted Dictionary Model and Initialization

**Files:**
- Create: `backend/src/main/java/com/university/oms/model/DictionaryType.java`
- Create: `backend/src/main/java/com/university/oms/model/DictionaryItem.java`
- Modify: `backend/src/main/java/com/university/oms/repository/InMemoryDatabase.java`
- Modify: `backend/src/main/java/com/university/oms/repository/DataPersistence.java`
- Modify: `backend/src/main/java/com/university/oms/repository/NoopDataPersistence.java`
- Modify: `backend/src/main/java/com/university/oms/repository/JdbcDataPersistence.java`
- Modify: `backend/src/main/java/com/university/oms/repository/MysqlDataLoader.java`
- Modify: `backend/sql/schema.sql`
- Modify: `backend/sql/data.sql`
- Test: `backend/src/test/java/com/university/oms/MysqlSchemaContractTest.java`

- [ ] **Step 1: Add failing SQL contract assertions**

Extend `MysqlSchemaContractTest.schemaProvidesTablesAndColumnsRequiredByWorkflowClosures()`:

```java
assertTrue(schema.contains("CREATE TABLE IF NOT EXISTS sys_dict_type"));
assertTrue(schema.contains("CREATE TABLE IF NOT EXISTS sys_dict_item"));
assertTrue(schema.contains("dict_type VARCHAR(60) NOT NULL UNIQUE"));
assertTrue(schema.contains("UNIQUE KEY uk_dict_item_code (dict_type, dict_code)"));
String data = new String(Files.readAllBytes(Paths.get("sql", "data.sql")), StandardCharsets.UTF_8);
assertTrue(data.contains("'business_status'"));
assertTrue(data.contains("'pending_dept', '部门负责人审批中'"));
assertTrue(data.contains("'role_key'"));
assertTrue(data.contains("'meeting_type'"));
```

- [ ] **Step 2: Run the focused test to verify RED**

Run from `backend/`:

```powershell
& 'D:\dev\maven\apache-maven-3.9.14\bin\mvn.cmd' -Dtest=MysqlSchemaContractTest test
```

Expected: FAIL because `sys_dict_type`, `sys_dict_item`, and dictionary seed statements do not yet exist.

- [ ] **Step 3: Define dictionary entities and repository maps**

Create two `BaseEntity` subclasses:

```java
public class DictionaryType extends BaseEntity {
    private String dictType;
    private String dictName;
    private boolean systemType;
    private boolean enabled = true;
    private String remark;
    public String getDictType() { return dictType; }
    public void setDictType(String dictType) { this.dictType = dictType; }
    public String getDictName() { return dictName; }
    public void setDictName(String dictName) { this.dictName = dictName; }
    public boolean isSystemType() { return systemType; }
    public void setSystemType(boolean systemType) { this.systemType = systemType; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}

public class DictionaryItem extends BaseEntity {
    private String dictType;
    private String dictCode;
    private String dictLabel;
    private Integer sortOrder = 0;
    private boolean enabled = true;
    private boolean systemItem;
    private String remark;
    public String getDictType() { return dictType; }
    public void setDictType(String dictType) { this.dictType = dictType; }
    public String getDictCode() { return dictCode; }
    public void setDictCode(String dictCode) { this.dictCode = dictCode; }
    public String getDictLabel() { return dictLabel; }
    public void setDictLabel(String dictLabel) { this.dictLabel = dictLabel; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean isSystemItem() { return systemItem; }
    public void setSystemItem(boolean systemItem) { this.systemItem = systemItem; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
```

In `InMemoryDatabase`, add:

```java
private final Map<String, DictionaryType> dictionaryTypes = new ConcurrentHashMap<String, DictionaryType>();
private final Map<String, DictionaryItem> dictionaryItems = new ConcurrentHashMap<String, DictionaryItem>();
public Map<String, DictionaryType> dictionaryTypes() { return dictionaryTypes; }
public Map<String, DictionaryItem> dictionaryItems() { return dictionaryItems; }
private String dictKey(String type, String code) { return type + ":" + code; }
```

Add helpers and call them once for every row in the Dictionary Seed Contract during `init()`:

```java
private void addDictType(Long id, String code, String name, boolean systemType) {
    DictionaryType type = new DictionaryType();
    fill(type, id);
    type.setDictType(code);
    type.setDictName(name);
    type.setSystemType(systemType);
    type.setEnabled(true);
    dictionaryTypes.put(code, type);
}

private void addDictItem(Long id, String type, String code, String label, int order, boolean systemItem) {
    DictionaryItem item = new DictionaryItem();
    fill(item, id);
    item.setDictType(type);
    item.setDictCode(code);
    item.setDictLabel(label);
    item.setSortOrder(order);
    item.setEnabled(true);
    item.setSystemItem(systemItem);
    dictionaryItems.put(dictKey(type, code), item);
}
```

- [ ] **Step 4: Add MySQL schema and repeatable seed records**

In `backend/sql/schema.sql` create:

```sql
CREATE TABLE IF NOT EXISTS sys_dict_type (
  id BIGINT PRIMARY KEY,
  dict_type VARCHAR(60) NOT NULL UNIQUE,
  dict_name VARCHAR(100) NOT NULL,
  system_type TINYINT DEFAULT 0,
  enabled TINYINT DEFAULT 1,
  remark VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_dict_item (
  id BIGINT PRIMARY KEY,
  dict_type VARCHAR(60) NOT NULL,
  dict_code VARCHAR(100) NOT NULL,
  dict_label VARCHAR(100) NOT NULL,
  sort_order INT DEFAULT 0,
  enabled TINYINT DEFAULT 1,
  system_item TINYINT DEFAULT 0,
  remark VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_dict_item_code (dict_type, dict_code),
  INDEX idx_dict_item_type_enabled (dict_type, enabled, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

In `backend/sql/data.sql`, insert all seed types and items with explicit IDs below `1000` through `INSERT IGNORE`. Re-running initialization must add missing predefined records but must not overwrite administrator-maintained labels, sort orders, remarks, or enabled flags.

- [ ] **Step 5: Extend persistence and MySQL loading**

Add methods to `DataPersistence` and both implementations:

```java
void saveDictionaryType(DictionaryType type);
void saveDictionaryItem(DictionaryItem item);
```

In `JdbcDataPersistence`, implement them with `REPLACE INTO sys_dict_type` and `REPLACE INTO sys_dict_item`; in `MysqlDataLoader.load()` clear/load the dictionary maps before loading business records:

```java
private void loadDictionaryTypes() {
    jdbcTemplate.query("SELECT * FROM sys_dict_type", rs -> {
        DictionaryType type = new DictionaryType();
        type.setId(rs.getLong("id"));
        type.setCreatedAt(toLocalDateTime(rs, "created_at"));
        type.setUpdatedAt(toLocalDateTime(rs, "updated_at"));
        type.setDictType(rs.getString("dict_type"));
        type.setDictName(rs.getString("dict_name"));
        type.setSystemType(rs.getInt("system_type") == 1);
        type.setEnabled(rs.getInt("enabled") == 1);
        type.setRemark(rs.getString("remark"));
        db.dictionaryTypes().put(type.getDictType(), type);
    });
}

private void loadDictionaryItems() {
    jdbcTemplate.query("SELECT * FROM sys_dict_item", rs -> {
        DictionaryItem item = new DictionaryItem();
        item.setId(rs.getLong("id"));
        item.setCreatedAt(toLocalDateTime(rs, "created_at"));
        item.setUpdatedAt(toLocalDateTime(rs, "updated_at"));
        item.setDictType(rs.getString("dict_type"));
        item.setDictCode(rs.getString("dict_code"));
        item.setDictLabel(rs.getString("dict_label"));
        item.setSortOrder(rs.getInt("sort_order"));
        item.setEnabled(rs.getInt("enabled") == 1);
        item.setSystemItem(rs.getInt("system_item") == 1);
        item.setRemark(rs.getString("remark"));
        db.dictionaryItems().put(item.getDictType() + ":" + item.getDictCode(), item);
    });
}
```

Include both tables in `maxId()` so newly managed dictionary rows do not collide with loaded IDs.

- [ ] **Step 6: Run the SQL contract test to verify GREEN**

Run:

```powershell
& 'D:\dev\maven\apache-maven-3.9.14\bin\mvn.cmd' -Dtest=MysqlSchemaContractTest test
```

Expected: PASS.

- [ ] **Step 7: Commit the persistent dictionary foundation**

```powershell
git add backend/src/main/java/com/university/oms/model backend/src/main/java/com/university/oms/repository backend/sql backend/src/test/java/com/university/oms/MysqlSchemaContractTest.java
git commit -m "feat: persist dictionary catalog and seeds"
```

### Task 2: Dictionary Query and Admin Maintenance APIs

**Files:**
- Create: `backend/src/main/java/com/university/oms/dto/DictionaryCatalogResponse.java`
- Create: `backend/src/main/java/com/university/oms/dto/DictionaryTypeRequest.java`
- Create: `backend/src/main/java/com/university/oms/dto/DictionaryItemRequest.java`
- Create: `backend/src/main/java/com/university/oms/service/DictionaryService.java`
- Create: `backend/src/main/java/com/university/oms/controller/DictionaryController.java`
- Test: `backend/src/test/java/com/university/oms/DictionaryCenterIntegrationTest.java`

- [ ] **Step 1: Write failing API, security, protection, and audit tests**

Create a `@SpringBootTest @AutoConfigureMockMvc @DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)` test using the login helpers from `SecurityIntegrationTest`, so dictionary maintenance does not leak into unrelated suites:

```java
@Test
void authenticatedUserReceivesVersionedDictionaryCatalog() throws Exception {
    JsonNode data = getJson("/api/dictionaries", login("user")).get("data");
    assertTrue(data.get("version").asText().length() > 0);
    assertEquals("草稿", findLabel(data, "business_status", "draft"));
    assertEquals("部门负责人审批中", findLabel(data, "business_status", "pending_dept"));
}

@Test
void ordinaryUserCannotMaintainDictionaries() throws Exception {
    mockMvc.perform(put("/api/admin/dictionaries/types/meeting_type/items/国内管理会议")
            .header("Authorization", bearer(login("user")))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"dictLabel\":\"教学会议\",\"sortOrder\":1,\"enabled\":true,\"remark\":\"\"}"))
            .andExpect(status().isForbidden());
}

@Test
void adminCanRenameOrdinaryItemAndDisabledItemsRemainInCatalog() throws Exception {
    String admin = login("admin");
    putJson("/api/admin/dictionaries/types/meeting_type/items/国内管理会议",
            "{\"dictLabel\":\"国内行政管理会议\",\"sortOrder\":1,\"enabled\":false,\"remark\":\"暂不新增\"}", admin);
    JsonNode data = getJson("/api/dictionaries", admin).get("data");
    JsonNode item = findItem(data, "meeting_type", "国内管理会议");
    assertEquals("国内行政管理会议", item.get("label").asText());
    assertFalse(item.get("enabled").asBoolean());
}

@Test
void adminCanCreateAndDisableOrdinaryDictionaryType() throws Exception {
    String admin = login("admin");
    postJson("/api/admin/dictionaries/types",
            "{\"dictType\":\"campus_area\",\"dictName\":\"校区\",\"enabled\":true,\"remark\":\"场地辅助选项\"}", admin);
    JsonNode updated = putJson("/api/admin/dictionaries/types/campus_area",
            "{\"dictType\":\"campus_area\",\"dictName\":\"校区\",\"enabled\":false,\"remark\":\"已停用\"}", admin);
    assertFalse(updated.get("data").get("enabled").asBoolean());
}

@Test
void adminCannotDisableOrRenameCodeOfProtectedItem() throws Exception {
    String admin = login("admin");
    mockMvc.perform(put("/api/admin/dictionaries/types/business_status/items/draft")
            .header("Authorization", bearer(admin))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"dictCode\":\"new_draft\",\"dictLabel\":\"待拟稿\",\"sortOrder\":1,\"enabled\":false}"))
            .andExpect(status().isBadRequest());
}
```

Add an assertion after ordinary item maintenance that `/api/workflow/audit-logs` contains module `dictionary`, action `update_item`, and detail containing `meeting_type`.

- [ ] **Step 2: Run the API test to verify RED**

Run:

```powershell
& 'D:\dev\maven\apache-maven-3.9.14\bin\mvn.cmd' -Dtest=DictionaryCenterIntegrationTest test
```

Expected: FAIL/404 because dictionary endpoints and service do not exist.

- [ ] **Step 3: Create request/response DTOs**

Use these DTO shapes:

```java
public class DictionaryCatalogResponse {
    private String version;
    private Map<String, List<DictionaryItem>> dictionaries;
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public Map<String, List<DictionaryItem>> getDictionaries() { return dictionaries; }
    public void setDictionaries(Map<String, List<DictionaryItem>> dictionaries) { this.dictionaries = dictionaries; }
}

public class DictionaryTypeRequest {
    @NotBlank private String dictType;
    @NotBlank private String dictName;
    private boolean enabled = true;
    private String remark;
    public String getDictType() { return dictType; }
    public void setDictType(String dictType) { this.dictType = dictType; }
    public String getDictName() { return dictName; }
    public void setDictName(String dictName) { this.dictName = dictName; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}

public class DictionaryItemRequest {
    private String dictCode;
    @NotBlank private String dictLabel;
    private Integer sortOrder = 0;
    private boolean enabled = true;
    private String remark;
    public String getDictCode() { return dictCode; }
    public void setDictCode(String dictCode) { this.dictCode = dictCode; }
    public String getDictLabel() { return dictLabel; }
    public void setDictLabel(String dictLabel) { this.dictLabel = dictLabel; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
```

- [ ] **Step 4: Implement dictionary service with protected-code policy**

Create `DictionaryService` using `InMemoryDatabase`, `DataPersistence`, and `AuthContext`. It exposes:

```java
public DictionaryCatalogResponse catalog();
public String version();
public List<DictionaryType> listTypes();
public List<DictionaryItem> listItems(String dictType);
public DictionaryType createType(DictionaryTypeRequest request);
public DictionaryType updateType(String dictType, DictionaryTypeRequest request);
public DictionaryItem createItem(String dictType, DictionaryItemRequest request);
public DictionaryItem updateItem(String dictType, String code, DictionaryItemRequest request);
public void requireEnabled(String dictType, String code, String fieldLabel);
```

Implementation rules:

```java
private static final Set<String> SYSTEM_TYPES = new LinkedHashSet<String>(
        Arrays.asList("business_status", "distribution_status", "biz_type",
                "flow_node", "role_key", "secrecy_level"));

public void requireEnabled(String type, String code, String label) {
    DictionaryItem item = db.dictionaryItems().get(type + ":" + code);
    if (item == null || !item.isEnabled()) {
        throw new BusinessException(label + "不在可选字典范围内：" + code);
    }
}
```

Reject attempts to change an existing type's `dictType` or an existing item's `dictCode`, because stored records depend on stable keys. For any `system_type` type, additionally reject disabling the type or disabling its `systemItem` rows. Permit changing `dictLabel`, `sortOrder`, and `remark`; permit enabling/disabling only ordinary types/items. For every create/update, set `updatedAt = LocalDateTime.now()`, call persistence, and create an `AuditLog` directly through `DataPersistence.saveAuditLog(...)` so `WorkflowService` does not need to be injected into `DictionaryService`.

Version is the maximum `updatedAt` across types and items formatted as ISO local date-time; catalog sorts type items by `sortOrder`, then `dictCode`, and includes disabled items for history rendering.

- [ ] **Step 5: Expose common and administrator endpoints**

Create `DictionaryController`:

```java
@RestController
public class DictionaryController {
    @GetMapping("/api/dictionaries")
    public ApiResponse<DictionaryCatalogResponse> catalog() { return ApiResponse.ok(service.catalog()); }

    @GetMapping("/api/dictionaries/version")
    public ApiResponse<String> version() { return ApiResponse.ok(service.version()); }

    @GetMapping("/api/admin/dictionaries/types")
    public ApiResponse<List<DictionaryType>> types() { return ApiResponse.ok(service.listTypes()); }

    @PostMapping("/api/admin/dictionaries/types")
    public ApiResponse<DictionaryType> createType(@Valid @RequestBody DictionaryTypeRequest request) {
        return ApiResponse.ok(service.createType(request));
    }

    @PutMapping("/api/admin/dictionaries/types/{dictType}")
    public ApiResponse<DictionaryType> updateType(@PathVariable String dictType,
                                                   @Valid @RequestBody DictionaryTypeRequest request) {
        return ApiResponse.ok(service.updateType(dictType, request));
    }

    @GetMapping("/api/admin/dictionaries/types/{dictType}/items")
    public ApiResponse<List<DictionaryItem>> items(@PathVariable String dictType) {
        return ApiResponse.ok(service.listItems(dictType));
    }

    @PostMapping("/api/admin/dictionaries/types/{dictType}/items")
    public ApiResponse<DictionaryItem> createItem(@PathVariable String dictType,
                                                   @Valid @RequestBody DictionaryItemRequest request) {
        return ApiResponse.ok(service.createItem(dictType, request));
    }

    @PutMapping("/api/admin/dictionaries/types/{dictType}/items/{code}")
    public ApiResponse<DictionaryItem> updateItem(@PathVariable String dictType, @PathVariable String code,
                                                   @Valid @RequestBody DictionaryItemRequest request) {
        return ApiResponse.ok(service.updateItem(dictType, code, request));
    }
}
```

The existing `AuthInterceptor` already protects `/api/admin/**`, while catalog/version stay authenticated for logged-in users.

- [ ] **Step 6: Run API tests to verify GREEN**

Run:

```powershell
& 'D:\dev\maven\apache-maven-3.9.14\bin\mvn.cmd' -Dtest=DictionaryCenterIntegrationTest test
```

Expected: PASS.

- [ ] **Step 7: Commit API work**

```powershell
git add backend/src/main/java/com/university/oms/dto backend/src/main/java/com/university/oms/service/DictionaryService.java backend/src/main/java/com/university/oms/controller/DictionaryController.java backend/src/test/java/com/university/oms/DictionaryCenterIntegrationTest.java
git commit -m "feat: expose dictionary query and administration APIs"
```

### Task 3: Enforce Dictionary Choices on Business Writes

**Files:**
- Create: `backend/src/test/java/com/university/oms/DictionaryBusinessValidationTest.java`
- Modify: `backend/src/main/java/com/university/oms/service/DocumentService.java`
- Modify: `backend/src/main/java/com/university/oms/service/SealService.java`
- Modify: `backend/src/main/java/com/university/oms/service/MeetingService.java`
- Modify: `backend/src/main/java/com/university/oms/service/ReportService.java`
- Modify: `backend/src/main/java/com/university/oms/service/TravelService.java`
- Modify: `backend/src/main/java/com/university/oms/service/WorkflowService.java`

- [ ] **Step 1: Write failing validation tests**

Create service/API integration tests that log in as `admin` to disable an ordinary option and as `user` to submit a payload:

```java
@Test
void disabledMeetingTypeCannotBeSelectedForNewMeeting() throws Exception {
    disableItem("meeting_type", "国内业务会议");
    String payload = "{\"title\":\"字典校验会议\",\"roomId\":1,\"organizerId\":2,"
            + "\"startTime\":\"2026-09-10T09:00:00\",\"endTime\":\"2026-09-10T10:00:00\","
            + "\"expectedCount\":20,\"venueType\":\"室内\",\"meetingType\":\"国内业务会议\","
            + "\"budget\":0,\"accommodationFee\":0,\"mealFee\":0,\"venueFee\":0,\"otherFee\":0}";
    expectBadRequest("/api/meetings", payload, "会议类别不在可选字典范围内");
}

@Test
void unknownDocumentTypeAndTravelStaffLevelAreRejected() throws Exception {
    expectBadRequest("/api/documents",
            "{\"title\":\"测试\",\"docType\":\"未知文种\",\"secrecyLevel\":\"公开\",\"content\":\"正文\",\"applicantId\":2}",
            "公文文种不在可选字典范围内");
    expectBadRequest("/api/travels",
            "{\"applicantId\":2,\"destination\":\"上海\",\"startDate\":\"2026-09-10\",\"endDate\":\"2026-09-11\","
            + "\"reason\":\"测试\",\"staffLevel\":\"四类\",\"travelType\":\"教学科研业务\","
            + "\"transport\":\"高铁二等座\",\"budget\":100}",
            "人员类别不在可选字典范围内");
}

@Test
void reportSealAndUploadedMaterialValidateTheirSelectableDictionaries() throws Exception {
    expectBadRequest("/api/reports",
            "{\"title\":\"测试\",\"type\":\"批示\",\"secrecyLevel\":\"内部\",\"content\":\"正文\",\"applicantId\":2}",
            "请示报告类型不在可选字典范围内");
    expectBadRequest("/api/seals/applications",
            "{\"sealId\":2,\"applicantId\":2,\"purpose\":\"测试\",\"matterLevel\":\"特殊事项\"}",
            "事项等级不在可选字典范围内");
}
```

Add a multipart assertion that upload with `secrecyLevel=非密级` is rejected for a draft seal application.

- [ ] **Step 2: Run validation tests to verify RED**

Run:

```powershell
& 'D:\dev\maven\apache-maven-3.9.14\bin\mvn.cmd' -Dtest=DictionaryBusinessValidationTest test
```

Expected: FAIL because current services accept unknown dictionary choices or do not use dictionary enable state.

- [ ] **Step 3: Inject and call `DictionaryService.requireEnabled(...)`**

Modify constructors and create/update entry points:

```java
// DocumentService.create
dictionaryService.requireEnabled("document_type", request.getDocType(), "公文文种");
dictionaryService.requireEnabled("secrecy_level", request.getSecrecyLevel(), "密级");

// SealService.apply
dictionaryService.requireEnabled("matter_level", request.getMatterLevel(), "事项等级");

// MeetingService.create
dictionaryService.requireEnabled("venue_type", request.getVenueType(), "场地类型");
dictionaryService.requireEnabled("meeting_type", request.getMeetingType(), "会议类别");

// ReportService.create
dictionaryService.requireEnabled("report_type", request.getType(), "请示报告类型");
dictionaryService.requireEnabled("secrecy_level", request.getSecrecyLevel(), "密级");

// TravelService.create
dictionaryService.requireEnabled("staff_level", request.getStaffLevel(), "人员类别");
dictionaryService.requireEnabled("travel_type", request.getTravelType(), "出差类型");
dictionaryService.requireEnabled("transport_type", request.getTransport(), "交通工具");

// WorkflowService.addAttachment/uploadAttachment/updateAttachment
dictionaryService.requireEnabled("secrecy_level", secrecyLevelValue, "材料密级");
```

Do not validate status values written by the workflow itself through editable selection rules; flow routes remain defined by `ApprovalFlowConfig`.

- [ ] **Step 4: Run validation and existing regulatory tests to verify GREEN**

Run:

```powershell
& 'D:\dev\maven\apache-maven-3.9.14\bin\mvn.cmd' -Dtest=DictionaryBusinessValidationTest,RegulatoryRulesTest,SealMaterialManagementTest,OfficeManagementSystemIntegrationTest test
```

Expected: PASS, demonstrating the new dictionary guard and preserved regulated workflows.

- [ ] **Step 5: Commit business validation**

```powershell
git add backend/src/main/java/com/university/oms/service backend/src/test/java/com/university/oms/DictionaryBusinessValidationTest.java
git commit -m "feat: validate business choices against dictionaries"
```

### Task 4: Frontend Dictionary Cache and Shared Display Helpers

**Files:**
- Modify: `frontend/package.json`
- Modify: `frontend/package-lock.json`
- Modify: `frontend/src/api.js`
- Create: `frontend/src/utils/dictionaries.js`
- Create: `frontend/src/utils/dictionaries.spec.js`
- Create: `frontend/src/stores/dictionary.js`
- Modify: `frontend/src/views/Login.vue`
- Modify: `frontend/src/App.vue`

- [ ] **Step 1: Add Vitest and write failing helper tests**

Add `"test": "vitest run"` to `scripts` and install `vitest` as a dev dependency using `npm install --save-dev vitest`. Create `frontend/src/utils/dictionaries.spec.js`:

```javascript
import { describe, expect, it } from 'vitest'
import { catalogLabel, catalogOptions, cacheKey, readCachedCatalog } from './dictionaries'

const catalog = {
  version: 'v1',
  dictionaries: {
    business_status: [
      { dictCode: 'draft', dictLabel: '草稿', enabled: true, sortOrder: 1 }
    ],
    meeting_type: [
      { dictCode: 'old', dictLabel: '历史会议', enabled: false, sortOrder: 1 },
      { dictCode: 'new', dictLabel: '新会议', enabled: true, sortOrder: 2 }
    ]
  }
}

it('labels known values and falls back for historical unknown codes', () => {
  expect(catalogLabel(catalog, 'business_status', 'draft')).toBe('草稿')
  expect(catalogLabel(catalog, 'business_status', 'legacy')).toBe('legacy')
})

it('only provides enabled items as selectable options', () => {
  expect(catalogOptions(catalog, 'meeting_type')).toEqual([{ label: '新会议', value: 'new' }])
})

it('loads only valid cached catalogs', () => {
  const storage = { getItem: () => JSON.stringify(catalog) }
  expect(readCachedCatalog(storage).version).toBe('v1')
  expect(cacheKey).toBe('oms_dictionary_catalog')
})
```

- [ ] **Step 2: Run frontend unit tests to verify RED**

Run from `frontend/`:

```powershell
npm run test
```

Expected: FAIL because `src/utils/dictionaries.js` does not exist.

- [ ] **Step 3: Implement helper functions and dictionary API calls**

Create `frontend/src/utils/dictionaries.js`:

```javascript
export const cacheKey = 'oms_dictionary_catalog'

export function catalogLabel(catalog, type, code) {
  const row = catalog?.dictionaries?.[type]?.find((item) => item.dictCode === code)
  return row?.dictLabel || code || '-'
}

export function catalogOptions(catalog, type) {
  return (catalog?.dictionaries?.[type] || [])
    .filter((item) => item.enabled)
    .sort((a, b) => a.sortOrder - b.sortOrder)
    .map((item) => ({ label: item.dictLabel, value: item.dictCode }))
}

export function readCachedCatalog(storage = localStorage) {
  try { return JSON.parse(storage.getItem(cacheKey) || 'null') } catch (_) { return null }
}
```

Extend `frontend/src/api.js`:

```javascript
dictionaries: () => http.get('/dictionaries'),
dictionaryVersion: () => http.get('/dictionaries/version'),
adminDictionaryTypes: () => http.get('/admin/dictionaries/types'),
adminDictionaryItems: (type) => http.get(`/admin/dictionaries/types/${type}/items`),
adminCreateDictionaryType: (data) => http.post('/admin/dictionaries/types', data),
adminUpdateDictionaryType: (type, data) => http.put(`/admin/dictionaries/types/${type}`, data),
adminCreateDictionaryItem: (type, data) => http.post(`/admin/dictionaries/types/${type}/items`, data),
adminUpdateDictionaryItem: (type, code, data) => http.put(`/admin/dictionaries/types/${type}/items/${code}`, data)
```

- [ ] **Step 4: Implement Pinia cache bootstrap**

Create `frontend/src/stores/dictionary.js`:

```javascript
export const useDictionaryStore = defineStore('dictionary', () => {
  const catalog = ref(readCachedCatalog())
  const labelOf = (type, code) => catalogLabel(catalog.value, type, code)
  const optionsOf = (type) => catalogOptions(catalog.value, type)

  async function refresh(force = false) {
    if (!force && catalog.value) {
      const version = await api.dictionaryVersion()
      if (version === catalog.value.version) return
    }
    catalog.value = await api.dictionaries()
    localStorage.setItem(cacheKey, JSON.stringify(catalog.value))
  }
  function clearMemory() { catalog.value = readCachedCatalog() }
  return { catalog, labelOf, optionsOf, refresh, clearMemory }
})
```

After successful login in `Login.vue`, `await dictionaryStore.refresh(true)` before navigation. In `App.vue`, call `dictionaryStore.refresh()` only when `userStore.isLoggedIn` is true so a restored authenticated session updates stale cache without requesting protected data on the login screen.

- [ ] **Step 5: Run tests and build to verify GREEN**

Run:

```powershell
npm run test
npm run build
```

Expected: unit tests PASS and Vite build succeeds.

- [ ] **Step 6: Commit frontend dictionary platform**

```powershell
git add frontend/package.json frontend/package-lock.json frontend/src/api.js frontend/src/utils frontend/src/stores/dictionary.js frontend/src/views/Login.vue frontend/src/App.vue
git commit -m "feat: cache versioned dictionaries in frontend"
```

### Task 5: Replace User-Facing Codes and Add Dictionary Management UI

**Files:**
- Create: `frontend/src/views/DictionaryManage.vue`
- Modify: `frontend/src/router/index.js`
- Modify: `frontend/src/App.vue`
- Modify: `frontend/src/views/Documents.vue`
- Modify: `frontend/src/views/Seals.vue`
- Modify: `frontend/src/views/Meetings.vue`
- Modify: `frontend/src/views/Reports.vue`
- Modify: `frontend/src/views/Travels.vue`
- Modify: `frontend/src/views/Approvals.vue`
- Modify: `frontend/src/views/Dashboard.vue`
- Modify: `frontend/src/views/Statistics.vue`
- Modify: `frontend/src/views/UserManage.vue`
- Modify: `frontend/src/style.css`

- [ ] **Step 1: Add shared dictionary use to each page and switch enumerated inputs**

In each `<script setup>`, obtain:

```javascript
import { useDictionaryStore } from '../stores/dictionary'
const dictionaryStore = useDictionaryStore()
const labelOf = dictionaryStore.labelOf
const optionsOf = dictionaryStore.optionsOf
```

Replace hard-coded select option arrays with:

```vue
<el-option v-for="option in optionsOf('secrecy_level')" :key="option.value"
  :label="option.label" :value="option.value" />
```

Use the matching type per page: `document_type`, `matter_level`, `meeting_type`, `venue_type`, `report_type`, `staff_level`, `travel_type`, and `transport_type`.

- [ ] **Step 2: Render description labels instead of raw values**

Use scoped table cells wherever current templates show codes:

```vue
<el-table-column label="状态" width="128">
  <template #default="{ row }">{{ labelOf('business_status', row.status) }}</template>
</el-table-column>
```

Apply the same form to:

- Documents: `secrecy_level`, `business_status`, `distribution_status`.
- Seals: `matter_level`, `business_status`, `secrecy_level`.
- Meetings: `meeting_type`, `venue_type`, `business_status`.
- Reports: `report_type`, `secrecy_level`, `business_status`.
- Travels: `staff_level`, `travel_type`, `transport_type`, `business_status`.
- Approvals: `biz_type`, `flow_node`, `role_key`, `business_status`.
- Dashboard and Statistics: translate status-distribution keys with `business_status`.
- UserManage: replace local `ROLE_LABELS` with `labelOf('role_key', key)` and source role selection labels from the dictionary catalog.

- [ ] **Step 3: Replace person IDs with name selection where current entities already exist**

In `Seals.vue`, load `api.users()` for authorized transfer/use contexts and replace numeric inputs for `receiverId` and `supervisorId` with `el-select` options labelled by `realName`. In `Documents.vue`, use the same user list to choose the distribution receiver and derive/select the associated department name instead of showing a raw free-input receiver ID.

- [ ] **Step 4: Implement administrator dictionary page**

Create `DictionaryManage.vue` with two un-nested panels: a left table for types and a right table for selected type items. Provide type create/edit dialogs wired to `adminCreateDictionaryType`/`adminUpdateDictionaryType`, allowing ordinary type enable/disable while locking the type code for existing or system records. Provide item create/edit dialogs wired to the matching item APIs; bind item edit payload to:

```javascript
const itemForm = reactive({ dictCode: '', dictLabel: '', sortOrder: 0, enabled: true, remark: '' })
const codeLocked = computed(() => Boolean(editingItem.value))
const enabledLocked = computed(() => Boolean(editingItem.value?.systemItem))
```

For all existing items disable the code control; for protected items additionally disable the enabled control and show:

```vue
<el-alert v-if="editingItem?.systemItem" type="warning" :closable="false"
  title="此编码参与流程或权限规则，仅允许调整显示说明、排序和备注。" />
```

After every successful save, reload admin lists and invoke `await dictionaryStore.refresh(true)` so all displayed labels update immediately.

- [ ] **Step 5: Wire route/navigation and responsive sizing**

In `frontend/src/router/index.js` add:

```javascript
import DictionaryManage from '../views/DictionaryManage.vue'
{ path: '/admin/dictionaries', component: DictionaryManage, meta: { requiresAdmin: true } }
```

In `App.vue`, show an administrator-only `字典管理` menu item. Use existing compact panel/table styling and dialog widths such as `width="min(560px, calc(100vw - 24px))"` so the maintenance workflow remains usable at mobile width.

- [ ] **Step 6: Build before browser QA**

Run:

```powershell
npm run test
npm run build
```

Expected: PASS; no template compilation or helper regression.

- [ ] **Step 7: Browser smoke-check label rendering and maintenance flow**

With backend and frontend running, verify:

1. Log in as `user`; open public business views and confirm no visible `draft`, `pending_dept`, `document`, or `dept_head` raw codes in record tables.
2. Open a new meeting/difference form and confirm enum fields are dropdown options from catalog.
3. Log in as `admin`; open `/admin/dictionaries`, rename an ordinary `meeting_type` label and confirm it updates in its page after cache refresh.
4. Open a protected `business_status` item; confirm code/enable fields are locked and its label can be edited.
5. Refresh the browser; confirm cached labels remain rendered and the version check does not lose choices.

Capture desktop (`1440x1000`) and mobile (`390x900`) screenshots and inspect browser console for errors.

- [ ] **Step 8: Commit user-facing adoption**

```powershell
git add frontend/src/views frontend/src/router/index.js frontend/src/App.vue frontend/src/style.css
git commit -m "feat: present dictionary descriptions across business pages"
```

### Task 6: Documentation, Full Regression, and MySQL Persistence

**Files:**
- Modify: `doc/需求.md`
- Modify: `doc/设计.md`
- Modify: `doc/接口文档.md`
- Modify: `doc/接口补充-流程闭环.md`
- Modify: `doc/数据库初始化.md`
- Modify: `doc/2026-05-26.md`

- [ ] **Step 1: Update delivery documents**

Document these delivered requirements and implementation details:

- Requirement: user-facing dictionary fields display maintained descriptions; enumerated inputs are selected from dictionaries; administrator can maintain ordinary dictionaries.
- Design: `sys_dict_type` / `sys_dict_item`, protected codes, cache version, frontend store, admin page, and business value validation.
- Interfaces: `GET /api/dictionaries`, `GET /api/dictionaries/version`, and `/api/admin/dictionaries/**` CRUD/update routes.
- Database initialization: dictionary tables and repeatable seed behavior; MySQL initialization still runs from `backend/scripts/init-mysql.ps1`.
- Test report: commands, pass/fail counts, API evidence, browser QA, cache behavior, MySQL restart persistence, and any nonblocking environment warning.

- [ ] **Step 2: Run complete backend and frontend automated checks**

Run:

```powershell
cd backend
& 'D:\dev\maven\apache-maven-3.9.14\bin\mvn.cmd' test
cd ..\frontend
npm run test
npm run build
```

Expected: Maven full suite PASS, Vitest PASS, and Vite production build succeeds.

- [ ] **Step 3: Initialize and verify MySQL dictionary persistence**

Run from `backend/`:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\init-mysql.ps1 -Password '123456'
powershell -ExecutionPolicy Bypass -File .\scripts\init-mysql.ps1 -Password '123456'
```

Start/restart backend with the MySQL profile:

```powershell
& 'D:\dev\maven\apache-maven-3.9.14\bin\mvn.cmd' spring-boot:run '-Dspring-boot.run.profiles=mysql'
```

API verification:

- Log in as `admin`.
- Rename or disable an ordinary new dictionary choice through `/api/admin/dictionaries/types/{type}/items/{code}`.
- Retrieve `/api/dictionaries` and record the new version and updated item.
- Restart MySQL-profile backend and retrieve the catalog again.

SQL verification:

```sql
SELECT dict_type, dict_code, dict_label, enabled
FROM sys_dict_item
WHERE dict_type IN ('business_status', 'meeting_type')
ORDER BY dict_type, sort_order;
```

Expected: seed data exists, ordinary maintenance persists after service restart, and running `init-mysql.ps1` a second time does not erase permitted ordinary administration state.

- [ ] **Step 4: Perform final API and browser smoke tests**

Verify public catalog, protected maintenance rejection, disabled-option business rejection, translated record tables, dropdown options, admin dictionary maintenance, and cache refresh as described in Tasks 2, 3, and 5. Update `doc/2026-05-26.md` with concrete result IDs/screens/commands and absolute date.

- [ ] **Step 5: Review diffs and commit delivery evidence**

Run:

```powershell
git diff --check
git status --short
git diff --stat
git add doc backend frontend docs/superpowers/plans/2026-05-26-dictionary-center.md
git commit -m "docs: record dictionary center verification"
```

Expected: `git diff --check` returns no whitespace errors and the commit contains only dictionary-center implementation, tests, and aligned delivery documentation.

- [ ] **Step 6: Push verified branch**

Run:

```powershell
git push origin codex/compliance-remediation-exec
```

Expected: GitHub branch `codex/compliance-remediation-exec` contains the dictionary center implementation and verification report.
