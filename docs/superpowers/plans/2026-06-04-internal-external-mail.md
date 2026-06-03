# Internal And External Mail Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a mail center that saves internal mail, supports TO/CC recipients selected from an organization tree, sends system notifications, and optionally sends real email to each user's saved email address.

**Architecture:** Add focused mail models and services beside the existing workflow/notification infrastructure. Persist mail message and per-recipient delivery/read state in both memory and MySQL modes, use `WorkflowService.notifyUser` for in-app notifications, and isolate SMTP behavior behind `EmailSenderService` so tests can run without real mail credentials.

**Tech Stack:** Spring Boot 2.5.6, Java 8, Spring Validation, Spring Mail, JdbcTemplate, MockMvc, Vue 3, Element Plus, Vitest.

---

## File Structure

- Modify: `backend/pom.xml` to add `spring-boot-starter-mail`.
- Modify: `backend/src/main/resources/application.properties` to add disabled-by-default external mail settings.
- Modify: `backend/sql/schema.sql` to create mail tables and add migration-safe `email` handling.
- Modify: `backend/sql/data.sql` so seeded users have email addresses.
- Modify: `backend/src/main/java/com/university/oms/model/User.java` to expose email.
- Modify: `backend/src/main/java/com/university/oms/dto/CreateUserRequest.java` and `backend/src/main/java/com/university/oms/dto/UpdateUserRequest.java` for email validation.
- Modify: `backend/src/main/java/com/university/oms/service/UserManageService.java`, `backend/src/main/java/com/university/oms/repository/JdbcDataPersistence.java`, `backend/src/main/java/com/university/oms/repository/MysqlDataLoader.java`, and `backend/src/main/java/com/university/oms/repository/InMemoryDatabase.java` to persist and seed email.
- Create: `backend/src/main/java/com/university/oms/model/MailMessage.java`.
- Create: `backend/src/main/java/com/university/oms/model/MailRecipient.java`.
- Create: `backend/src/main/java/com/university/oms/dto/MailSendRequest.java`.
- Create: `backend/src/main/java/com/university/oms/dto/MailDetailResponse.java`.
- Create: `backend/src/main/java/com/university/oms/dto/MailRecipientResponse.java`.
- Create: `backend/src/main/java/com/university/oms/dto/OrgTreeNode.java`.
- Modify: `backend/src/main/java/com/university/oms/repository/InMemoryDatabase.java` and `backend/src/main/java/com/university/oms/repository/DataPersistence.java` for mail collections and save methods.
- Modify: `backend/src/main/java/com/university/oms/repository/JdbcDataPersistence.java`, `backend/src/main/java/com/university/oms/repository/MysqlDataLoader.java`, and `backend/src/main/java/com/university/oms/repository/NoopDataPersistence.java` for mail persistence.
- Create: `backend/src/main/java/com/university/oms/service/OrgTreeService.java`.
- Create: `backend/src/main/java/com/university/oms/controller/OrgController.java`.
- Create: `backend/src/main/java/com/university/oms/service/EmailSenderService.java`.
- Create: `backend/src/main/java/com/university/oms/service/MailService.java`.
- Create: `backend/src/main/java/com/university/oms/controller/MailController.java`.
- Create: `backend/src/test/java/com/university/oms/MailIntegrationTest.java`.
- Modify: `backend/src/test/java/com/university/oms/MysqlSchemaContractTest.java`.
- Modify: `frontend/src/api.js` with mail and organization endpoints.
- Modify: `frontend/src/router/index.js` and `frontend/src/utils/navigation.js` to add the mail route/menu item.
- Create: `frontend/src/components/OrgUserTreeSelect.vue`.
- Create: `frontend/src/views/Mails.vue`.
- Create: `frontend/src/components/OrgUserTreeSelect.spec.js`.
- Create: `frontend/src/views/Mails.spec.js`.

---

### Task 1: Require And Persist User Email

**Files:**
- Modify: `backend/src/main/java/com/university/oms/model/User.java`
- Modify: `backend/src/main/java/com/university/oms/dto/CreateUserRequest.java`
- Modify: `backend/src/main/java/com/university/oms/dto/UpdateUserRequest.java`
- Modify: `backend/src/main/java/com/university/oms/service/UserManageService.java`
- Modify: `backend/src/main/java/com/university/oms/repository/JdbcDataPersistence.java`
- Modify: `backend/src/main/java/com/university/oms/repository/MysqlDataLoader.java`
- Modify: `backend/src/main/java/com/university/oms/repository/InMemoryDatabase.java`
- Modify: `backend/sql/data.sql`
- Test: `backend/src/test/java/com/university/oms/MailIntegrationTest.java`

- [ ] **Step 1: Write failing user email validation tests**

Create `backend/src/test/java/com/university/oms/MailIntegrationTest.java` with this initial content:

```java
package com.university.oms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.university.oms.repository.InMemoryDatabase;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MailIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InMemoryDatabase db;

    @Test
    void adminCreateUserRequiresEmail() throws Exception {
        String adminToken = login("admin");

        mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"noemail\",\"password\":\"123456\",\"realName\":\"No Email\",\"deptId\":4,\"roleKeys\":\"office_user\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void adminCreateUserRejectsInvalidEmail() throws Exception {
        String adminToken = login("admin");

        mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"bademail\",\"password\":\"123456\",\"realName\":\"Bad Email\",\"email\":\"bad-email\",\"deptId\":4,\"roleKeys\":\"office_user\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void adminCreateUserStoresEmail() throws Exception {
        String adminToken = login("admin");

        JsonNode response = postJson("/api/admin/users",
                "{\"username\":\"mailuser\",\"password\":\"123456\",\"realName\":\"Mail User\",\"email\":\"mailuser@example.com\",\"deptId\":4,\"roleKeys\":\"office_user\"}",
                adminToken);

        long id = response.get("data").get("id").asLong();
        assertEquals("mailuser@example.com", db.users().get(id).getEmail());

        mockMvc.perform(get("/api/admin/users/" + id).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("mailuser@example.com"));
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
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd backend; mvn -Dtest=MailIntegrationTest test`

Expected: compile failure because `User#getEmail()` does not exist, or failing assertions because email is not persisted.

- [ ] **Step 3: Add email to user model and DTOs**

In `User.java`, add:

```java
private String email;

public String getEmail() {
    return email;
}

public void setEmail(String email) {
    this.email = email;
}
```

In `CreateUserRequest.java`, add imports and field:

```java
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@NotBlank
@Email
private String email;

public String getEmail() { return email; }
public void setEmail(String email) { this.email = email; }
```

In `UpdateUserRequest.java`, add:

```java
import javax.validation.constraints.Email;

@Email
private String email;

public String getEmail() { return email; }
public void setEmail(String email) { this.email = email; }
```

- [ ] **Step 4: Save email in user service and persistence**

In `UserManageService.createUser`, after `setRealName`, add:

```java
user.setEmail(request.getEmail());
```

In `UserManageService.updateUser`, add:

```java
if (request.getEmail() != null) user.setEmail(request.getEmail());
```

In `JdbcDataPersistence.saveUser`, replace the two `null` values for email/phone with:

```java
u.getEmail(), null
```

In `MysqlDataLoader.loadUsers`, select and set email:

```java
List<User> users = jdbcTemplate.query("SELECT u.id, u.username, u.password, u.real_name, u.dept_id, u.email, d.dept_name " +
        "FROM sys_user u LEFT JOIN sys_dept d ON u.dept_id=d.id", (rs, rowNum) -> {
    User user = new User();
    db.fill(user, rs.getLong("id"));
    user.setUsername(rs.getString("username"));
    user.setPassword(rs.getString("password"));
    user.setRealName(rs.getString("real_name"));
    user.setEmail(rs.getString("email"));
    user.setDeptId(rs.getLong("dept_id"));
    user.setDeptName(rs.getString("dept_name"));
    user.setRoleKeys(new LinkedHashSet<String>());
    return user;
});
```

In `InMemoryDatabase.addUser`, assign deterministic test emails:

```java
user.setEmail(username + "@example.com");
```

In `backend/sql/data.sql`, ensure every `sys_user` insert supplies a non-empty email address.

- [ ] **Step 5: Run the user email tests**

Run: `cd backend; mvn -Dtest=MailIntegrationTest test`

Expected: all three tests pass.

- [ ] **Step 6: Commit**

```bash
git add backend/src/main/java/com/university/oms/model/User.java backend/src/main/java/com/university/oms/dto/CreateUserRequest.java backend/src/main/java/com/university/oms/dto/UpdateUserRequest.java backend/src/main/java/com/university/oms/service/UserManageService.java backend/src/main/java/com/university/oms/repository/JdbcDataPersistence.java backend/src/main/java/com/university/oms/repository/MysqlDataLoader.java backend/src/main/java/com/university/oms/repository/InMemoryDatabase.java backend/sql/data.sql backend/src/test/java/com/university/oms/MailIntegrationTest.java
git commit -m "feat: require user email"
```

---

### Task 2: Add Mail Tables, Models, And Persistence

**Files:**
- Modify: `backend/sql/schema.sql`
- Create: `backend/src/main/java/com/university/oms/model/MailMessage.java`
- Create: `backend/src/main/java/com/university/oms/model/MailRecipient.java`
- Modify: `backend/src/main/java/com/university/oms/repository/InMemoryDatabase.java`
- Modify: `backend/src/main/java/com/university/oms/repository/DataPersistence.java`
- Modify: `backend/src/main/java/com/university/oms/repository/NoopDataPersistence.java`
- Modify: `backend/src/main/java/com/university/oms/repository/JdbcDataPersistence.java`
- Modify: `backend/src/main/java/com/university/oms/repository/MysqlDataLoader.java`
- Modify: `backend/src/test/java/com/university/oms/MysqlSchemaContractTest.java`
- Test: `backend/src/test/java/com/university/oms/MailIntegrationTest.java`

- [ ] **Step 1: Write schema contract tests**

In `MysqlSchemaContractTest.java`, add assertions:

```java
assertTrue(schema.contains("CREATE TABLE IF NOT EXISTS oa_mail_message"));
assertTrue(schema.contains("CREATE TABLE IF NOT EXISTS oa_mail_recipient"));
assertTrue(schema.contains("email_status VARCHAR(20) DEFAULT 'pending'"));
assertTrue(schema.contains("INDEX idx_mail_recipient_user"));
```

- [ ] **Step 2: Run schema contract test to verify it fails**

Run: `cd backend; mvn -Dtest=MysqlSchemaContractTest test`

Expected: failure because the mail table DDL does not exist.

- [ ] **Step 3: Add schema**

In `backend/sql/schema.sql`, after `sys_notification`, add:

```sql
CREATE TABLE IF NOT EXISTS oa_mail_message (
  id BIGINT PRIMARY KEY,
  sender_id BIGINT NOT NULL,
  subject VARCHAR(255) NOT NULL,
  content LONGTEXT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_mail_sender (sender_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS oa_mail_recipient (
  id BIGINT PRIMARY KEY,
  mail_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  recipient_type VARCHAR(10) NOT NULL,
  read_status TINYINT DEFAULT 0,
  read_at DATETIME,
  email_status VARCHAR(20) DEFAULT 'pending',
  email_error VARCHAR(1000),
  email_sent_at DATETIME,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_mail_user_type (mail_id, user_id, recipient_type),
  INDEX idx_mail_recipient_user (user_id, read_status, created_at),
  INDEX idx_mail_recipient_mail (mail_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

Also add migration-safe calls near the existing `add_column_if_missing` section:

```sql
CALL add_column_if_missing('sys_user', 'email', 'VARCHAR(100)');
```

- [ ] **Step 4: Create mail model classes**

Create `MailMessage.java`:

```java
package com.university.oms.model;

public class MailMessage extends BaseEntity {
    private Long senderId;
    private String subject;
    private String content;

    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
```

Create `MailRecipient.java`:

```java
package com.university.oms.model;

import java.time.LocalDateTime;

public class MailRecipient extends BaseEntity {
    private Long mailId;
    private Long userId;
    private String recipientType;
    private boolean readStatus;
    private LocalDateTime readAt;
    private String emailStatus = "pending";
    private String emailError;
    private LocalDateTime emailSentAt;

    public Long getMailId() { return mailId; }
    public void setMailId(Long mailId) { this.mailId = mailId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getRecipientType() { return recipientType; }
    public void setRecipientType(String recipientType) { this.recipientType = recipientType; }
    public boolean isReadStatus() { return readStatus; }
    public void setReadStatus(boolean readStatus) { this.readStatus = readStatus; }
    public LocalDateTime getReadAt() { return readAt; }
    public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }
    public String getEmailStatus() { return emailStatus; }
    public void setEmailStatus(String emailStatus) { this.emailStatus = emailStatus; }
    public String getEmailError() { return emailError; }
    public void setEmailError(String emailError) { this.emailError = emailError; }
    public LocalDateTime getEmailSentAt() { return emailSentAt; }
    public void setEmailSentAt(LocalDateTime emailSentAt) { this.emailSentAt = emailSentAt; }
}
```

- [ ] **Step 5: Add repository storage hooks**

In `InMemoryDatabase`, add:

```java
private final Map<Long, MailMessage> mailMessages = new ConcurrentHashMap<Long, MailMessage>();
private final List<MailRecipient> mailRecipients = Collections.synchronizedList(new ArrayList<MailRecipient>());

public Map<Long, MailMessage> mailMessages() { return mailMessages; }
public List<MailRecipient> mailRecipients() { return mailRecipients; }
```

Clear these collections in `MysqlDataLoader.load()`:

```java
db.mailMessages().clear();
db.mailRecipients().clear();
```

In `DataPersistence`, add:

```java
void saveMailMessage(MailMessage message);
void saveMailRecipient(MailRecipient recipient);
```

In `NoopDataPersistence`, add empty implementations:

```java
public void saveMailMessage(MailMessage message) { }
public void saveMailRecipient(MailRecipient recipient) { }
```

- [ ] **Step 6: Add JDBC persistence and loading**

In `JdbcDataPersistence`, add:

```java
@Override
public void saveMailMessage(MailMessage m) {
    jdbcTemplate.update("REPLACE INTO oa_mail_message (id, sender_id, subject, content, created_at, updated_at) VALUES (?,?,?,?,?,?)",
            m.getId(), m.getSenderId(), m.getSubject(), m.getContent(), m.getCreatedAt(), m.getUpdatedAt());
}

@Override
public void saveMailRecipient(MailRecipient r) {
    jdbcTemplate.update("REPLACE INTO oa_mail_recipient " +
                    "(id, mail_id, user_id, recipient_type, read_status, read_at, email_status, email_error, email_sent_at, created_at, updated_at) " +
                    "VALUES (?,?,?,?,?,?,?,?,?,?,?)",
            r.getId(), r.getMailId(), r.getUserId(), r.getRecipientType(), r.isReadStatus() ? 1 : 0,
            r.getReadAt(), r.getEmailStatus(), r.getEmailError(), r.getEmailSentAt(), r.getCreatedAt(), r.getUpdatedAt());
}
```

In `MysqlDataLoader`, add `loadMailMessages()` and `loadMailRecipients()` and call them after `loadNotifications()`:

```java
private void loadMailMessages() {
    jdbcTemplate.query("SELECT * FROM oa_mail_message", rs -> {
        MailMessage m = new MailMessage();
        m.setId(rs.getLong("id"));
        m.setSenderId(rs.getLong("sender_id"));
        m.setSubject(rs.getString("subject"));
        m.setContent(rs.getString("content"));
        m.setCreatedAt(toLocalDateTime(rs, "created_at"));
        m.setUpdatedAt(toLocalDateTime(rs, "updated_at"));
        db.mailMessages().put(m.getId(), m);
    });
}

private void loadMailRecipients() {
    jdbcTemplate.query("SELECT * FROM oa_mail_recipient", rs -> {
        MailRecipient r = new MailRecipient();
        r.setId(rs.getLong("id"));
        r.setMailId(rs.getLong("mail_id"));
        r.setUserId(rs.getLong("user_id"));
        r.setRecipientType(rs.getString("recipient_type"));
        r.setReadStatus(rs.getInt("read_status") == 1);
        r.setReadAt(toLocalDateTime(rs, "read_at"));
        r.setEmailStatus(rs.getString("email_status"));
        r.setEmailError(rs.getString("email_error"));
        r.setEmailSentAt(toLocalDateTime(rs, "email_sent_at"));
        r.setCreatedAt(toLocalDateTime(rs, "created_at"));
        r.setUpdatedAt(toLocalDateTime(rs, "updated_at"));
        db.mailRecipients().add(r);
    });
}
```

Extend `maxId()` with:

```java
"COALESCE((SELECT MAX(id) FROM oa_mail_message),0)," +
"COALESCE((SELECT MAX(id) FROM oa_mail_recipient),0)," +
```

- [ ] **Step 7: Run backend schema tests**

Run: `cd backend; mvn -Dtest=MysqlSchemaContractTest test`

Expected: PASS.

- [ ] **Step 8: Commit**

```bash
git add backend/sql/schema.sql backend/src/main/java/com/university/oms/model/MailMessage.java backend/src/main/java/com/university/oms/model/MailRecipient.java backend/src/main/java/com/university/oms/repository/InMemoryDatabase.java backend/src/main/java/com/university/oms/repository/DataPersistence.java backend/src/main/java/com/university/oms/repository/NoopDataPersistence.java backend/src/main/java/com/university/oms/repository/JdbcDataPersistence.java backend/src/main/java/com/university/oms/repository/MysqlDataLoader.java backend/src/test/java/com/university/oms/MysqlSchemaContractTest.java
git commit -m "feat: add mail persistence"
```

---

### Task 3: Add Organization Tree API

**Files:**
- Create: `backend/src/main/java/com/university/oms/dto/OrgTreeNode.java`
- Create: `backend/src/main/java/com/university/oms/service/OrgTreeService.java`
- Create: `backend/src/main/java/com/university/oms/controller/OrgController.java`
- Test: `backend/src/test/java/com/university/oms/MailIntegrationTest.java`

- [ ] **Step 1: Write failing organization tree test**

Append to `MailIntegrationTest`:

```java
@Test
void organizationTreeContainsDepartmentsAndUsers() throws Exception {
    String token = login("user");

    String body = mockMvc.perform(get("/api/org/tree").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

    JsonNode data = objectMapper.readTree(body).get("data");
    assertTrue(containsNode(data, "dept"));
    assertTrue(containsNode(data, "user"));
}

private boolean containsNode(JsonNode nodes, String type) {
    for (JsonNode node : nodes) {
        if (type.equals(node.get("type").asText())) {
            return true;
        }
        JsonNode children = node.get("children");
        if (children != null && children.isArray() && containsNode(children, type)) {
            return true;
        }
    }
    return false;
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd backend; mvn -Dtest=MailIntegrationTest#organizationTreeContainsDepartmentsAndUsers test`

Expected: 404 because `/api/org/tree` does not exist.

- [ ] **Step 3: Add organization DTO**

Create `OrgTreeNode.java`:

```java
package com.university.oms.dto;

import java.util.ArrayList;
import java.util.List;

public class OrgTreeNode {
    private String id;
    private String label;
    private String type;
    private Long deptId;
    private Long userId;
    private String email;
    private List<OrgTreeNode> children = new ArrayList<OrgTreeNode>();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public List<OrgTreeNode> getChildren() { return children; }
    public void setChildren(List<OrgTreeNode> children) { this.children = children; }
}
```

- [ ] **Step 4: Add organization tree service and controller**

Create `OrgTreeService.java`:

```java
package com.university.oms.service;

import com.university.oms.dto.OrgTreeNode;
import com.university.oms.model.Department;
import com.university.oms.model.User;
import com.university.oms.repository.InMemoryDatabase;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrgTreeService {
    private final InMemoryDatabase db;

    public OrgTreeService(InMemoryDatabase db) {
        this.db = db;
    }

    public List<OrgTreeNode> tree() {
        Map<Long, OrgTreeNode> deptNodes = new LinkedHashMap<Long, OrgTreeNode>();
        List<OrgTreeNode> roots = new ArrayList<OrgTreeNode>();
        for (Department dept : db.departments().values()) {
            OrgTreeNode node = new OrgTreeNode();
            node.setId("dept-" + dept.getId());
            node.setLabel(dept.getDeptName());
            node.setType("dept");
            node.setDeptId(dept.getId());
            deptNodes.put(dept.getId(), node);
        }
        for (Department dept : db.departments().values()) {
            OrgTreeNode node = deptNodes.get(dept.getId());
            Long parentId = dept.getParentId();
            OrgTreeNode parent = parentId == null ? null : deptNodes.get(parentId);
            if (parent == null || parentId == 0L) {
                roots.add(node);
            } else {
                parent.getChildren().add(node);
            }
        }
        for (User user : db.users().values()) {
            OrgTreeNode userNode = new OrgTreeNode();
            userNode.setId("user-" + user.getId());
            userNode.setLabel(user.getRealName());
            userNode.setType("user");
            userNode.setUserId(user.getId());
            userNode.setDeptId(user.getDeptId());
            userNode.setEmail(user.getEmail());
            OrgTreeNode dept = deptNodes.get(user.getDeptId());
            if (dept == null) {
                roots.add(userNode);
            } else {
                dept.getChildren().add(userNode);
            }
        }
        return roots;
    }
}
```

Create `OrgController.java`:

```java
package com.university.oms.controller;

import com.university.oms.common.ApiResponse;
import com.university.oms.dto.OrgTreeNode;
import com.university.oms.service.OrgTreeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/org")
public class OrgController {
    private final OrgTreeService service;

    public OrgController(OrgTreeService service) {
        this.service = service;
    }

    @GetMapping("/tree")
    public ApiResponse<List<OrgTreeNode>> tree() {
        return ApiResponse.ok(service.tree());
    }
}
```

- [ ] **Step 5: Run organization tree test**

Run: `cd backend; mvn -Dtest=MailIntegrationTest#organizationTreeContainsDepartmentsAndUsers test`

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add backend/src/main/java/com/university/oms/dto/OrgTreeNode.java backend/src/main/java/com/university/oms/service/OrgTreeService.java backend/src/main/java/com/university/oms/controller/OrgController.java backend/src/test/java/com/university/oms/MailIntegrationTest.java
git commit -m "feat: expose organization tree"
```

---

### Task 4: Add Internal Mail Send, Inbox, Sent, Detail, And Read State

**Files:**
- Create: `backend/src/main/java/com/university/oms/dto/MailSendRequest.java`
- Create: `backend/src/main/java/com/university/oms/dto/MailRecipientResponse.java`
- Create: `backend/src/main/java/com/university/oms/dto/MailDetailResponse.java`
- Create: `backend/src/main/java/com/university/oms/service/MailService.java`
- Create: `backend/src/main/java/com/university/oms/controller/MailController.java`
- Test: `backend/src/test/java/com/university/oms/MailIntegrationTest.java`

- [ ] **Step 1: Write failing mail flow tests**

Append to `MailIntegrationTest`:

```java
@Test
void sendingMailCreatesInboxSentItemsAndNotification() throws Exception {
    String userToken = login("user");

    JsonNode sent = postJson("/api/mails",
            "{\"subject\":\"Weekly Plan\",\"content\":\"Please read this mail.\",\"toUserIds\":[3],\"ccUserIds\":[5]}",
            userToken).get("data");

    assertEquals("Weekly Plan", sent.get("subject").asText());
    assertEquals(2, sent.get("recipients").size());

    String headToken = login("head");
    JsonNode inbox = getJson("/api/mails/inbox", headToken).get("data");
    assertTrue(containsMailSubject(inbox, "Weekly Plan"));

    JsonNode userSent = getJson("/api/mails/sent", userToken).get("data");
    assertTrue(containsMailSubject(userSent, "Weekly Plan"));

    JsonNode notifications = getJson("/api/workflow/notifications", headToken).get("data");
    assertTrue(containsNotificationTitle(notifications, "新邮件"));
}

@Test
void mailDetailIsOnlyVisibleToSenderOrRecipient() throws Exception {
    String userToken = login("user");
    JsonNode mail = postJson("/api/mails",
            "{\"subject\":\"Private Mail\",\"content\":\"Visible to sender and receiver.\",\"toUserIds\":[3],\"ccUserIds\":[]}",
            userToken).get("data");
    long mailId = mail.get("id").asLong();

    getJson("/api/mails/" + mailId, userToken);
    getJson("/api/mails/" + mailId, login("head"));

    mockMvc.perform(get("/api/mails/" + mailId).header("Authorization", "Bearer " + login("finance")))
            .andExpect(status().isForbidden());
}

@Test
void recipientCanMarkMailRead() throws Exception {
    String userToken = login("user");
    JsonNode mail = postJson("/api/mails",
            "{\"subject\":\"Read Mail\",\"content\":\"Mark this as read.\",\"toUserIds\":[3],\"ccUserIds\":[]}",
            userToken).get("data");
    long mailId = mail.get("id").asLong();
    String headToken = login("head");

    postJson("/api/mails/" + mailId + "/read", "{}", headToken);

    JsonNode detail = getJson("/api/mails/" + mailId, headToken).get("data");
    assertTrue(detail.get("currentUserRead").asBoolean());
}

private JsonNode getJson(String url, String token) throws Exception {
    String body = mockMvc.perform(get(url).header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
    return objectMapper.readTree(body);
}

private boolean containsMailSubject(JsonNode mails, String subject) {
    for (JsonNode mail : mails) {
        if (subject.equals(mail.get("subject").asText())) {
            return true;
        }
    }
    return false;
}

private boolean containsNotificationTitle(JsonNode notifications, String titlePart) {
    for (JsonNode notification : notifications) {
        if (notification.get("title").asText().contains(titlePart)) {
            return true;
        }
    }
    return false;
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `cd backend; mvn -Dtest=MailIntegrationTest test`

Expected: 404 for `/api/mails`.

- [ ] **Step 3: Add mail request and response DTOs**

Create `MailSendRequest.java`:

```java
package com.university.oms.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

public class MailSendRequest {
    @NotBlank
    @Size(max = 255)
    private String subject;
    @NotBlank
    private String content;
    private List<Long> toUserIds = new ArrayList<Long>();
    private List<Long> ccUserIds = new ArrayList<Long>();

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public List<Long> getToUserIds() { return toUserIds; }
    public void setToUserIds(List<Long> toUserIds) { this.toUserIds = toUserIds; }
    public List<Long> getCcUserIds() { return ccUserIds; }
    public void setCcUserIds(List<Long> ccUserIds) { this.ccUserIds = ccUserIds; }
}
```

Create `MailRecipientResponse.java`:

```java
package com.university.oms.dto;

import java.time.LocalDateTime;

public class MailRecipientResponse {
    private Long userId;
    private String realName;
    private String deptName;
    private String recipientType;
    private boolean readStatus;
    private String emailStatus;
    private String emailError;
    private LocalDateTime emailSentAt;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    public String getRecipientType() { return recipientType; }
    public void setRecipientType(String recipientType) { this.recipientType = recipientType; }
    public boolean isReadStatus() { return readStatus; }
    public void setReadStatus(boolean readStatus) { this.readStatus = readStatus; }
    public String getEmailStatus() { return emailStatus; }
    public void setEmailStatus(String emailStatus) { this.emailStatus = emailStatus; }
    public String getEmailError() { return emailError; }
    public void setEmailError(String emailError) { this.emailError = emailError; }
    public LocalDateTime getEmailSentAt() { return emailSentAt; }
    public void setEmailSentAt(LocalDateTime emailSentAt) { this.emailSentAt = emailSentAt; }
}
```

Create `MailDetailResponse.java`:

```java
package com.university.oms.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MailDetailResponse {
    private Long id;
    private Long senderId;
    private String senderName;
    private String subject;
    private String content;
    private LocalDateTime createdAt;
    private String currentUserRecipientType;
    private boolean currentUserRead;
    private List<MailRecipientResponse> recipients = new ArrayList<MailRecipientResponse>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getCurrentUserRecipientType() { return currentUserRecipientType; }
    public void setCurrentUserRecipientType(String currentUserRecipientType) { this.currentUserRecipientType = currentUserRecipientType; }
    public boolean isCurrentUserRead() { return currentUserRead; }
    public void setCurrentUserRead(boolean currentUserRead) { this.currentUserRead = currentUserRead; }
    public List<MailRecipientResponse> getRecipients() { return recipients; }
    public void setRecipients(List<MailRecipientResponse> recipients) { this.recipients = recipients; }
}
```

- [ ] **Step 4: Add internal mail service**

Create `MailService.java` with send/list/detail/read methods. Use these rules exactly:

```java
private List<Long> unique(List<Long> values) {
    LinkedHashSet<Long> ids = new LinkedHashSet<Long>();
    if (values != null) {
        ids.addAll(values);
    }
    return new ArrayList<Long>(ids);
}
```

When merging recipients:

```java
List<Long> toIds = unique(request.getToUserIds());
List<Long> ccIds = unique(request.getCcUserIds());
ccIds.removeAll(toIds);
if (toIds.isEmpty()) {
    throw new BusinessException("收件人不能为空");
}
```

When notifying:

```java
workflowService.notifyUser(userId, "新邮件：" + message.getSubject(),
        sender.getRealName() + " 给您发送了一封邮件", "mail", message.getId());
```

When converting detail:

```java
response.setCurrentUserRecipientType(currentRecipient == null ? "sender" : currentRecipient.getRecipientType());
response.setCurrentUserRead(currentRecipient == null || currentRecipient.isReadStatus());
```

- [ ] **Step 5: Add mail controller**

Create `MailController.java`:

```java
package com.university.oms.controller;

import com.university.oms.common.ApiResponse;
import com.university.oms.dto.MailDetailResponse;
import com.university.oms.dto.MailSendRequest;
import com.university.oms.service.MailService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/mails")
public class MailController {
    private final MailService service;

    public MailController(MailService service) {
        this.service = service;
    }

    @PostMapping
    public ApiResponse<MailDetailResponse> send(@Valid @RequestBody MailSendRequest request) {
        return ApiResponse.ok(service.send(request));
    }

    @GetMapping("/inbox")
    public ApiResponse<List<MailDetailResponse>> inbox() {
        return ApiResponse.ok(service.inbox());
    }

    @GetMapping("/sent")
    public ApiResponse<List<MailDetailResponse>> sent() {
        return ApiResponse.ok(service.sent());
    }

    @GetMapping("/{id}")
    public ApiResponse<MailDetailResponse> detail(@PathVariable Long id) {
        return ApiResponse.ok(service.detail(id));
    }

    @PostMapping("/{id}/read")
    public ApiResponse<MailDetailResponse> markRead(@PathVariable Long id) {
        return ApiResponse.ok(service.markRead(id));
    }
}
```

- [ ] **Step 6: Run mail flow tests**

Run: `cd backend; mvn -Dtest=MailIntegrationTest test`

Expected: PASS for user email, org tree, mail send, visibility, and read tests.

- [ ] **Step 7: Commit**

```bash
git add backend/src/main/java/com/university/oms/dto/MailSendRequest.java backend/src/main/java/com/university/oms/dto/MailRecipientResponse.java backend/src/main/java/com/university/oms/dto/MailDetailResponse.java backend/src/main/java/com/university/oms/service/MailService.java backend/src/main/java/com/university/oms/controller/MailController.java backend/src/test/java/com/university/oms/MailIntegrationTest.java
git commit -m "feat: add internal mail center APIs"
```

---

### Task 5: Add Real Email Sending And Retry State

**Files:**
- Modify: `backend/pom.xml`
- Modify: `backend/src/main/resources/application.properties`
- Create: `backend/src/main/java/com/university/oms/service/EmailSenderService.java`
- Modify: `backend/src/main/java/com/university/oms/service/MailService.java`
- Modify: `backend/src/main/java/com/university/oms/controller/MailController.java`
- Test: `backend/src/test/java/com/university/oms/MailIntegrationTest.java`

- [ ] **Step 1: Write failing external email state tests**

Append to `MailIntegrationTest`:

```java
@Test
void externalEmailDisabledMarksRecipientsSkipped() throws Exception {
    String userToken = login("user");

    JsonNode mail = postJson("/api/mails",
            "{\"subject\":\"External Disabled\",\"content\":\"External mail disabled in tests.\",\"toUserIds\":[3],\"ccUserIds\":[]}",
            userToken).get("data");

    assertEquals("skipped", mail.get("recipients").get(0).get("emailStatus").asText());
}

@Test
void senderCanRetryFailedOrSkippedEmail() throws Exception {
    String userToken = login("user");
    JsonNode mail = postJson("/api/mails",
            "{\"subject\":\"Retry Mail\",\"content\":\"Retry this mail.\",\"toUserIds\":[3],\"ccUserIds\":[]}",
            userToken).get("data");
    long mailId = mail.get("id").asLong();

    JsonNode retried = postJson("/api/mails/" + mailId + "/retry-email", "{}", userToken).get("data");

    assertEquals("skipped", retried.get("recipients").get(0).get("emailStatus").asText());
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `cd backend; mvn -Dtest=MailIntegrationTest#externalEmailDisabledMarksRecipientsSkipped,MailIntegrationTest#senderCanRetryFailedOrSkippedEmail test`

Expected: first test returns `pending`, second returns 404.

- [ ] **Step 3: Add Spring Mail dependency and config**

In `backend/pom.xml`, add:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```

In `application.properties`, add disabled defaults:

```properties
oms.mail.external-enabled=false
oms.mail.from-name=高校办公管理系统
spring.mail.host=
spring.mail.port=587
spring.mail.username=
spring.mail.password=
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

- [ ] **Step 4: Add email sender service**

Create `EmailSenderService.java`:

```java
package com.university.oms.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnClass(JavaMailSender.class)
public class EmailSenderService {
    private final JavaMailSender mailSender;
    private final boolean enabled;
    private final String username;
    private final String fromName;

    public EmailSenderService(JavaMailSender mailSender,
                              @Value("${oms.mail.external-enabled:false}") boolean enabled,
                              @Value("${spring.mail.username:}") String username,
                              @Value("${oms.mail.from-name:高校办公管理系统}") String fromName) {
        this.mailSender = mailSender;
        this.enabled = enabled;
        this.username = username;
        this.fromName = fromName;
    }

    public boolean isEnabled() {
        return enabled && username != null && !username.trim().isEmpty();
    }

    public void sendMail(String toEmail, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(username);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(content);
        mailSender.send(message);
    }

    public String getFromName() {
        return fromName;
    }
}
```

- [ ] **Step 5: Integrate email status in `MailService`**

Inject `EmailSenderService`. After each recipient is created, call a helper:

```java
private void deliverExternalEmail(MailMessage message, MailRecipient recipient, User sender, User receiver) {
    if (!emailSenderService.isEnabled()) {
        recipient.setEmailStatus("skipped");
        recipient.setEmailError("外部邮件未启用");
        persistence.saveMailRecipient(recipient);
        return;
    }
    if (receiver.getEmail() == null || receiver.getEmail().trim().isEmpty()) {
        recipient.setEmailStatus("skipped");
        recipient.setEmailError("用户邮箱为空");
        persistence.saveMailRecipient(recipient);
        return;
    }
    try {
        String body = "发件人：" + sender.getRealName() + "\n\n" + message.getContent()
                + "\n\n请登录高校办公管理系统查看站内信和处理后续事项。";
        emailSenderService.sendMail(receiver.getEmail(), message.getSubject(), body);
        recipient.setEmailStatus("sent");
        recipient.setEmailError(null);
        recipient.setEmailSentAt(LocalDateTime.now());
    } catch (RuntimeException ex) {
        recipient.setEmailStatus("failed");
        recipient.setEmailError(ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage());
    }
    recipient.setUpdatedAt(LocalDateTime.now());
    persistence.saveMailRecipient(recipient);
}
```

Add `retryEmail(Long id)` that requires current user to be sender or admin, then retries recipients whose status is `failed` or `skipped`.

- [ ] **Step 6: Add retry endpoint**

In `MailController.java`, add:

```java
@PostMapping("/{id}/retry-email")
public ApiResponse<MailDetailResponse> retryEmail(@PathVariable Long id) {
    return ApiResponse.ok(service.retryEmail(id));
}
```

- [ ] **Step 7: Run backend mail tests**

Run: `cd backend; mvn -Dtest=MailIntegrationTest test`

Expected: PASS.

- [ ] **Step 8: Commit**

```bash
git add backend/pom.xml backend/src/main/resources/application.properties backend/src/main/java/com/university/oms/service/EmailSenderService.java backend/src/main/java/com/university/oms/service/MailService.java backend/src/main/java/com/university/oms/controller/MailController.java backend/src/test/java/com/university/oms/MailIntegrationTest.java
git commit -m "feat: send external mail with delivery status"
```

---

### Task 6: Add Frontend API, Routing, And Organization Tree Selector

**Files:**
- Modify: `frontend/src/api.js`
- Modify: `frontend/src/router/index.js`
- Modify: `frontend/src/utils/navigation.js`
- Create: `frontend/src/components/OrgUserTreeSelect.vue`
- Create: `frontend/src/components/OrgUserTreeSelect.spec.js`

- [ ] **Step 1: Write failing component and navigation tests**

Create `frontend/src/components/OrgUserTreeSelect.spec.js`:

```javascript
import { describe, expect, it } from 'vitest'
import { readFileSync } from 'node:fs'
import { dirname, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'

const currentDir = dirname(fileURLToPath(import.meta.url))
const source = readFileSync(resolve(currentDir, 'OrgUserTreeSelect.vue'), 'utf-8')

describe('OrgUserTreeSelect', () => {
  it('uses an Element Plus tree and filters selectable user nodes', () => {
    expect(source).toContain('<el-tree')
    expect(source).toContain('type === \\'user\\'')
    expect(source).toContain('selectedUsers')
    expect(source).toContain('modelValue')
  })
})
```

Add to `frontend/src/utils/navigation.spec.js`:

```javascript
it('shows mail center to logged in users', () => {
  expect(visibleMenuItems(['office_user']).some((item) => item.index === '/mails')).toBe(true)
  expect(visibleMenuItems(['admin']).some((item) => item.index === '/mails')).toBe(true)
})
```

- [ ] **Step 2: Run frontend tests to verify they fail**

Run: `cd frontend; npm test -- OrgUserTreeSelect.spec.js navigation.spec.js`

Expected: `OrgUserTreeSelect.vue` missing and mail menu assertion failing.

- [ ] **Step 3: Add API endpoints**

In `frontend/src/api.js`, add:

```javascript
  orgTree: () => http.get('/org/tree'),
  sendMail: (data) => http.post('/mails', data),
  mailInbox: () => http.get('/mails/inbox'),
  mailSent: () => http.get('/mails/sent'),
  mailDetail: (id) => http.get(`/mails/${id}`),
  markMailRead: (id) => http.post(`/mails/${id}/read`),
  retryMailEmail: (id) => http.post(`/mails/${id}/retry-email`),
```

- [ ] **Step 4: Add route and menu item**

In `frontend/src/router/index.js`, import and route:

```javascript
import Mails from '../views/Mails.vue'

{ path: '/mails', component: Mails },
```

In `frontend/src/utils/navigation.js`, add after dashboard:

```javascript
{ index: '/mails', label: '邮件中心' },
```

- [ ] **Step 5: Create organization tree selector component**

Create `frontend/src/components/OrgUserTreeSelect.vue`:

```vue
<template>
  <div class="org-user-tree-select">
    <el-tree
      ref="treeRef"
      :data="treeData"
      node-key="id"
      show-checkbox
      default-expand-all
      :props="{ label: 'label', children: 'children' }"
      :check-strictly="true"
      @check="handleCheck"
    />
    <div class="selected-users">
      <el-tag v-for="user in selectedUsers" :key="user.userId" closable @close="removeUser(user.userId)">
        {{ user.label }}
      </el-tag>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, ref, watch } from 'vue'

const props = defineProps({
  modelValue: { type: Array, default: () => [] },
  treeData: { type: Array, default: () => [] }
})
const emit = defineEmits(['update:modelValue'])
const treeRef = ref(null)

const flattenUsers = (nodes) => {
  const users = []
  nodes.forEach((node) => {
    if (node.type === 'user') users.push(node)
    if (node.children) users.push(...flattenUsers(node.children))
  })
  return users
}

const selectedUsers = computed(() =>
  flattenUsers(props.treeData).filter((user) => props.modelValue.includes(user.userId))
)

const handleCheck = () => {
  const checked = treeRef.value?.getCheckedNodes(false, false) || []
  emit('update:modelValue', checked.filter((node) => node.type === 'user').map((node) => node.userId))
  nextTick(syncCheckedKeys)
}

const removeUser = (userId) => {
  emit('update:modelValue', props.modelValue.filter((id) => id !== userId))
}

const syncCheckedKeys = () => {
  treeRef.value?.setCheckedKeys(props.modelValue.map((id) => `user-${id}`))
}

watch(() => props.modelValue, () => nextTick(syncCheckedKeys), { deep: true })
watch(() => props.treeData, () => nextTick(syncCheckedKeys), { deep: true })
</script>

<style scoped>
.org-user-tree-select {
  display: grid;
  gap: 8px;
}

.selected-users {
  min-height: 32px;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
</style>
```

- [ ] **Step 6: Run frontend tests**

Run: `cd frontend; npm test -- OrgUserTreeSelect.spec.js navigation.spec.js`

Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add frontend/src/api.js frontend/src/router/index.js frontend/src/utils/navigation.js frontend/src/components/OrgUserTreeSelect.vue frontend/src/components/OrgUserTreeSelect.spec.js frontend/src/utils/navigation.spec.js
git commit -m "feat: add mail navigation and org user selector"
```

---

### Task 7: Add Mail Center Page

**Files:**
- Create: `frontend/src/views/Mails.vue`
- Create: `frontend/src/views/Mails.spec.js`

- [ ] **Step 1: Write failing mail page test**

Create `frontend/src/views/Mails.spec.js`:

```javascript
import { describe, expect, it } from 'vitest'
import { readFileSync } from 'node:fs'
import { dirname, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'

const currentDir = dirname(fileURLToPath(import.meta.url))
const source = readFileSync(resolve(currentDir, 'Mails.vue'), 'utf-8')

describe('mail center page', () => {
  it('contains inbox, sent, compose views and delivery status handling', () => {
    expect(source).toContain('收件箱')
    expect(source).toContain('已发送')
    expect(source).toContain('写邮件')
    expect(source).toContain('OrgUserTreeSelect')
    expect(source).toContain('sendMail')
    expect(source).toContain('retryMailEmail')
    expect(source).toContain('emailStatus')
  })
})
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd frontend; npm test -- Mails.spec.js`

Expected: missing `Mails.vue`.

- [ ] **Step 3: Create mail page**

Create `frontend/src/views/Mails.vue` with:

```vue
<template>
  <div class="mail-page">
    <div class="panel report-header">
      <h3>邮件中心</h3>
    </div>
    <div class="page-actions">
      <el-button type="primary" @click="openCompose">写邮件</el-button>
      <el-button @click="loadAll">刷新</el-button>
    </div>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="收件箱" name="inbox">
        <el-table :data="inbox" border>
          <el-table-column prop="subject" label="主题" min-width="180" />
          <el-table-column prop="senderName" label="发件人" width="120" />
          <el-table-column label="类型" width="80">
            <template #default="{ row }">{{ row.currentUserRecipientType === 'cc' ? '抄送' : '收件' }}</template>
          </el-table-column>
          <el-table-column label="状态" width="80">
            <template #default="{ row }">{{ row.currentUserRead ? '已读' : '未读' }}</template>
          </el-table-column>
          <el-table-column prop="createdAt" label="发送时间" width="180" />
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button size="small" @click="openDetail(row)">查看</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="已发送" name="sent">
        <el-table :data="sent" border>
          <el-table-column prop="subject" label="主题" min-width="180" />
          <el-table-column label="收件人" min-width="220">
            <template #default="{ row }">{{ recipientSummary(row) }}</template>
          </el-table-column>
          <el-table-column label="邮箱状态" width="150">
            <template #default="{ row }">{{ deliverySummary(row) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="170">
            <template #default="{ row }">
              <el-button size="small" @click="openDetail(row)">查看</el-button>
              <el-button size="small" @click="retryEmail(row)">重试邮箱</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="composeVisible" title="写邮件" width="720px" :close-on-click-modal="false">
      <el-form label-position="top">
        <el-form-item label="主题"><el-input v-model="form.subject" /></el-form-item>
        <el-form-item label="收件人">
          <OrgUserTreeSelect v-model="form.toUserIds" :tree-data="orgTree" />
        </el-form-item>
        <el-form-item label="抄送人">
          <OrgUserTreeSelect v-model="form.ccUserIds" :tree-data="orgTree" />
        </el-form-item>
        <el-form-item label="正文">
          <el-input v-model="form.content" type="textarea" :rows="8" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="composeVisible = false">取消</el-button>
        <el-button type="primary" :loading="sending" @click="submitMail">发送</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="邮件详情" width="720px">
      <template v-if="detail">
        <h4>{{ detail.subject }}</h4>
        <p class="meta">发件人：{{ detail.senderName }}</p>
        <p class="content">{{ detail.content }}</p>
        <el-table :data="detail.recipients" border>
          <el-table-column prop="realName" label="人员" />
          <el-table-column prop="recipientType" label="类型" width="80" />
          <el-table-column prop="emailStatus" label="邮箱状态" width="110" />
          <el-table-column prop="emailError" label="失败原因" />
        </el-table>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '../api'
import OrgUserTreeSelect from '../components/OrgUserTreeSelect.vue'

const activeTab = ref('inbox')
const inbox = ref([])
const sent = ref([])
const orgTree = ref([])
const composeVisible = ref(false)
const detailVisible = ref(false)
const detail = ref(null)
const sending = ref(false)
const form = reactive({ subject: '', content: '', toUserIds: [], ccUserIds: [] })

const loadAll = async () => {
  orgTree.value = await api.orgTree()
  inbox.value = await api.mailInbox()
  sent.value = await api.mailSent()
}

const openCompose = () => {
  form.subject = ''
  form.content = ''
  form.toUserIds = []
  form.ccUserIds = []
  composeVisible.value = true
}

const submitMail = async () => {
  if (!form.subject || !form.content || form.toUserIds.length === 0) {
    ElMessage.warning('主题、正文和收件人不能为空')
    return
  }
  sending.value = true
  try {
    const result = await api.sendMail(form)
    composeVisible.value = false
    const failed = result.recipients.some((item) => item.emailStatus === 'failed')
    const skipped = result.recipients.some((item) => item.emailStatus === 'skipped')
    ElMessage.success(failed || skipped ? '站内信已发送，部分邮箱未发送成功' : '站内信和邮箱均已发送')
    await loadAll()
  } catch (error) {
    ElMessage.error(error.message || '发送失败')
  } finally {
    sending.value = false
  }
}

const openDetail = async (row) => {
  detail.value = await api.mailDetail(row.id)
  if (!detail.value.currentUserRead) {
    detail.value = await api.markMailRead(row.id)
  }
  detailVisible.value = true
  await loadAll()
}

const retryEmail = async (row) => {
  await api.retryMailEmail(row.id)
  ElMessage.success('邮箱发送状态已刷新')
  await loadAll()
}

const recipientSummary = (row) => row.recipients.map((item) => item.realName).join('、')
const deliverySummary = (row) => {
  const statuses = row.recipients.map((item) => item.emailStatus)
  if (statuses.every((status) => status === 'sent')) return '全部成功'
  if (statuses.some((status) => status === 'failed')) return '部分失败'
  if (statuses.some((status) => status === 'skipped')) return '未启用'
  return '处理中'
}

onMounted(loadAll)
</script>

<style scoped>
.mail-page {
  display: grid;
  gap: 12px;
}

.meta {
  color: #606266;
}

.content {
  white-space: pre-wrap;
  line-height: 1.7;
}
</style>
```

- [ ] **Step 4: Run mail page test**

Run: `cd frontend; npm test -- Mails.spec.js`

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add frontend/src/views/Mails.vue frontend/src/views/Mails.spec.js
git commit -m "feat: add mail center page"
```

---

### Task 8: Final Verification

**Files:**
- Review all files modified in Tasks 1-7.

- [ ] **Step 1: Run backend mail-focused tests**

Run: `cd backend; mvn -Dtest=MailIntegrationTest,MysqlSchemaContractTest test`

Expected: PASS.

- [ ] **Step 2: Run full backend tests**

Run: `cd backend; mvn test`

Expected: PASS.

- [ ] **Step 3: Run frontend tests**

Run: `cd frontend; npm test`

Expected: PASS.

- [ ] **Step 4: Build frontend**

Run: `cd frontend; npm run build`

Expected: PASS and Vite writes `dist`.

- [ ] **Step 5: Manual smoke test with memory repository**

Run backend:

```bash
cd backend
mvn spring-boot:run
```

Run frontend in another terminal:

```bash
cd frontend
npm run dev
```

Manual path:

1. Log in as `admin` / `123456`.
2. Open user management and create a user with a valid email.
3. Log in as `user` / `123456`.
4. Open Mail Center.
5. Compose a mail to `head` and CC `office`.
6. Confirm the sent page shows both recipients and external status `skipped` when SMTP is disabled.
7. Log in as `head` / `123456`.
8. Confirm the mail appears in inbox and opens successfully.
9. Confirm a notification exists for the new mail.

- [ ] **Step 6: Document SMTP setup note**

If the README already has a configuration section, add:

````markdown
### External Email

Internal mail works without SMTP. To also send real email, set:

```properties
oms.mail.external-enabled=true
spring.mail.host=smtp.qq.com
spring.mail.port=587
spring.mail.username=your-account@qq.com
spring.mail.password=your-authorization-code
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

Use an email authorization code from the provider instead of the normal login password.
````

- [ ] **Step 7: Commit verification docs if README changed**

```bash
git add README.md
git commit -m "docs: add external mail configuration"
```

Skip this commit only if README already documents equivalent SMTP configuration.
