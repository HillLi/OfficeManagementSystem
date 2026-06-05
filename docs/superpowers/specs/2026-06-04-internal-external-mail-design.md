# 站内信与真实邮件发送设计

## 背景

系统需要支持用户在办公系统内发送邮件，可从组织树中选择收件人和抄送人。发送后，收件人应能在系统内看到站内信，同时系统也要根据用户资料中的邮箱地址发送真实邮件。

当前项目已经具备用户、部门、通知和权限基础：

- `sys_user` 表已有 `email` 字段。
- `sys_dept` 支持 `parent_id`，可以组织成部门树。
- `WorkflowService` 已有站内通知能力。
- 前端已有用户、部门基础数据接口和 Element Plus 组件体系。

本设计在现有能力上扩展，不引入复杂消息队列。真实邮件发送失败时，站内信仍然保留，失败原因记录到收件人状态中。

## 范围

本轮实现包含：

- 管理员创建用户时强制填写邮箱。
- 用户资料模型和持久化保存邮箱。
- 组织树选人接口，返回部门和用户的树形结构。
- 站内邮件发送、收件箱、发件箱、详情、已读。
- 支持收件人和抄送人。
- 发送站内信后尝试发送真实邮箱。
- 记录每个收件人的真实邮件发送状态。
- 邮件发送后生成系统通知。

本轮不包含：

- 邮件附件。
- 富文本编辑器。
- 定时发送。
- 管理员查看所有用户邮件正文。
- 外部邮箱收信同步。

## 方案选择

### 方案一：只做站内信

实现最快，完全依赖数据库和系统通知，不需要 SMTP 配置。但不能满足真实邮箱提醒要求。

### 方案二：只发真实邮箱

实现表面简单，但系统内没有收件箱、已读、抄送记录和审计痕迹。SMTP 失败时用户也可能完全收不到。

### 方案三：站内信为主，真实邮箱为外部提醒

推荐采用。发送时先保存站内信和收件人记录，再尝试发送真实邮箱。站内信保证业务可达，真实邮件增强提醒能力。每个收件人的外部邮件状态单独记录，便于页面提示和后续重试。

## 数据设计

### 用户邮箱

`sys_user.email` 已存在。后端模型需要补齐：

- `User.email`
- `CreateUserRequest.email`
- `UpdateUserRequest.email`

创建用户时邮箱必填，更新用户时如果传入邮箱则校验格式。

### 邮件主表

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
```

### 邮件收件人表

```sql
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

`recipient_type` 取值：

- `to`：收件人。
- `cc`：抄送人。

`email_status` 取值：

- `pending`：待发送。
- `sent`：真实邮箱发送成功。
- `failed`：真实邮箱发送失败。
- `skipped`：未启用外部邮件或用户邮箱不可用。

## 后端设计

### 模型和 DTO

新增模型：

- `MailMessage`
- `MailRecipient`
- `OrgTreeNode`

新增请求：

- `MailSendRequest`

请求结构：

```json
{
  "subject": "本周工作安排",
  "content": "请各位老师周五前提交本周总结。",
  "toUserIds": [2, 3],
  "ccUserIds": [5]
}
```

校验规则：

- `subject` 必填，长度不超过 255。
- `content` 必填。
- `toUserIds` 至少有一个用户。
- `toUserIds` 和 `ccUserIds` 去重。
- 收件人和抄送人不能重复；如果重复，以收件人为准，避免同一用户收到两条记录。
- 所有用户 ID 必须存在。
- 创建用户时邮箱必填且格式合法。

### 接口

```text
GET /api/org/tree
返回部门-用户组织树

POST /api/mails
发送站内信并尝试发送真实邮件

GET /api/mails/inbox
当前用户收件箱

GET /api/mails/sent
当前用户发件箱

GET /api/mails/{id}
邮件详情

POST /api/mails/{id}/read
当前用户将邮件标记为已读

POST /api/mails/{id}/retry-email
发件人或管理员重试失败的真实邮件
```

### 组织树

组织树由 `sys_dept.parent_id` 和用户 `dept_id` 生成。节点结构：

```json
{
  "id": "dept-4",
  "label": "软件学院",
  "type": "dept",
  "deptId": 4,
  "children": [
    {
      "id": "user-2",
      "label": "张老师",
      "type": "user",
      "userId": 2,
      "email": "zhang@example.com"
    }
  ]
}
```

前端只允许选择 `type=user` 的叶子节点。部门节点用于展开和定位人员。

### 邮件发送流程

`MailService.send(request)`：

1. 读取当前登录用户作为发件人。
2. 校验主题、正文、收件人和抄送人。
3. 保存 `MailMessage`。
4. 为收件人和抄送人保存 `MailRecipient`。
5. 对每个收件人调用现有通知能力，生成一条系统通知。
6. 如果启用外部邮件，读取用户邮箱并调用 `EmailSenderService`。
7. 根据发送结果更新对应 `MailRecipient.emailStatus`、`emailError` 和 `emailSentAt`。
8. 返回邮件详情和每个收件人的发送状态。

真实邮件发送失败不回滚站内信。接口返回成功，同时提示哪些用户的外部邮箱失败。

### 真实邮件服务

新增 `EmailSenderService`，封装 Spring Mail：

- `sendMail(toEmail, subject, content)`
- `isEnabled()`

配置：

```properties
spring.mail.host=smtp.qq.com
spring.mail.port=587
spring.mail.username=example@qq.com
spring.mail.password=邮箱授权码
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

oms.mail.external-enabled=true
oms.mail.from-name=高校办公管理系统
```

外部邮件正文包含：

- 邮件主题。
- 发件人姓名。
- 正文内容。
- 系统提示：请登录高校办公管理系统查看站内信和处理后续事项。

### 权限

- 登录用户可以发送邮件。
- 用户只能查看自己收到的邮件。
- 用户只能查看自己发出的邮件。
- 邮件详情中，收件人可以看到自己收到的邮件正文；发件人可以看到完整收件人和抄送人状态。
- 管理员默认不读取所有邮件正文，避免越权查看私人内容。
- `retry-email` 仅发件人和管理员可用。

## 前端设计

新增页面 `Mails.vue`，菜单名称为“邮件中心”。

页面包含三个视图：

- 收件箱：显示主题、发件人、收件类型、已读状态、发送时间。
- 已发送：显示主题、收件人摘要、外部邮件发送状态、发送时间。
- 写邮件：主题、正文、收件人组织树、抄送人组织树。

新增通用组件 `OrgUserTreeSelect.vue`：

- 使用 Element Plus 树形控件。
- 部门节点可展开，不作为最终值提交。
- 用户节点可勾选。
- 选中后用标签展示人员姓名和部门。
- 收件人和抄送人复用同一组件。

发送后提示：

- 全部成功：`站内信和邮箱均已发送`。
- 部分邮箱失败：`站内信已发送，部分邮箱发送失败，可在发件箱查看详情`。
- 外部邮件未启用：`站内信已发送，外部邮箱未启用`。

## 错误处理

- 收件人为空：拒绝发送。
- 用户不存在：拒绝发送。
- 创建用户未填邮箱：拒绝保存。
- 邮箱格式不合法：拒绝保存。
- SMTP 配置缺失：站内信发送成功，外部状态记为 `skipped`。
- SMTP 发送异常：站内信发送成功，外部状态记为 `failed`，记录异常摘要。
- 用户查看无权限邮件：返回 `403`。

## 测试计划

后端集成测试：

- 管理员创建用户时未填邮箱返回失败。
- 管理员创建用户时邮箱格式非法返回失败。
- 组织树返回部门和用户节点。
- 发送邮件后生成 `MailMessage` 和 `MailRecipient`。
- 收件人和抄送人收到系统通知。
- 当前用户收件箱只返回自己的邮件。
- 当前用户发件箱只返回自己发送的邮件。
- 收件人查看详情后可标记已读。
- 外部邮件关闭时状态为 `skipped`。
- 外部邮件发送异常时状态为 `failed`，站内信仍保存。

前端测试：

- 写邮件页面可以从组织树选择收件人和抄送人。
- 发送请求包含 `toUserIds` 和 `ccUserIds`。
- 收件箱展示未读状态。
- 发件箱展示每个收件人的外部邮件发送状态。

## 实施顺序

1. 补齐用户邮箱字段和强制校验。
2. 增加邮件数据表、模型和持久化。
3. 增加组织树接口。
4. 增加站内邮件接口和业务服务。
5. 接入系统通知。
6. 接入 Spring Mail 和外部邮件状态记录。
7. 增加前端邮件中心和组织树选人组件。
8. 增加后端集成测试和前端组件测试。

## 验收标准

- 管理员新增用户时必须填写合法邮箱。
- 发邮件时可以通过“软件学院 - 用户”这样的树形结构选择人员。
- 邮件支持收件人和抄送人。
- 收件人登录系统后能在收件箱看到站内信。
- 收件人能收到系统通知。
- 配置 SMTP 后，真实邮箱可以收到同一封邮件。
- SMTP 失败不会导致站内信丢失。
- 发件箱能看到每个收件人的真实邮件发送状态。
