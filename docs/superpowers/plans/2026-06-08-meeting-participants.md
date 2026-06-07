# 会议参会人员管理 & 纪要确认公示 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为会议管理模块增加参会人选定、通知、记录员纪要填写、全员确认、公示/归档的完整闭环。

**Architecture:** 后端新增 MeetingParticipant 模型和参会人集合，扩展 MeetingService 六个新方法，前端复用已有 OrgUserTreeSelect 组件选择参会人，通过 el-tabs 增加「我参与的会议」视图。

**Tech Stack:** Java 8 / Spring Boot 2.5.6 / Vue 3 / Element Plus / Vitest / JUnit 5

---

## File Structure

### 新增文件

| 文件 | 职责 |
|------|------|
| `backend/src/main/java/com/university/oms/model/MeetingParticipant.java` | 参会人实体模型 |
| `backend/src/test/java/com/university/oms/service/MeetingParticipantTest.java` | 参会人相关后端测试 |

### 修改文件

| 文件 | 职责 |
|------|------|
| `backend/src/main/java/com/university/oms/model/Meeting.java` | 新增 recorderId 字段 |
| `backend/src/main/java/com/university/oms/dto/MeetingRequest.java` | 新增 participants、recorderId 字段 |
| `backend/src/main/java/com/university/oms/repository/InMemoryDatabase.java` | 新增 participants 集合和访问器 |
| `backend/src/main/java/com/university/oms/repository/DataPersistence.java` | 新增 saveMeetingParticipant 方法 |
| `backend/src/main/java/com/university/oms/repository/JdbcDataPersistence.java` | 实现参会人持久化 |
| `backend/src/main/java/com/university/oms/repository/MysqlDataLoader.java` | 新增加载参会人数据 |
| `backend/src/main/java/com/university/oms/service/MeetingService.java` | 核心：六个新方法 + 修改 create/archiveMinutes |
| `backend/src/main/java/com/university/oms/service/BusinessAccessService.java` | 新增参会人权限检查方法 |
| `backend/src/main/java/com/university/oms/controller/MeetingController.java` | 新增六个端点 |
| `backend/sql/schema.sql` | 新增 oa_meeting_participant 表、oa_meeting 增加 recorder_id 列 |
| `backend/sql/data.sql` | 新增 minutes_pending、minutes_confirmed 字典项 |
| `frontend/src/api.js` | 新增六个 API 方法 |
| `frontend/src/views/Meetings.vue` | 核心前端改造：参会人选择、纪要确认、公示/归档 |
| `frontend/src/views/Meetings.spec.js` | 新增前端测试 |

---

## Task 1: 后端数据层 — MeetingParticipant 模型 + InMemoryDatabase + 持久化

**Files:**
- Create: `backend/src/main/java/com/university/oms/model/MeetingParticipant.java`
- Modify: `backend/src/main/java/com/university/oms/model/Meeting.java:26` (新增 recorderId 字段)
- Modify: `backend/src/main/java/com/university/oms/repository/InMemoryDatabase.java:41` (新增集合) + `:298` (新增访问器)
- Modify: `backend/src/main/java/com/university/oms/repository/DataPersistence.java:28` (新增接口方法)
- Modify: `backend/src/main/java/com/university/oms/repository/JdbcDataPersistence.java:220` (新增实现)
- Modify: `backend/sql/schema.sql:148` (新增表和列)

- [ ] **Step 1: 创建 MeetingParticipant.java 模型**

文件：`backend/src/main/java/com/university/oms/model/MeetingParticipant.java`

```java
package com.university.oms.model;

import java.time.LocalDateTime;

public class MeetingParticipant extends BaseEntity {
    private Long meetingId;
    private Long userId;
    private boolean recorder;
    private boolean minutesConfirmed;
    private LocalDateTime confirmedAt;

    public Long getMeetingId() { return meetingId; }
    public void setMeetingId(Long meetingId) { this.meetingId = meetingId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public boolean isRecorder() { return recorder; }
    public void setRecorder(boolean recorder) { this.recorder = recorder; }
    public boolean isMinutesConfirmed() { return minutesConfirmed; }
    public void setMinutesConfirmed(boolean minutesConfirmed) { this.minutesConfirmed = minutesConfirmed; }
    public LocalDateTime getConfirmedAt() { return confirmedAt; }
    public void setConfirmedAt(LocalDateTime confirmedAt) { this.confirmedAt = confirmedAt; }
}
```

- [ ] **Step 2: 在 Meeting.java 中新增 recorderId 字段**

在 `backend/src/main/java/com/university/oms/model/Meeting.java` 第 26 行 `private String status;` 之后插入：

```java
    private Long recorderId;

    public Long getRecorderId() { return recorderId; }
    public void setRecorderId(Long recorderId) { this.recorderId = recorderId; }
```

- [ ] **Step 3: 在 InMemoryDatabase.java 中新增参会人集合**

在 `backend/src/main/java/com/university/oms/repository/InMemoryDatabase.java` 第 41 行之后（最后一个字段声明后）新增字段：

```java
    private final Map<Long, MeetingParticipant> participants = new ConcurrentHashMap<Long, MeetingParticipant>();
```

在第 298 行（最后一个访问器方法之后）新增访问器：

```java
    public Map<Long, MeetingParticipant> participants() { return participants; }
```

- [ ] **Step 4: 在 DataPersistence.java 中新增接口方法**

在 `backend/src/main/java/com/university/oms/repository/DataPersistence.java` 第 28 行之后新增：

```java
    default void saveMeetingParticipant(MeetingParticipant participant) { }
```

- [ ] **Step 5: 在 JdbcDataPersistence.java 中实现持久化**

在 `backend/src/main/java/com/university/oms/repository/JdbcDataPersistence.java` 的 saveFlowTask() 方法之后新增：

```java
    @Override
    public void saveMeetingParticipant(MeetingParticipant p) {
        jdbcTemplate.update(
            "REPLACE INTO oa_meeting_participant (id, meeting_id, user_id, is_recorder, minutes_confirmed, confirmed_at, created_at, updated_at) VALUES (?,?,?,?,?,?,?,?)",
            p.getId(), p.getMeetingId(), p.getUserId(), p.isRecorder() ? 1 : 0,
            p.isMinutesConfirmed() ? 1 : 0, p.getConfirmedAt(), p.getCreatedAt(), p.getUpdatedAt());
    }
```

- [ ] **Step 6: 更新 SQL Schema**

在 `backend/sql/schema.sql` 的 oa_meeting 表闭合括号之前（第 148 行前），新增 recorder_id 列：

```sql
  recorder_id BIGINT,
```

在 schema.sql 末尾新增建表语句：

```sql
CREATE TABLE IF NOT EXISTS oa_meeting_participant (
    id BIGINT PRIMARY KEY,
    meeting_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    is_recorder TINYINT(1) DEFAULT 0,
    minutes_confirmed TINYINT(1) DEFAULT 0,
    confirmed_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_meeting_user (meeting_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

- [ ] **Step 7: 在 MysqlDataLoader.java 中新增加载逻辑**

在 `backend/src/main/java/com/university/oms/repository/MysqlDataLoader.java` 的 load() 方法中，新增 clear 调用和 load 调用（与其他表一致的模式）。

在 clear 块中新增：
```java
db.participants().clear();
```

在 load 块中新增：
```java
loadParticipants();
```

新增 loadParticipants() 方法：
```java
private void loadParticipants() {
    jdbcTemplate.query("SELECT * FROM oa_meeting_participant", rs -> {
        MeetingParticipant p = new MeetingParticipant();
        db.fill(p, rs.getLong("id"));
        p.setMeetingId(rs.getLong("meeting_id"));
        p.setUserId(rs.getLong("user_id"));
        p.setRecorder(rs.getInt("is_recorder") == 1);
        p.setMinutesConfirmed(rs.getInt("minutes_confirmed") == 1);
        p.setConfirmedAt(rs.getTimestamp("confirmed_at") != null ? rs.getTimestamp("confirmed_at").toLocalDateTime() : null);
        db.participants().put(p.getId(), p);
    });
}
```

在 maxId() 的 SQL 中新增：
```sql
, COALESCE((SELECT MAX(id) FROM oa_meeting_participant),0)
```

- [ ] **Step 8: 更新 data.sql 字典项**

在 `backend/sql/data.sql` 的 business_status 字典项之后新增：

```sql
INSERT IGNORE INTO sys_dict_item (id, dict_type, dict_code, dict_label, sort_order, enabled, system_item) VALUES (216, 'business_status', 'minutes_pending', '纪要待确认', 156, 1, 1);
INSERT IGNORE INTO sys_dict_item (id, dict_type, dict_code, dict_label, sort_order, enabled, system_item) VALUES (217, 'business_status', 'minutes_confirmed', '纪要已确认', 157, 1, 1);
```

- [ ] **Step 9: 提交**

```bash
git add -A
git commit -m "feat(meeting): add MeetingParticipant model, persistence and schema"
```

---

## Task 2: 后端服务层 — MeetingRequest 扩展 + MeetingService 核心逻辑

**Files:**
- Modify: `backend/src/main/java/com/university/oms/dto/MeetingRequest.java:29` (新增字段)
- Modify: `backend/src/main/java/com/university/oms/service/MeetingService.java:80-152` (修改 create/archiveMinutes + 新增方法)
- Modify: `backend/src/main/java/com/university/oms/service/BusinessAccessService.java:58` (新增权限方法)

- [ ] **Step 1: 扩展 MeetingRequest.java**

在 `backend/src/main/java/com/university/oms/dto/MeetingRequest.java` 第 29 行之后新增字段：

```java
    private List<Long> participants;
    private Long recorderId;

    public List<Long> getParticipants() { return participants; }
    public void setParticipants(List<Long> participants) { this.participants = participants; }
    public Long getRecorderId() { return recorderId; }
    public void setRecorderId(Long recorderId) { this.recorderId = recorderId; }
```

在文件顶部新增 import：
```java
import java.util.List;
```

- [ ] **Step 2: 修改 MeetingService.create() 方法**

在 `backend/src/main/java/com/university/oms/service/MeetingService.java` 的 create() 方法中，在 `validateMeetingFee(request)` 调用之后（约第 104 行），新增参会人校验和保存逻辑：

```java
        // 校验参会人
        if (request.getParticipants() == null || request.getParticipants().isEmpty()) {
            throw new BusinessException("请选择至少一位参会人员");
        }
        if (request.getRecorderId() == null) {
            throw new BusinessException("请指定会议记录员");
        }
        if (!request.getParticipants().contains(request.getRecorderId())) {
            throw new BusinessException("记录员必须在参会人员中");
        }
```

在 `meeting.setOtherFee(request.getOtherFee());` 之后新增：

```java
        meeting.setRecorderId(request.getRecorderId());
```

在 `workflowService.startFlow(...)` 之后新增保存参会人的逻辑：

```java
        // 保存参会人
        for (Long userId : request.getParticipants()) {
            MeetingParticipant participant = new MeetingParticipant();
            db.fill(participant, db.nextId());
            participant.setMeetingId(meeting.getId());
            participant.setUserId(userId);
            participant.setRecorder(userId.equals(request.getRecorderId()));
            db.participants().put(participant.getId(), participant);
            persistence.saveMeetingParticipant(participant);
        }
```

同时在 MeetingService 文件顶部新增 import：
```java
import com.university.oms.model.MeetingParticipant;
import java.util.Set;
import java.util.HashSet;
```

- [ ] **Step 3: 修改 MeetingService.archiveMinutes() 方法**

将 archiveMinutes() 方法修改为：仅记录员可调用，提交后状态变为 `minutes_pending`，并通知参会人确认：

将原有的：
```java
    public Meeting archiveMinutes(Long id, MeetingMinutesRequest request) {
        Meeting meeting = db.meetings().get(id);
        if (meeting == null) {
            throw new BusinessException("会议不存在");
        }
        accessService.requireMeetingMinutesArchive(meeting);
        if (!"approved".equals(meeting.getStatus())) {
            throw new BusinessException("只有审批通过的会议可以归档纪要");
        }
        meeting.setMinutes(request.getMinutes());
        meeting.setSignInCount(request.getSignInCount() == null ? 0 : request.getSignInCount());
        meeting.setStatus("archived");
        meeting.setUpdatedAt(LocalDateTime.now());
        persistence.saveMeeting(meeting);
        approvalService.record("meeting", id, AuthContext.currentUserIdOr(meeting.getOrganizerId()), "archive_minutes", "会议纪要归档");
        workflowService.advanceFlow("meeting", id, "approved", "archived", meeting.getOrganizerId());
        return meeting;
    }
```

替换为：
```java
    public Meeting archiveMinutes(Long id, MeetingMinutesRequest request) {
        Meeting meeting = db.meetings().get(id);
        if (meeting == null) {
            throw new BusinessException("会议不存在");
        }
        if (!"approved".equals(meeting.getStatus())) {
            throw new BusinessException("只有审批通过的会议可以填写纪要");
        }
        Long currentUserId = AuthContext.currentUserIdOr(0L);
        if (!currentUserId.equals(meeting.getRecorderId())) {
            throw new BusinessException("只有记录员可以填写会议纪要");
        }
        meeting.setMinutes(request.getMinutes());
        meeting.setSignInCount(request.getSignInCount() == null ? 0 : request.getSignInCount());
        meeting.setStatus("minutes_pending");
        meeting.setUpdatedAt(LocalDateTime.now());
        persistence.saveMeeting(meeting);
        approvalService.record("meeting", id, currentUserId, "archive_minutes", "记录员填写会议纪要");
        workflowService.advanceFlow("meeting", id, "approved", "minutes_pending", meeting.getOrganizerId());
        // 通知所有参会人确认纪要
        for (MeetingParticipant p : getMeetingParticipants(meeting.getId())) {
            if (!p.isRecorder()) {
                workflowService.notifyUser(p.getUserId(), "会议纪要待确认",
                        "会议《" + meeting.getTitle() + "》的纪要已填写，请及时确认。", "meeting", meeting.getId());
            }
        }
        return meeting;
    }
```

- [ ] **Step 4: 在 MeetingService 中新增六个方法**

在 archiveMinutes() 方法之后、validateMeetingFee() 之前新增：

```java
    public List<MeetingParticipant> getMeetingParticipants(Long meetingId) {
        List<MeetingParticipant> result = new ArrayList<MeetingParticipant>();
        for (MeetingParticipant p : db.participants().values()) {
            if (p.getMeetingId().equals(meetingId)) {
                result.add(p);
            }
        }
        return result;
    }

    public List<Meeting> participatedMeetings() {
        User user = AuthContext.requireUser();
        Set<Long> meetingIds = new HashSet<Long>();
        for (MeetingParticipant p : db.participants().values()) {
            if (p.getUserId().equals(user.getId())) {
                meetingIds.add(p.getMeetingId());
            }
        }
        List<Meeting> result = new ArrayList<Meeting>();
        for (Long mid : meetingIds) {
            Meeting m = db.meetings().get(mid);
            if (m != null) {
                result.add(m);
            }
        }
        return result;
    }

    public Meeting confirmMinutes(Long meetingId) {
        User user = AuthContext.requireUser();
        Meeting meeting = db.meetings().get(meetingId);
        if (meeting == null) {
            throw new BusinessException("会议不存在");
        }
        if (!"minutes_pending".equals(meeting.getStatus())) {
            throw new BusinessException("当前状态无法确认纪要");
        }
        MeetingParticipant target = null;
        for (MeetingParticipant p : db.participants().values()) {
            if (p.getMeetingId().equals(meetingId) && p.getUserId().equals(user.getId())) {
                target = p;
                break;
            }
        }
        if (target == null) {
            throw new BusinessException("您不是该会议的参会人员");
        }
        if (target.isMinutesConfirmed()) {
            throw new BusinessException("您已确认过纪要");
        }
        target.setMinutesConfirmed(true);
        target.setConfirmedAt(LocalDateTime.now());
        target.setUpdatedAt(LocalDateTime.now());
        persistence.saveMeetingParticipant(target);
        approvalService.record("meeting", meetingId, user.getId(), "confirm_minutes", "参会人确认纪要");

        // 检查是否全员确认
        boolean allConfirmed = true;
        for (MeetingParticipant p : getMeetingParticipants(meetingId)) {
            if (!p.isMinutesConfirmed()) {
                allConfirmed = false;
                break;
            }
        }
        if (allConfirmed) {
            meeting.setStatus("minutes_confirmed");
            meeting.setUpdatedAt(LocalDateTime.now());
            persistence.saveMeeting(meeting);
            workflowService.advanceFlow("meeting", meetingId, "minutes_pending", "minutes_confirmed", meeting.getOrganizerId());
            workflowService.notifyUser(meeting.getOrganizerId(), "会议纪要全员确认完成",
                    "会议《" + meeting.getTitle() + "》的纪要已由所有参会人确认，请决定是否公示。", "meeting", meetingId);
        }
        return meeting;
    }

    public Meeting publishMeeting(Long meetingId) {
        User user = AuthContext.requireUser();
        Meeting meeting = db.meetings().get(meetingId);
        if (meeting == null) {
            throw new BusinessException("会议不存在");
        }
        if (!user.getId().equals(meeting.getOrganizerId())) {
            throw new BusinessException("只有组织者可以发布");
        }
        if (!"minutes_confirmed".equals(meeting.getStatus())) {
            throw new BusinessException("只有全员确认后的会议可以发布");
        }
        // 创建通知公告
        Announcement announcement = new Announcement();
        db.fill(announcement, db.nextId());
        announcement.setTitle("会议纪要公示：" + meeting.getTitle());
        announcement.setContent(meeting.getMinutes() != null ? meeting.getMinutes() : "");
        announcement.setCategory("会议纪要");
        announcement.setTargetType("all");
        announcement.setPublisherId(user.getId());
        announcement.setStatus("published");
        announcement.setPublishedAt(LocalDateTime.now());
        db.announcements().put(announcement.getId(), announcement);
        persistence.saveAnnouncement(announcement);

        meeting.setStatus("archived");
        meeting.setUpdatedAt(LocalDateTime.now());
        persistence.saveMeeting(meeting);
        approvalService.record("meeting", meetingId, user.getId(), "publish", "发布为公告");
        workflowService.advanceFlow("meeting", meetingId, "minutes_confirmed", "archived", meeting.getOrganizerId());
        return meeting;
    }

    public Meeting archiveDirectly(Long meetingId) {
        User user = AuthContext.requireUser();
        Meeting meeting = db.meetings().get(meetingId);
        if (meeting == null) {
            throw new BusinessException("会议不存在");
        }
        if (!user.getId().equals(meeting.getOrganizerId())) {
            throw new BusinessException("只有组织者可以归档");
        }
        if (!"minutes_confirmed".equals(meeting.getStatus())) {
            throw new BusinessException("只有全员确认后的会议可以归档");
        }
        meeting.setStatus("archived");
        meeting.setUpdatedAt(LocalDateTime.now());
        persistence.saveMeeting(meeting);
        approvalService.record("meeting", meetingId, user.getId(), "archive", "直接归档");
        workflowService.advanceFlow("meeting", meetingId, "minutes_confirmed", "archived", meeting.getOrganizerId());
        return meeting;
    }

    public void remindParticipant(Long meetingId, Long userId) {
        User currentUser = AuthContext.requireUser();
        Meeting meeting = db.meetings().get(meetingId);
        if (meeting == null) {
            throw new BusinessException("会议不存在");
        }
        if (!currentUser.getId().equals(meeting.getOrganizerId())) {
            throw new BusinessException("只有组织者可以催办");
        }
        // 验证被催办人是参会人且未确认
        MeetingParticipant target = null;
        for (MeetingParticipant p : db.participants().values()) {
            if (p.getMeetingId().equals(meetingId) && p.getUserId().equals(userId)) {
                target = p;
                break;
            }
        }
        if (target == null) {
            throw new BusinessException("该用户不是参会人员");
        }
        if (target.isMinutesConfirmed()) {
            throw new BusinessException("该参会人已确认纪要");
        }
        workflowService.notifyUser(userId, "会议纪要确认催办",
                "组织者提醒您尽快确认会议《" + meeting.getTitle() + "》的纪要。", "meeting", meetingId);
    }
```

在 MeetingService 文件顶部新增 import：
```java
import com.university.oms.model.Announcement;
```

注意：publishMeeting() 方法中需要通过 persistence 保存公告。确认 `DataPersistence` 接口已有 `saveAnnouncement(Announcement)` 方法。由于 AnnouncementService 的 create 方法内部有权限检查，这里直接操作数据库和持久化层更简洁。

- [ ] **Step 5: 在 BusinessAccessService 中新增权限方法**

在 `backend/src/main/java/com/university/oms/service/BusinessAccessService.java` 的 requireMeetingMinutesArchive() 方法之后新增：

```java
    public void requireMeetingRecorder(Meeting meeting) {
        User user = AuthContext.requireUser();
        if (!user.getId().equals(meeting.getRecorderId())) {
            throw new BusinessException("只有记录员可以填写会议纪要");
        }
    }

    public void requireMeetingOrganizer(Meeting meeting) {
        User user = AuthContext.requireUser();
        if (!user.getId().equals(meeting.getOrganizerId())) {
            throw new BusinessException("只有组织者可以操作");
        }
    }
```

- [ ] **Step 6: 扩展 meetings() 方法以包含参会人可见性**

在 `MeetingService.meetings()` 方法（第 52-68 行）中，现有的可见范围逻辑需要增加：参会人可以看到自己参与的会议。

在 `scoped` 列表构建循环中，新增参会人可见性检查：

在现有 for 循环内 `if` 条件末尾追加：
```java
                || isParticipant(meeting.getId(), user.getId())
```

并新增私有辅助方法：
```java
    private boolean isParticipant(Long meetingId, Long userId) {
        for (MeetingParticipant p : db.participants().values()) {
            if (p.getMeetingId().equals(meetingId) && p.getUserId().equals(userId)) {
                return true;
            }
        }
        return false;
    }
```

- [ ] **Step 7: 更新 JdbcDataPersistence 的 saveMeeting 方法**

在 `backend/src/main/java/com/university/oms/repository/JdbcDataPersistence.java` 的 saveMeeting() 方法中，REPLACE INTO SQL 新增 `recorder_id` 列。

在现有的列列表和 VALUES 中新增：
```sql
recorder_id
```
和对应的参数：
```java
m.getRecorderId()
```

- [ ] **Step 8: 提交**

```bash
git add -A
git commit -m "feat(meeting): add participant selection, minutes confirmation and publish logic"
```

---

## Task 3: 后端控制器 — 新增六个 API 端点

**Files:**
- Modify: `backend/src/main/java/com/university/oms/controller/MeetingController.java:47` (新增端点)

- [ ] **Step 1: 在 MeetingController 中新增端点**

在 `backend/src/main/java/com/university/oms/controller/MeetingController.java` 的 archiveMinutes() 方法之后（第 47 行前），新增：

```java
    @GetMapping("/participated")
    public ApiResponse<List<Meeting>> participatedMeetings() {
        return ApiResponse.ok(service.participatedMeetings());
    }

    @GetMapping("/{id}/participants")
    public ApiResponse<List<MeetingParticipant>> getParticipants(@PathVariable Long id) {
        return ApiResponse.ok(service.getMeetingParticipants(id));
    }

    @PostMapping("/{id}/confirm-minutes")
    public ApiResponse<Meeting> confirmMinutes(@PathVariable Long id) {
        return ApiResponse.ok(service.confirmMinutes(id));
    }

    @PostMapping("/{id}/publish")
    public ApiResponse<Meeting> publish(@PathVariable Long id) {
        return ApiResponse.ok(service.publishMeeting(id));
    }

    @PostMapping("/{id}/archive")
    public ApiResponse<Meeting> archive(@PathVariable Long id) {
        return ApiResponse.ok(service.archiveDirectly(id));
    }

    @PostMapping("/{id}/remind-participant/{userId}")
    public ApiResponse<Void> remindParticipant(@PathVariable Long id, @PathVariable Long userId) {
        service.remindParticipant(id, userId);
        return ApiResponse.ok(null);
    }
```

在文件顶部新增 import：
```java
import com.university.oms.model.MeetingParticipant;
import java.util.List;
```

- [ ] **Step 2: 提交**

```bash
git add -A
git commit -m "feat(meeting): add participant, confirmation and publish API endpoints"
```

---

## Task 4: 后端测试

**Files:**
- Create: `backend/src/test/java/com/university/oms/service/MeetingParticipantTest.java`

- [ ] **Step 1: 编写后端测试**

创建 `backend/src/test/java/com/university/oms/service/MeetingParticipantTest.java`：

```java
package com.university.oms.service;

import com.university.oms.common.BusinessException;
import com.university.oms.dto.MeetingRequest;
import com.university.oms.dto.MeetingMinutesRequest;
import com.university.oms.model.Meeting;
import com.university.oms.model.MeetingParticipant;
import com.university.oms.model.User;
import com.university.oms.repository.InMemoryDatabase;
import com.university.oms.security.AuthContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MeetingParticipantTest {
    private InMemoryDatabase db;
    private MeetingService meetingService;
    private TestAuthHelper authHelper;

    @BeforeEach
    void setUp() {
        db = new InMemoryDatabase();
        db.init();
        DataPersistence noopPersistence = new NoopDataPersistence();
        DictionaryService dictService = new DictionaryService(db);
        BusinessAccessService accessService = new BusinessAccessService(db);
        WorkflowService workflowService = new WorkflowService(db, noopPersistence,
                new ApprovalFlowConfig(), accessService, null, dictService);
        ApprovalService approvalService = new ApprovalService(db, noopPersistence, workflowService, accessService);
        meetingService = new MeetingService(db, approvalService, noopPersistence,
                workflowService, accessService, dictService);
        authHelper = new TestAuthHelper(db);
    }

    @AfterEach
    void tearDown() {
        AuthContext.clear();
    }

    @Test
    void createMeetingRequiresParticipants() {
        authHelper.loginAs("user");
        MeetingRequest req = new MeetingRequest();
        req.setTitle("测试会议");
        req.setRoomId(1L);
        req.setOrganizerId(2L);
        req.setStartTime(LocalDateTime.of(2026, 7, 1, 9, 0));
        req.setEndTime(LocalDateTime.of(2026, 7, 1, 11, 0));
        req.setParticipants(null);
        assertThrows(BusinessException.class, () -> meetingService.create(req));
    }

    @Test
    void createMeetingRequiresEmptyParticipants() {
        authHelper.loginAs("user");
        MeetingRequest req = new MeetingRequest();
        req.setTitle("测试会议");
        req.setRoomId(1L);
        req.setOrganizerId(2L);
        req.setStartTime(LocalDateTime.of(2026, 7, 1, 9, 0));
        req.setEndTime(LocalDateTime.of(2026, 7, 1, 11, 0));
        req.setParticipants(Arrays.asList());
        assertThrows(BusinessException.class, () -> meetingService.create(req));
    }

    @Test
    void createMeetingRequiresRecorderId() {
        authHelper.loginAs("user");
        MeetingRequest req = validRequest();
        req.setParticipants(Arrays.asList(2L, 3L));
        req.setRecorderId(null);
        assertThrows(BusinessException.class, () -> meetingService.create(req));
    }

    @Test
    void createMeetingRecorderMustBeParticipant() {
        authHelper.loginAs("user");
        MeetingRequest req = validRequest();
        req.setParticipants(Arrays.asList(2L, 3L));
        req.setRecorderId(999L);
        assertThrows(BusinessException.class, () -> meetingService.create(req));
    }

    @Test
    void createMeetingWithParticipantsSuccess() {
        authHelper.loginAs("user");
        MeetingRequest req = validRequest();
        req.setParticipants(Arrays.asList(2L, 3L, 5L));
        req.setRecorderId(3L);
        Meeting meeting = meetingService.create(req);
        assertNotNull(meeting);
        assertEquals(3, meeting.getExpectedCount());
        assertEquals(3L, meeting.getRecorderId());

        List<MeetingParticipant> participants = meetingService.getMeetingParticipants(meeting.getId());
        assertEquals(3, participants.size());
        assertTrue(participants.stream().anyMatch(p -> p.getUserId() == 3L && p.isRecorder()));
    }

    @Test
    void archiveMinutesRequiresRecorder() {
        authHelper.loginAs("user");
        MeetingRequest req = validRequest();
        req.setParticipants(Arrays.asList(2L, 3L));
        req.setRecorderId(3L);
        Meeting meeting = meetingService.create(req);

        // 模拟审批通过
        meeting.setStatus("approved");

        // 以非记录员身份填写纪要
        authHelper.loginAs("user");
        MeetingMinutesRequest minutesReq = new MeetingMinutesRequest();
        minutesReq.setMinutes("会议纪要内容");
        assertThrows(BusinessException.class, () -> meetingService.archiveMinutes(meeting.getId(), minutesReq));
    }

    @Test
    void archiveMinutesByRecorderSetsPendingStatus() {
        authHelper.loginAs("user");
        MeetingRequest req = validRequest();
        req.setParticipants(Arrays.asList(2L, 3L));
        req.setRecorderId(2L);
        Meeting meeting = meetingService.create(req);
        meeting.setStatus("approved");

        MeetingMinutesRequest minutesReq = new MeetingMinutesRequest();
        minutesReq.setMinutes("会议纪要内容");
        Meeting result = meetingService.archiveMinutes(meeting.getId(), minutesReq);
        assertEquals("minutes_pending", result.getStatus());
    }

    @Test
    void confirmMinutesByNonParticipantFails() {
        authHelper.loginAs("user");
        MeetingRequest req = validRequest();
        req.setParticipants(Arrays.asList(2L, 3L));
        req.setRecorderId(2L);
        Meeting meeting = meetingService.create(req);
        meeting.setStatus("approved");
        meetingService.archiveMinutes(meeting.getId(), new MeetingMinutesRequest());

        // 以非参会人身份确认
        authHelper.loginAs("leader");
        assertThrows(BusinessException.class, () -> meetingService.confirmMinutes(meeting.getId()));
    }

    @Test
    void confirmMinutesByParticipantSuccess() {
        authHelper.loginAs("user");
        MeetingRequest req = validRequest();
        req.setParticipants(Arrays.asList(2L, 3L));
        req.setRecorderId(2L);
        Meeting meeting = meetingService.create(req);
        meeting.setStatus("approved");
        meetingService.archiveMinutes(meeting.getId(), new MeetingMinutesRequest());

        // 参会人3确认
        authHelper.loginAs("head");
        Meeting result = meetingService.confirmMinutes(meeting.getId());
        // 还未全员确认（记录员2尚未确认）
        assertEquals("minutes_pending", result.getStatus());

        // 记录员2确认
        authHelper.loginAs("user");
        result = meetingService.confirmMinutes(meeting.getId());
        assertEquals("minutes_confirmed", result.getStatus());
    }

    @Test
    void duplicateConfirmFails() {
        authHelper.loginAs("user");
        MeetingRequest req = validRequest();
        req.setParticipants(Arrays.asList(2L, 3L));
        req.setRecorderId(2L);
        Meeting meeting = meetingService.create(req);
        meeting.setStatus("approved");
        meetingService.archiveMinutes(meeting.getId(), new MeetingMinutesRequest());

        authHelper.loginAs("head");
        meetingService.confirmMinutes(meeting.getId());
        assertThrows(BusinessException.class, () -> meetingService.confirmMinutes(meeting.getId()));
    }

    @Test
    void publishCreatesAnnouncement() {
        authHelper.loginAs("user");
        MeetingRequest req = validRequest();
        req.setParticipants(Arrays.asList(2L, 3L));
        req.setRecorderId(2L);
        Meeting meeting = meetingService.create(req);
        meeting.setStatus("approved");
        meetingService.archiveMinutes(meeting.getId(), minutesWithContent("纪要内容"));
        authHelper.loginAs("head");
        meetingService.confirmMinutes(meeting.getId());
        authHelper.loginAs("user");
        meetingService.confirmMinutes(meeting.getId());

        Meeting result = meetingService.publishMeeting(meeting.getId());
        assertEquals("archived", result.getStatus());
        // 验证公告已创建
        assertTrue(db.announcements().values().stream()
                .anyMatch(a -> a.getTitle().contains(meeting.getTitle())));
    }

    @Test
    void archiveDirectlyWithoutPublish() {
        authHelper.loginAs("user");
        MeetingRequest req = validRequest();
        req.setParticipants(Arrays.asList(2L, 3L));
        req.setRecorderId(2L);
        Meeting meeting = meetingService.create(req);
        meeting.setStatus("approved");
        meetingService.archiveMinutes(meeting.getId(), minutesWithContent("纪要内容"));
        authHelper.loginAs("head");
        meetingService.confirmMinutes(meeting.getId());
        authHelper.loginAs("user");
        meetingService.confirmMinutes(meeting.getId());

        Meeting result = meetingService.archiveDirectly(meeting.getId());
        assertEquals("archived", result.getStatus());
        // 不应创建公告
        assertFalse(db.announcements().values().stream()
                .anyMatch(a -> a.getTitle().contains(meeting.getTitle())));
    }

    @Test
    void publishRequiresOrganizer() {
        authHelper.loginAs("user");
        MeetingRequest req = validRequest();
        req.setParticipants(Arrays.asList(2L, 3L));
        req.setRecorderId(3L);
        Meeting meeting = meetingService.create(req);
        meeting.setStatus("minutes_confirmed");

        authHelper.loginAs("head");
        assertThrows(BusinessException.class, () -> meetingService.publishMeeting(meeting.getId()));
    }

    private MeetingRequest validRequest() {
        MeetingRequest req = new MeetingRequest();
        req.setTitle("测试会议");
        req.setRoomId(1L);
        req.setOrganizerId(2L);
        req.setStartTime(LocalDateTime.of(2026, 7, 1, 9, 0));
        req.setEndTime(LocalDateTime.of(2026, 7, 1, 11, 0));
        req.setExpectedCount(2);
        return req;
    }

    private MeetingMinutesRequest minutesWithContent(String content) {
        MeetingMinutesRequest req = new MeetingMinutesRequest();
        req.setMinutes(content);
        return req;
    }
}
```

> **注意：** 此测试文件中的 `TestAuthHelper` 和 `NoopDataPersistence` 需要确认是否已在项目中存在。如果不存在，需要创建简单的辅助类。`TestAuthHelper` 封装了设置 AuthContext 的逻辑，`NoopDataPersistence` 是 DataPersistence 接口的空实现。根据项目的实际测试工具类调整。

- [ ] **Step 2: 运行测试确认失败**

Run: `cd backend && mvn test -pl . -Dtest=MeetingParticipantTest -Dsurefire.useFile=false`
Expected: FAIL（因为 MeetingParticipant 模型和 MeetingService 方法还未完全实现）

- [ ] **Step 3: 确保测试通过**

如果 Task 1-3 已全部实现，运行：
Run: `cd backend && mvn test -Dsurefire.useFile=false`
Expected: ALL PASS

- [ ] **Step 4: 提交**

```bash
git add -A
git commit -m "test(meeting): add participant, confirmation and publish tests"
```

---

## Task 5: 前端 API 层

**Files:**
- Modify: `frontend/src/api.js:127` (新增六个 API 方法)

- [ ] **Step 1: 在 api.js 中新增方法**

在 `frontend/src/api.js` 的 `archiveMeetingMinutes` 方法之后（约第 127 行后）新增：

```javascript
  meetingsParticipated: () => http.get('/meetings/participated'),
  meetingParticipants: (id) => http.get(`/meetings/${id}/participants`),
  confirmMeetingMinutes: (id) => http.post(`/meetings/${id}/confirm-minutes`),
  publishMeeting: (id) => http.post(`/meetings/${id}/publish`),
  archiveMeeting: (id) => http.post(`/meetings/${id}/archive`),
  remindParticipant: (meetingId, userId) => http.post(`/meetings/${meetingId}/remind-participant/${userId}`),
```

- [ ] **Step 2: 提交**

```bash
git add -A
git commit -m "feat(meeting): add participant API methods to frontend"
```

---

## Task 6: 前端核心 — Meetings.vue 全面改造

**Files:**
- Modify: `frontend/src/views/Meetings.vue` (全面改造)

这是最大的改动。将现有的单列表页面改造为带 Tab 的页面，包含「会议申请/列表」和「我参与的会议」两个 Tab。

- [ ] **Step 1: 重写 Meetings.vue**

将 `frontend/src/views/Meetings.vue` 的全部内容替换为以下完整代码：

```vue
<template>
  <div class="meeting-page">
    <div class="panel report-header">
      <h3>会议管理</h3>
    </div>

    <el-tabs v-model="activeTab">
      <!-- ===== Tab 1: 会议管理 ===== -->
      <el-tab-pane label="会议列表" name="list">
        <div class="page-actions">
          <el-button type="primary" @click="openApplicationDialog">会议申请</el-button>
        </div>

        <el-table :data="meetings" border v-loading="loading">
          <el-table-column prop="title" label="主题" min-width="140" />
          <el-table-column label="参会人" width="75">
            <template #default="{ row }">
              <el-button link type="primary" @click="showParticipants(row)">{{ getParticipantCount(row) }}</el-button>
            </template>
          </el-table-column>
          <el-table-column label="类别" width="130"><template #default="{ row }">{{ labelOf('meeting_type', row.meetingType) }}</template></el-table-column>
          <el-table-column label="场地" width="72"><template #default="{ row }">{{ labelOf('venue_type', row.venueType) }}</template></el-table-column>
          <el-table-column prop="budget" label="预算" width="90" />
          <el-table-column label="大型活动" width="88"><template #default="{ row }">{{ row.largeActivity ? '是' : '否' }}</template></el-table-column>
          <el-table-column label="状态" width="115"><template #default="{ row }">{{ labelOf('business_status', row.status) }}</template></el-table-column>
          <el-table-column label="办理" width="220">
            <template #default="{ row }">
              <div class="table-actions">
                <!-- 记录员填写纪要 -->
                <el-button v-if="row.status === 'approved' && isRecorder(row)" size="small" type="success" @click="archiveMinutes(row)">填写纪要</el-button>
                <!-- 组织者在全员确认后：公示/归档 -->
                <el-button v-if="row.status === 'minutes_confirmed' && isOrganizer(row)" size="small" type="warning" @click="publishMeeting(row)">发布为公告</el-button>
                <el-button v-if="row.status === 'minutes_confirmed' && isOrganizer(row)" size="small" @click="archiveDirectly(row)">直接归档</el-button>
                <!-- 查看确认进度 -->
                <el-button v-if="row.status === 'minutes_pending' || row.status === 'minutes_confirmed'" size="small" @click="showConfirmProgress(row)">确认进度</el-button>
                <el-button size="small" @click="openFlowGuide(row)">流程导览</el-button>
              </div>
            </template>
          </el-table-column>
          <template #empty><el-empty description="暂无会议" /></template>
        </el-table>
      </el-tab-pane>

      <!-- ===== Tab 2: 我参与的会议 ===== -->
      <el-tab-pane label="我参与的会议" name="participated">
        <el-table :data="participatedMeetings" border v-loading="loading">
          <el-table-column prop="title" label="主题" min-width="160" />
          <el-table-column label="开始时间" width="170"><template #default="{ row }">{{ formatDate(row.startTime) }}</template></el-table-column>
          <el-table-column label="状态" width="115"><template #default="{ row }">{{ labelOf('business_status', row.status) }}</template></el-table-column>
          <el-table-column label="纪要状态" width="100">
            <template #default="{ row }">
              <template v-if="row.status === 'minutes_pending'">
                <el-tag type="warning" size="small">待确认</el-tag>
              </template>
              <template v-else-if="row.status === 'minutes_confirmed' || row.status === 'archived'">
                <el-tag type="success" size="small">已确认</el-tag>
              </template>
              <template v-else>
                <el-tag size="small">未填写</el-tag>
              </template>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180">
            <template #default="{ row }">
              <div class="table-actions">
                <el-button v-if="row.status === 'minutes_pending' && !isMyConfirmed(row)" size="small" type="primary" @click="confirmMinutes(row)">确认纪要</el-button>
                <el-button v-if="row.minutes" size="small" @click="viewMinutes(row)">查看纪要</el-button>
              </div>
            </template>
          </el-table-column>
          <template #empty><el-empty description="暂无参与的会议" /></template>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <!-- ===== 会议申请对话框 ===== -->
    <el-dialog v-model="applicationDialog" title="会议申请" width="640px" :close-on-click-modal="false">
      <el-form label-position="top">
        <el-form-item label="主题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="会议室">
          <el-select v-model="form.roomId"><el-option v-for="room in rooms" :key="room.id" :label="`${room.roomName}（${room.capacity} 人）`" :value="room.id" /></el-select>
        </el-form-item>
        <el-form-item label="开始时间"><el-date-picker v-model="form.startTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" /></el-form-item>
        <el-form-item label="结束时间"><el-date-picker v-model="form.endTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" /></el-form-item>
        <el-form-item label="参会人员（已选：{{ form.participants.length }} 人）">
          <OrgUserTreeSelect v-model="form.participants" :tree-data="orgTree" />
        </el-form-item>
        <el-form-item label="记录员">
          <el-select v-model="form.recorderId" placeholder="请先选择参会人员" :disabled="form.participants.length === 0">
            <el-option v-for="uid in form.participants" :key="uid" :label="userName(uid)" :value="uid" />
          </el-select>
        </el-form-item>
        <el-form-item label="场地类型">
          <el-select v-model="form.venueType"><el-option v-for="item in optionsOf('venue_type')" :key="item.value" :label="item.label" :value="item.value" /></el-select>
        </el-form-item>
        <el-form-item label="会议类别">
          <el-select v-model="form.meetingType"><el-option v-for="item in optionsOf('meeting_type')" :key="item.value" :label="item.label" :value="item.value" /></el-select>
        </el-form-item>
        <el-form-item label="住宿费"><el-input-number v-model="form.accommodationFee" :min="0" /></el-form-item>
        <el-form-item label="伙食费"><el-input-number v-model="form.mealFee" :min="0" /></el-form-item>
        <el-form-item label="场地费"><el-input-number v-model="form.venueFee" :min="0" /></el-form-item>
        <el-form-item label="其他费用"><el-input-number v-model="form.otherFee" :min="0" /></el-form-item>
        <el-form-item label="申报预算"><el-input-number v-model="form.budget" :min="0" /></el-form-item>
        <template v-if="isLarge">
          <el-form-item label="风险报告地址"><el-input v-model="form.riskReportUrl" /></el-form-item>
          <el-form-item label="安全方案地址"><el-input v-model="form.securityPlanUrl" /></el-form-item>
          <el-form-item label="应急预案地址"><el-input v-model="form.emergencyPlanUrl" /></el-form-item>
        </template>
      </el-form>
      <p v-if="isLarge" class="rule-note">大型活动须至少提前 15 个工作日申请并提交安全材料。</p>
      <template #footer>
        <el-tag v-if="isLarge" type="danger">大型活动</el-tag>
        <el-button @click="applicationDialog = false">取消</el-button>
        <el-button type="primary" @click="submit">提交会议</el-button>
      </template>
    </el-dialog>

    <!-- ===== 参会人列表对话框 ===== -->
    <el-dialog v-model="participantDialog" title="参会人员" width="500px">
      <el-table :data="participantList" border>
        <el-table-column prop="userId" label="用户ID" width="80" />
        <el-table-column label="姓名" width="120">
          <template #default="{ row }">{{ participantName(row.userId) }}</template>
        </el-table-column>
        <el-table-column label="角色" width="100">
          <template #default="{ row }">{{ row.recorder ? '记录员' : '参会人' }}</template>
        </el-table-column>
        <el-table-column label="确认状态" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.minutesConfirmed" type="success" size="small">已确认</el-tag>
            <el-tag v-else type="info" size="small">未确认</el-tag>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="participantDialog = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- ===== 纪要查看对话框 ===== -->
    <el-dialog v-model="minutesViewDialog" title="会议纪要" width="600px">
      <div style="white-space: pre-wrap;">{{ currentMinutes }}</div>
      <template #footer>
        <el-button @click="minutesViewDialog = false">关闭</el-button>
      </template>
    </el-dialog>

    <WorkflowGuideDialog ref="flowGuideDialog" />
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api } from '../api'
import { useDictionaryStore } from '../stores/dictionary'
import { readSessionUser } from '../utils/sessionUser'
import OrgUserTreeSelect from '../components/OrgUserTreeSelect.vue'
import WorkflowGuideDialog from '../components/WorkflowGuideDialog.vue'

const dictionaryStore = useDictionaryStore()
const labelOf = dictionaryStore.labelOf
const optionsOf = dictionaryStore.optionsOf
const currentUser = readSessionUser(undefined, { id: 2 })

const activeTab = ref('list')
const loading = ref(false)
const rooms = ref([])
const meetings = ref([])
const participatedMeetings = ref([])
const orgTree = ref([])
const userOptions = ref([])
const applicationDialog = ref(false)
const participantDialog = ref(false)
const participantList = ref([])
const minutesViewDialog = ref(false)
const currentMinutes = ref('')
const flowGuideDialog = ref(null)

const form = reactive({
  title: '',
  roomId: null,
  organizerId: currentUser.id || 2,
  startTime: '',
  endTime: '',
  participants: [],
  recorderId: null,
  venueType: '室内',
  meetingType: '国内管理会议',
  accommodationFee: 0,
  mealFee: 0,
  venueFee: 0,
  otherFee: 0,
  budget: 0,
  riskReportUrl: '',
  securityPlanUrl: '',
  emergencyPlanUrl: ''
})

const isLarge = computed(() =>
  (form.venueType === '室内' && form.participants.length > 500) ||
  (form.venueType === '室外' && form.participants.length > 100)
)

function formatDate(dt) {
  if (!dt) return ''
  return dt.replace('T', ' ')
}

function userName(uid) {
  const user = userOptions.value.find(u => u.id === uid)
  return user ? (user.realName || user.username) : `用户${uid}`
}

function participantName(uid) {
  return userName(uid)
}

function getParticipantCount(meeting) {
  // 从缓存的参会人数 map 中获取
  return participantCounts.value[meeting.id] || '-'
}

const participantCounts = ref({})

function isRecorder(meeting) {
  return meeting.recorderId === currentUser.id
}

function isOrganizer(meeting) {
  return meeting.organizerId === currentUser.id
}

function isMyConfirmed(meeting) {
  // 检查当前用户在参会人列表中的确认状态
  const list = participantDetails.value[meeting.id] || []
  const me = list.find(p => p.userId === currentUser.id)
  return me ? me.minutesConfirmed : false
}

const participantDetails = ref({})

const load = async () => {
  loading.value = true
  try {
    const [roomsData, meetingsData, participatedData, treeData, userData] = await Promise.all([
      api.rooms(),
      api.meetings(),
      api.meetingsParticipated().catch(() => []),
      api.orgTree().catch(() => []),
      api.userOptions().catch(() => [])
    ])
    rooms.value = roomsData
    meetings.value = meetingsData
    participatedMeetings.value = participatedData
    orgTree.value = treeData
    userOptions.value = userData
  } catch (e) {
    ElMessage.error('加载数据失败：' + (e.message || '网络异常'))
  } finally {
    loading.value = false
  }
}

const openApplicationDialog = () => {
  form.title = ''
  form.roomId = null
  form.startTime = ''
  form.endTime = ''
  form.participants = []
  form.recorderId = null
  form.venueType = '室内'
  form.meetingType = '国内管理会议'
  form.accommodationFee = 0
  form.mealFee = 0
  form.venueFee = 0
  form.otherFee = 0
  form.budget = 0
  form.riskReportUrl = ''
  form.securityPlanUrl = ''
  form.emergencyPlanUrl = ''
  applicationDialog.value = true
}

const submit = async () => {
  if (form.participants.length === 0) {
    ElMessage.warning('请选择至少一位参会人员')
    return
  }
  if (!form.recorderId) {
    ElMessage.warning('请指定会议记录员')
    return
  }
  try {
    const data = { ...form }
    data.expectedCount = form.participants.length
    await api.createMeeting(data)
    ElMessage.success('会议申请已提交')
    applicationDialog.value = false
    await load()
  } catch (error) {
    ElMessage.error(error.message || '提交失败')
  }
}

const archiveMinutes = async (meeting) => {
  const { value } = await ElMessageBox.prompt('请输入会议纪要', '填写纪要', {
    inputType: 'textarea',
    inputValue: meeting.minutes || ''
  })
  try {
    await api.archiveMeetingMinutes(meeting.id, { minutes: value, signInCount: meeting.expectedCount })
    ElMessage.success('会议纪要已填写，等待参会人确认')
    await load()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

const confirmMinutes = async (meeting) => {
  try {
    await ElMessageBox.confirm('确认该会议纪要内容无误？', '确认纪要')
    await api.confirmMeetingMinutes(meeting.id)
    ElMessage.success('纪要已确认')
    await load()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '确认失败')
    }
  }
}

const publishMeeting = async (meeting) => {
  try {
    await ElMessageBox.confirm('确认将会议纪要发布为通知公告？发布后全校可见。', '发布为公告')
    await api.publishMeeting(meeting.id)
    ElMessage.success('已发布为公告')
    await load()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '发布失败')
    }
  }
}

const archiveDirectly = async (meeting) => {
  try {
    await ElMessageBox.confirm('确认直接归档？归档后纪要仅在内部存档，不对外公开。', '直接归档')
    await api.archiveMeeting(meeting.id)
    ElMessage.success('已归档')
    await load()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '归档失败')
    }
  }
}

const showParticipants = async (meeting) => {
  try {
    participantList.value = await api.meetingParticipants(meeting.id)
    participantDialog.value = true
  } catch (e) {
    ElMessage.error('加载参会人失败')
  }
}

const showConfirmProgress = async (meeting) => {
  try {
    participantList.value = await api.meetingParticipants(meeting.id)
    participantDialog.value = true
  } catch (e) {
    ElMessage.error('加载确认进度失败')
  }
}

const viewMinutes = (meeting) => {
  currentMinutes.value = meeting.minutes || '暂无纪要内容'
  minutesViewDialog.value = true
}

const openFlowGuide = (meeting) => {
  flowGuideDialog.value?.open('meeting', meeting.id)
}

onMounted(load)
</script>

<style scoped>
.rule-note {
  color: #e6a23c;
  font-size: 12px;
}
</style>
```

- [ ] **Step 2: 提交**

```bash
git add -A
git commit -m "feat(meeting): full frontend rewrite with participants, confirmation and publish"
```

---

## Task 7: 前端测试

**Files:**
- Create: `frontend/src/views/Meetings.spec.js`

- [ ] **Step 1: 编写前端测试**

创建 `frontend/src/views/Meetings.spec.js`，按照项目的 source-string 断言模式：

```javascript
import { describe, expect, it } from 'vitest'
import { readFileSync } from 'node:fs'
import { resolve, dirname } from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = dirname(fileURLToPath(import.meta.url))
const source = readFileSync(resolve(__dirname, 'Meetings.vue'), 'utf-8')

describe('Meetings.vue', () => {
  it('has participant selection in application form', () => {
    expect(source).toContain('OrgUserTreeSelect')
    expect(source).toContain('form.participants')
  })

  it('has recorder select', () => {
    expect(source).toContain('记录员')
    expect(source).toContain('form.recorderId')
  })

  it('has participated meetings tab', () => {
    expect(source).toContain('我参与的会议')
    expect(source).toContain('participatedMeetings')
  })

  it('has confirm minutes button', () => {
    expect(source).toContain('confirmMinutes')
    expect(source).toContain('confirm-meeting-minutes')
  })

  it('has publish and archive buttons', () => {
    expect(source).toContain('publishMeeting')
    expect(source).toContain('archiveMeeting')
    expect(source).toContain('发布为公告')
    expect(source).toContain('直接归档')
  })

  it('has confirm progress button', () => {
    expect(source).toContain('确认进度')
    expect(source).toContain('showConfirmProgress')
  })

  it('has minutes_pending and minutes_confirmed status display', () => {
    expect(source).toContain('minutes_pending')
    expect(source).toContain('minutes_confirmed')
  })

  it('validates participants before submit', () => {
    expect(source).toContain('请选择至少一位参会人员')
    expect(source).toContain('请指定会议记录员')
  })

  it('has loading state', () => {
    expect(source).toContain('v-loading')
  })

  it('has empty state', () => {
    expect(source).toContain('el-empty')
  })
})
```

- [ ] **Step 2: 运行前端测试**

Run: `cd frontend && npx vitest run src/views/Meetings.spec.js`
Expected: PASS

- [ ] **Step 3: 运行全部前端测试和构建**

Run: `cd frontend && npx vitest run && npm run build`
Expected: ALL PASS

- [ ] **Step 4: 提交**

```bash
git add -A
git commit -m "test(meeting): add frontend tests for participant features"
```

---

## Task 8: 端到端验证

**Files:** 无新文件

- [ ] **Step 1: 启动后端**

Run: `cd backend && mvn spring-boot:run`

- [ ] **Step 2: 启动前端**

Run: `cd frontend && npm run dev`

- [ ] **Step 3: 手动验证以下流程**

1. 以 `user` 登录
2. 进入「会议管理」→ 点击「会议申请」
3. 填写表单，选择参会人员（如 head, finance），指定记录员（如 head）
4. 提交 → 应成功
5. 切换到 `head` 账号 → 进入审批 → 审批通过
6. 切回 `head` 账号（记录员）→ 会议列表中应显示「填写纪要」按钮
7. 填写纪要 → 状态变为「纪要待确认」
8. 切到「我参与的会议」Tab → 应显示待确认的会议
9. 参会人点击「确认纪要」
10. 所有人确认后 → 状态变为「纪要已确认」
11. 切回 `user`（组织者）→ 应显示「发布为公告」和「直接归档」按钮
12. 点击「发布为公告」→ 去通知公告页面验证公告已创建

- [ ] **Step 4: 运行全部后端测试**

Run: `cd backend && mvn test`
Expected: ALL PASS

- [ ] **Step 5: 运行全部前端测试**

Run: `cd frontend && npx vitest run && npm run build`
Expected: ALL PASS

- [ ] **Step 6: 最终提交**

```bash
git add -A
git commit -m "feat(meeting): complete participant management, minutes confirmation and publish"
```
