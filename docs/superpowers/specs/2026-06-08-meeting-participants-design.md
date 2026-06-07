# 会议参会人员管理 & 纪要确认公示 设计文档

**日期**：2026-06-08
**范围**：会议管理模块增强 — 参会人选定、通知、记录员纪要填写、全员确认、公示/归档

---

## 1. 需求概述

当前会议管理模块无法选定参会人员，会议纪要由组织者直接归档，无确认环节。本次增强实现：

1. 会议申请时从系统全部用户中选择参会人，并指定其中一人为记录员
2. 会议审批通过后自动通知所有参会人
3. 记录员填写会议纪要，参会人全员确认
4. 组织者决定是否将纪要发布为通知公告，或直接归档

### 1.1 完整流程

```
① 组织者申请会议（选参会人 + 指定记录员）
    ↓
② 审批流（部门负责人 / 保卫部等）
    ↓ 审批通过
③ 自动给所有参会人发送站内通知
    ↓
④ 记录员填写纪要 → 状态变为 minutes_pending（纪要待确认）
    ↓
⑤ 参会人全员确认纪要 → 状态变为 minutes_confirmed（纪要已确认）
    ↓
⑥ 组织者选择：
   ├── 「发布为公告」→ 自动创建通知公告 → archived
   └── 「直接归档」→ 仅内部存档 → archived
```

### 1.2 状态流转

```
draft → pending_dept → approved → minutes_pending → minutes_confirmed → archived
                                ↗ (大型活动)                     ↘
              pending_security →                                  发布为公告
```

| 状态 | 说明 |
|------|------|
| `draft` | 草稿（暂未使用，当前直接提交） |
| `pending_dept` | 待部门负责人审批 |
| `pending_security` | 大型活动待保卫部审批 |
| `approved` | 审批通过，等待记录员填写纪要 |
| `minutes_pending` | 纪要已填写，等待参会人全员确认 |
| `minutes_confirmed` | 全员确认完成，等待组织者决定归档或公示 |
| `rejected` | 审批退回 |

### 1.3 角色操作矩阵

| 角色 | 操作 |
|------|------|
| 组织者 | 申请会议、选参会人、指定记录员、催办未确认参会人、决定公示/归档 |
| 记录员 | 审批通过后填写会议纪要 |
| 参会人 | 收到通知、查看纪要、确认纪要 |
| 审批人 | 审批会议（不变） |

---

## 2. 数据层设计

### 2.1 新增模型：MeetingParticipant

```java
package com.university.oms.model;

public class MeetingParticipant extends BaseEntity {
    private Long meetingId;       // 会议 ID
    private Long userId;          // 参会人 ID
    private boolean recorder;     // 是否为记录员
    private boolean minutesConfirmed; // 纪要是否已确认
    private LocalDateTime confirmedAt; // 确认时间
}
```

### 2.2 Meeting 模型新增字段

| 字段 | 类型 | 说明 |
|------|------|------|
| `recorderId` | Long | 记录员用户 ID（冗余，方便查询） |

### 2.3 InMemoryDatabase 新增

- `ConcurrentHashMap<Long, MeetingParticipant> participants()` — 以自增 ID 为 key
- `List<MeetingParticipant> meetingParticipants(Long meetingId)` — 按会议查询参会人

### 2.4 MySQL Schema 新增

```sql
CREATE TABLE IF NOT EXISTS oa_meeting_participant (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    meeting_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    is_recorder TINYINT(1) DEFAULT 0,
    minutes_confirmed TINYINT(1) DEFAULT 0,
    confirmed_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_meeting_user (meeting_id, user_id)
);
```

---

## 3. API 设计

### 3.1 修改现有接口

#### `POST /api/meetings` — 会议申请（新增字段）

请求体新增：

```json
{
  "title": "...",
  "roomId": 1,
  "organizerId": 2,
  "startTime": "...",
  "endTime": "...",
  "venueType": "室内",
  "meetingType": "国内管理会议",
  "budget": 3000,
  "participants": [2, 3, 5, 8],   // 新增：参会人 ID 列表
  "recorderId": 3                  // 新增：记录员 ID（必须在参会人中）
}
```

**校验规则**：
- `participants` 不能为空，至少 1 人
- `recorderId` 必须在 `participants` 中
- `expectedCount` 从 `participants.length` 自动得出，不再由前端传入

#### `POST /api/meetings/{id}/minutes` — 纪要归档（权限变更）

- 仅记录员可调用（原为组织者）
- 提交后状态变为 `minutes_pending`（原为直接 `archived`）

请求体不变：

```json
{
  "minutes": "会议纪要正文...",
  "signInCount": 8
}
```

### 3.2 新增接口

#### `GET /api/meetings/participated` — 我参与的会议

返回当前登录人作为参会人的会议列表，含纪要确认状态。

响应示例：

```json
[
  {
    "meetingId": 1001,
    "title": "系统试运行培训会",
    "startTime": "2026-06-22T09:00:00",
    "status": "minutes_pending",
    "isRecorder": false,
    "minutesConfirmed": false,
    "minutes": "会议纪要正文..."
  }
]
```

#### `POST /api/meetings/{id}/confirm-minutes` — 参会人确认纪要

- 只有该会议的参会人可以调用
- 每人只能确认一次
- 当所有参会人均确认后，会议状态自动变为 `minutes_confirmed`

#### `GET /api/meetings/{id}/participants` — 查询会议参会人

返回参会人列表及确认状态，供组织者查看进度。

响应示例：

```json
[
  { "userId": 2, "realName": "张三", "recorder": false, "minutesConfirmed": true, "confirmedAt": "2026-06-22T15:30:00" },
  { "userId": 3, "realName": "李四", "recorder": true, "minutesConfirmed": true, "confirmedAt": "2026-06-22T14:00:00" },
  { "userId": 5, "realName": "王五", "recorder": false, "minutesConfirmed": false, "confirmedAt": null }
]
```

#### `POST /api/meetings/{id}/remind-participant/{userId}` — 催办未确认参会人

- 仅组织者可调用
- 给指定参会人发送站内通知

#### `POST /api/meetings/{id}/publish` — 发布为公告

- 仅组织者可调用
- 会议状态必须为 `minutes_confirmed`
- 自动创建一条通知公告（标题、内容从会议信息生成）
- 会议状态变为 `archived`

#### `POST /api/meetings/{id}/archive` — 直接归档

- 仅组织者可调用
- 会议状态必须为 `minutes_confirmed`
- 不创建公告，直接归档
- 会议状态变为 `archived`

---

## 4. 后端变更清单

### 4.1 新增文件

| 文件 | 说明 |
|------|------|
| `model/MeetingParticipant.java` | 参会人模型 |
| `dto/MeetingParticipantRequest.java` | 参会人相关请求 DTO |

### 4.2 修改文件

| 文件 | 变更 |
|------|------|
| `model/Meeting.java` | 新增 `recorderId` 字段 |
| `dto/MeetingRequest.java` | 新增 `participants`（`List<Long>`）、`recorderId` 字段 |
| `dto/MeetingMinutesRequest.java` | 无变更 |
| `service/MeetingService.java` | create() 保存参会人、通知参会人；archiveMinutes() 权限改为记录员、状态改为 minutes_pending；新增 confirmMinutes()、participants()、remindParticipant()、publish()、archive() |
| `controller/MeetingController.java` | 新增 6 个端点 |
| `repository/InMemoryDatabase.java` | 新增 participants 集合和辅助方法 |
| `repository/JdbcDataPersistence.java` | 新增参会人表的 CRUD |
| `repository/MysqlDataLoader.java` | 新增从 oa_meeting_participant 加载数据 |
| `service/WorkflowService.java` | advanceFlow() 支持 minutes_pending、minutes_confirmed 状态 |
| `design/ApprovalFlowConfig.java` | 如需要，新增状态的角色映射 |

### 4.3 MeetingService 关键逻辑

**create() 变更**：
1. 校验 participants 不为空
2. 校验 recorderId 在 participants 中
3. 从 participants.size() 自动设置 expectedCount
4. 保存 Meeting 后，循环创建 MeetingParticipant 记录
5. 走审批流不变

**审批通过后通知**：
- 在 advanceFlow() 中检测 meeting 类型进入 approved 状态时
- 给所有参会人发送站内通知：「您被邀请参加会议：{title}，时间为 {startTime}」

**archiveMinutes() 变更**：
1. 权限校验：调用人必须是记录员
2. 保存纪要内容
3. 状态变为 `minutes_pending`
4. 给所有参会人发送通知：「会议《{title}》纪要已填写，请确认」

**confirmMinutes() 新增**：
1. 校验调用人是参会人
2. 标记该参会人的 minutesConfirmed = true
3. 检查是否全员确认，是则将会议状态改为 `minutes_confirmed`
4. 全员确认后通知组织者

**publish() 新增**：
1. 校验状态为 `minutes_confirmed`、调用人为组织者
2. 调用 AnnouncementService 创建公告
3. 状态变为 `archived`

**archive() 新增**：
1. 校验状态为 `minutes_confirmed`、调用人为组织者
2. 状态变为 `archived`

---

## 5. 前端变更清单

### 5.1 Meetings.vue 变更

**会议申请对话框**：
- 新增「参会人员」区域，使用已有的 `OrgUserTreeSelect` 组件
- 新增「记录员」下拉框，选项从已选参会人中生成
- 移除「预计人数」手动输入，自动从参会人数得出

**会议列表**：
- 新增「参会人」列（显示人数，点击查看详情弹窗）
- 「纪要归档」按钮：仅在 `approved` 状态且当前用户是记录员时显示
- 新增「确认纪要」按钮：参会人在 `minutes_pending` 状态时显示
- 新增「发布为公告」/「直接归档」按钮：组织者在 `minutes_confirmed` 状态时显示
- 新增「查看确认进度」按钮：组织者在 `minutes_pending` / `minutes_confirmed` 状态时显示
- 状态列新增 `minutes_pending`、`minutes_confirmed` 的中文标签

**新增 Tab：「我参与的会议」**：
- 显示我作为参会人的会议列表
- 纪要待确认的会议可展开查看纪要内容
- 提供「确认纪要」按钮

### 5.2 api.js 新增方法

```javascript
meetingsParticipated: () => http.get('/meetings/participated'),
meetingParticipants: (id) => http.get(`/meetings/${id}/participants`),
confirmMeetingMinutes: (id) => http.post(`/meetings/${id}/confirm-minutes`),
remindParticipant: (meetingId, userId) => http.post(`/meetings/${meetingId}/remind-participant/${userId}`),
publishMeeting: (id) => http.post(`/meetings/${id}/publish`),
archiveMeeting: (id) => http.post(`/meetings/${id}/archive`),
```

### 5.3 字典补充

在 `business_status` 字典中新增：
- `minutes_pending` → 纪要待确认
- `minutes_confirmed` → 纪要已确认

---

## 6. 测试要点

### 6.1 后端单元测试

| 测试场景 | 预期结果 |
|----------|----------|
| 会议申请参会人为空 | 抛出异常 |
| 记录员不在参会人中 | 抛出异常 |
| 正常申请含参会人 | 创建参会人记录，expectedCount 自动设置 |
| 审批通过 | 所有参会人收到通知 |
| 记录员填写纪要 | 状态变为 minutes_pending |
| 非记录员填写纪要 | 抛出异常 |
| 参会人确认纪要 | 标记已确认 |
| 非参会人确认纪要 | 抛出异常 |
| 最后一人确认 | 状态自动变为 minutes_confirmed，通知组织者 |
| 发布为公告 | 创建公告记录，状态变为 archived |
| 直接归档 | 不创建公告，状态变为 archived |
| 催办 | 指定参会人收到通知 |

### 6.2 前端测试

| 测试场景 | 预期结果 |
|----------|----------|
| 未选参会人提交 | 提示选择参会人 |
| 已选参会人后记录员下拉有选项 | 可选择 |
| 参会人选择器勾选/取消 | 预计人数自动更新 |
| 我参与的会议 Tab | 显示参与的会议 |
| 确认纪要按钮 | 点击后状态更新 |

---

## 7. 影响范围

| 影响项 | 说明 |
|--------|------|
| 现有会议数据 | 无参会人记录，列表显示为空，不影响现有功能 |
| 审批流 | 不变，仅 approved 后的行为增加 |
| 通知公告模块 | 新增「发布为公告」会调用 AnnouncementService.create |
| 权限 | 不变，使用现有的角色体系 |
