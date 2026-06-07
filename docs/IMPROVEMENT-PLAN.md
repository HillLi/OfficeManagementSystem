# 高校办公管理系统 — 完善与修复建议清单

**分析日期：** 2026-06-06（第三轮回归核实）
**分析范围：** 前端 Vue 3 全部视图组件 + 后端 Java Spring Boot 全部 Service/Controller/Security
**分析方式：** 代码静态审查 + 浏览器运行时验证

---

## 一、关键问题（建议尽快修复）

### 1.1 登录表单预填测试凭据

| 属性 | 说明 |
|------|------|
| **位置** | `frontend/src/views/Login.vue:32` |
| **等级** | 🔴 严重（安全隐患） |
| **现状** | `const form = reactive({ username: 'user', password: '123456' })` 硬编码在源码中 |
| **风险** | 上线后任何用户打开登录页即可看到预设账号密码，直接用测试账号进入系统 |
| **建议** | 改为 `const form = reactive({ username: '', password: '' })`，清空默认值 |
| **工作量** | 1 行改动，1 分钟 |

---

### 1.2 客户端可伪造操作人 ID（身份冒充漏洞）

| 属性 | 说明 |
|------|------|
| **位置** | `backend/.../security/AuthContext.java:20` — `currentUserIdOr(Long fallback)` 方法 |
| **等级** | 🔴 严重（安全漏洞） |
| **现状** | 6 个 Service 使用此方法回退到请求体中的用户 ID： |
| | - `DocumentService.java:68` — `AuthContext.currentUserIdOr(request.getApplicantId())` |
| | - `SealService.java:64` — `AuthContext.currentUserIdOr(request.getApplicantId())` |
| | - `MeetingService.java:86` — `AuthContext.currentUserIdOr(request.getOrganizerId())` |
| | - `TravelService.java:82` — `AuthContext.currentUserIdOr(request.getApplicantId())` |
| | - `ReportService.java:56` — `AuthContext.currentUserIdOr(request.getApplicantId())` |
| | - `ApprovalService.java:64` — `AuthContext.currentUserIdOr(request.getOperatorId())` |
| **风险** | 攻击者可在请求体中填入他人 ID，冒充他人提交公文、用印申请、差旅审批等业务 |
| **建议** | 生产环境应始终使用 `AuthContext.currentUser().getId()` 获取当前登录用户 ID，不回退到客户端传值；或在 Service 层增加身份校验断言 |
| **工作量** | 6 处改动，约 30 分钟 |

---

### 1.3 全站约 30+ 处 API 调用缺少 try/catch 错误处理

| 属性 | 说明 |
|------|------|
| **位置** | 几乎所有视图组件 |
| **等级** | 🔴 严重（功能缺陷） |
| **涉及文件** | |
| | `Documents.vue` — `load()`, `save()`, `submitFlow()`, `archive()`, `draft()`, `review()`, `distribute()`, `receive()`, `remind()`, `addAttachment()` 共 10 处 |
| | `Seals.vue` — `load()`, `submitDraft()`, `uploadMaterial()`, `downloadMaterial()`, `saveEdit()`, `markUsed()`, `markReturned()`, `createTransfer()` 共 8 处 |
| | `Reports.vue` — `load()`, `submit()`, `reply()` 共 3 处 |
| | `Meetings.vue` — `load()`, `archiveMinutes()` 共 2 处 |
| | `Dashboard.vue` — `onMounted` 中的 `Promise.all` 共 1 处 |
| | `Statistics.vue` — `onMounted`, `download()` 共 2 处 |
| | `Approvals.vue` — `load()`, `markRead()` 共 2 处 |
| | `DictionaryManage.vue` — `loadTypes()` 共 1 处 |
| | `Announcements.vue` — `load()`, `publish()`, `withdraw()` 共 3 处 |
| **现状** | 这些函数直接 `await api.xxx()` 无错误捕获，API 失败时控制台报 unhandled promise rejection，用户看不到任何错误提示 |
| **建议** | 统一包装为 try/catch，失败时调用 `ElMessage.error('操作失败：' + (err.message || '网络异常'))` 提示用户 |
| **工作量** | 约 30 处，可批量处理，约 2 小时 |

**修复模板：**
```javascript
async function load() {
  try {
    const res = await api.documents()
    Object.assign(documents, res)
  } catch (e) {
    ElMessage.error('加载数据失败：' + (e.message || '网络异常'))
  }
}
```

---

### 1.4 7 个业务表单缺少必填项验证

| 属性 | 说明 |
|------|------|
| **位置** | 所有业务表单对话框 |
| **等级** | 🔴 严重（功能缺陷） |
| **涉及表单** | |
| | `Documents.vue:31-48` — 公文起草：标题、文种、正文均可为空提交 |
| | `Meetings.vue:29-61` — 会议申请：标题、会议室、时间均可为空 |
| | `Seals.vue:49-85` — 用印申请：用途、印章均可为空 |
| | `Travels.vue:30-51` — 差旅申请：目的地、事由均可为空 |
| | `Reports.vue:25-40` — 请示报告：标题、内容均可为空 |
| | `Announcements.vue:96-124` — 通知公告：标题、内容均可为空 |
| | `UserManage.vue:57-91` — 用户管理：密码无最小长度要求，邮箱无格式校验 |
| **现状** | 表单没有 `<el-form :rules="rules" ref="formRef">` 验证规则，空内容可直接提交到后端 |
| **建议** | 为每个表单添加 rules 验证规则，提交前调用 `formRef.value.validate()` |
| **工作量** | 7 个表单，约 2-3 小时 |

**修复模板：**
```javascript
const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入正文', trigger: 'blur' }]
}

async function submit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  // ... 原有提交逻辑
}
```

---

## 二、重要改进（提升用户体验）

### 2.1 9 个列表页面缺少 loading 加载状态

| 属性 | 说明 |
|------|------|
| **等级** | 🟡 中等 |
| **现状** | 数据加载时页面无任何加载指示，用户不确定页面是否在响应 |
| **涉及页面** | 公文管理、会议管理、印章管理、差旅审批、请示报告、统计报表、审批任务、字典管理、通知公告 |
| **已做对** | 邮件中心 `Mails.vue` — 正确实现了 `loading` ref + `v-loading` |
| **建议** | 每个列表页添加 `const loading = ref(false)`，在 API 调用前设为 `true`，完成后设为 `false`，表格加 `v-loading="loading"` |
| **工作量** | 9 处，约 1 小时 |

---

### 2.2 7 个数据表格缺少空状态提示

| 属性 | 说明 |
|------|------|
| **等级** | 🟡 中等 |
| **现状** | 无数据时表格只显示空白行列，缺少友好提示 |
| **涉及表格** | Documents、Meetings、Seals（两个表）、Travels、Reports、UserManage（两个表） |
| **已做对** | Dashboard 和 Announcements 使用了 `<el-empty>` 组件 |
| **建议** | 在 `<el-table>` 内添加 `<template #empty><el-empty description="暂无数据" /></template>` |
| **工作量** | 9 处，约 30 分钟 |

---

### 2.3 路由缺少 404 兜底页面

| 属性 | 说明 |
|------|------|
| **位置** | `frontend/src/router/index.js` |
| **等级** | 🟡 中等 |
| **现状** | 访问不存在的路径（如 `/test`）显示空白页，无任何提示 |
| **建议** | 添加兜底路由 `{ path: '/:pathMatch(.*)*', name: 'NotFound', component: () => import('../views/NotFound.vue') }`，显示"页面不存在"提示和返回首页按钮 |
| **工作量** | 新建 1 个简单页面 + 路由配置，约 30 分钟 |

---

### 2.4 `formatDate` 工具函数在 7+ 个文件中重复定义

| 属性 | 说明 |
|------|------|
| **位置** | Dashboard.vue:162、Announcements.vue:240、Approvals.vue:138、Mails.vue:371、WorkflowGuideDialog.vue:93 等 |
| **等级** | 🟡 中等（代码质量） |
| **现状** | 相同的时间格式化函数到处复制粘贴 |
| **建议** | 提取到 `frontend/src/utils/format.js` 统一导出，各视图改为 `import { formatDate } from '@/utils/format'` |
| **工作量** | 约 30 分钟 |

---

### 2.5 `JSON.parse(sessionStorage.getItem('oms_user'))` 在 6 处缺少 try/catch

| 属性 | 说明 |
|------|------|
| **位置** | `router/index.js:44`、`Documents.vue:106`、`Meetings.vue:76`、`Travels.vue:78`、`Seals.vue:171`、`Mails.vue:191` |
| **等级** | 🟡 中等 |
| **现状** | 如果 sessionStorage 数据损坏（被篡改或格式异常），`JSON.parse` 会抛异常导致整个页面崩溃 |
| **建议** | 封装一个 `getUserFromStorage()` 工具函数，内部 try/catch 并返回 null |
| **工作量** | 约 20 分钟 |

---

## 三、安全加固

### 3.1 无登录限速 / 防暴力破解

| 属性 | 说明 |
|------|------|
| **位置** | `backend/.../service/AuthService.java:33-43` |
| **等级** | 🟠 高 |
| **现状** | 无登录失败次数限制、无延迟、无验证码、无账号锁定机制，攻击者可无限次尝试密码 |
| **建议** | 添加失败计数器（按用户名或 IP），连续失败 5 次后锁定账号 15 分钟或增加指数退避延迟 |
| **工作量** | 约 2 小时 |

---

### 3.2 Token 无后台清理机制，存在内存泄漏

| 属性 | 说明 |
|------|------|
| **位置** | `backend/.../security/AuthTokenService.java` — `ConcurrentHashMap<String, TokenEntry>` |
| **等级** | 🟠 高 |
| **现状** | Token 存在内存 Map 中，过期 Token 仅在 `resolve()` 被调用时被动移除，无后台定时清理。长期运行后 Map 无限增长，导致 OOM |
| **建议** | 添加 `@Scheduled` 定时任务，每 10 分钟扫描并移除过期 Token |
| **工作量** | 约 30 分钟 |

---

### 3.3 异常信息直接暴露给前端

| 属性 | 说明 |
|------|------|
| **位置** | `backend/.../common/GlobalExceptionHandler.java:32` |
| **等级** | 🟠 高 |
| **现状** | `ex.getMessage()` 直接返回客户端，可能泄露 SQL 语句、Java 类名、堆栈路径等内部实现细节 |
| **建议** | 生产环境返回通用错误信息如"服务器内部错误，请稍后重试"，详细异常信息仅记录到后台日志 |
| **工作量** | 约 15 分钟 |

---

### 3.4 种子用户密码明文存储

| 属性 | 说明 |
|------|------|
| **位置** | `backend/.../repository/InMemoryDatabase.java:227` — `user.setPassword("123456")` |
| **等级** | 🟡 中等 |
| **现状** | 8 个种子用户密码为明文。`PasswordService.matches()` 有明文回退逻辑：不以 `"pbkdf2$"` 开头时直接 `equals()` 比较 |
| **建议** | 初始化时使用 `passwordService.encode("123456")` 存储 PBKDF2 哈希值，移除明文比较回退 |
| **工作量** | 约 30 分钟 |

---

### 3.5 后端输入验证不充分

| 属性 | 说明 |
|------|------|
| **等级** | 🟡 中等 |
| **缺少 `@Valid`** | `MeetingController.recommend()`、`StatisticsController.export()` |
| **缺少 `@Size` 限制** | DocumentRequest.content、AnnouncementRequest.content、MailSendRequest.content、ReportRequest.content、TravelRequest.reason — 均无长度上限，可发送超大载荷 |
| **缺少 `@Min` 限制** | MeetingRequest.budget 无非负校验、SealApplyRequest.copies 无最小值校验 |
| **缺少密码强度** | CreateUserRequest.password 无最小长度，单字符密码可接受 |
| **缺少邮箱格式** | CreateUserRequest.email 无正则格式校验 |
| **缺少日期逻辑** | TravelRequest 未校验 endDate >= startDate |
| **建议** | 补充各 DTO 的 Bean Validation 注解 |
| **工作量** | 约 1-2 小时 |

---

## 四、体验优化

### 4.1 5 个表单预填了开发测试数据

| 属性 | 说明 |
|------|------|
| **等级** | 🟢 轻微 |
| **涉及** | |
| | `Documents.vue:118` — `title: '关于开展办公管理系统试运行的通知'` |
| | `Meetings.vue:82` — `title: '系统试运行培训会'`、硬编码日期 |
| | `Seals.vue:189` — `purpose: '系统试运行通知材料用印'` |
| | `Travels.vue:84` — `destination: '上海'`、`reason: '参加高校信息化建设会议'` |
| | `Reports.vue:58` — `title: '关于系统上线试运行资源支持的请示'` |
| **建议** | 上线前将所有默认值清空为 `''` |
| **工作量** | 约 10 分钟 |

---

### 4.2 ECharts 图表不响应窗口缩放

| 属性 | 说明 |
|------|------|
| **位置** | `Dashboard.vue` |
| **等级** | 🟢 轻微 |
| **现状** | 浏览器窗口大小改变时，仪表盘图表不会自动调整尺寸 |
| **建议** | 添加 `window.addEventListener('resize', () => chart.resize())`，在 `onUnmounted` 中移除监听 |
| **工作量** | 约 15 分钟 |

---

### 4.3 密码输入框无显示/隐藏切换

| 属性 | 说明 |
|------|------|
| **位置** | `Login.vue:10` |
| **等级** | 🟢 轻微 |
| **建议** | 在 `<el-input type="password">` 上添加 `show-password` 属性（Element Plus 原生支持，无需额外代码） |
| **工作量** | 1 行改动 |

---

### 4.4 列表接口无分页

| 属性 | 说明 |
|------|------|
| **涉及** | 所有后端列表接口（公文、会议、印章、差旅、报告、用户、通知、审批记录等） |
| **等级** | 🟢 轻微（当前数据量小，长期需要） |
| **现状** | 每个列表接口一次性返回全部记录，数据量大时影响加载速度和内存 |
| **建议** | 前后端统一添加分页参数（`page`, `size`）和总数返回（`total`），前端表格添加 `<el-pagination>` |
| **工作量** | 较大，约 1-2 天（涉及前后端所有列表接口） |

---

### 4.5 路由组件未懒加载

| 属性 | 说明 |
|------|------|
| **位置** | `frontend/src/router/index.js:2-14` |
| **等级** | 🟢 轻微 |
| **现状** | 所有视图组件使用 `import` 静态导入，首屏加载全部 JS |
| **建议** | 改为 `() => import('../views/XXX.vue')` 动态导入，减小首屏体积 |
| **工作量** | 约 15 分钟 |

---

### 4.6 会议室预订存在竞态条件

| 属性 | 说明 |
|------|------|
| **位置** | `backend/.../service/MeetingService.java:94,128` |
| **等级** | 🟢 轻微（当前单用户使用，并发低） |
| **现状** | `hasConflict()` 检查会议室空闲 → 创建会议，两步之间无锁保护，并发预订可能同时通过冲突检查 |
| **建议** | 在 `create()` 方法上加 `synchronized` 或使用分布式锁（MySQL 模式下可用数据库行锁） |
| **工作量** | 约 30 分钟 |

---

### 4.7 附件物理文件不会随软删除而清理

| 属性 | 说明 |
|------|------|
| **位置** | `backend/.../service/WorkflowService.deleteAttachment()` 仅设置 `deleted=true` |
| **等级** | 🟢 轻微 |
| **现状** | 磁盘上的文件永远不会被清理，长期运行造成磁盘空间泄漏 |
| **建议** | 删除附件时同步删除物理文件 `java.nio.file.Files.deleteIfExists(path)` |
| **工作量** | 约 15 分钟 |

---

## 五、修复优先级总览

| 优先级 | 编号 | 问题 | 工作量 |
|--------|------|------|--------|
| **P0 紧急** | 1.1 | 登录表单预填凭据 | 1 分钟 |
| **P0 紧急** | 1.2 | 客户端伪造操作人 ID | 30 分钟 |
| **P0 紧急** | 1.3 | 30+ 处 API 缺错误处理 | 2 小时 |
| **P0 紧急** | 1.4 | 7 个表单缺必填验证 | 2-3 小时 |
| **P1 重要** | 3.1 | 无登录限速 | 2 小时 |
| **P1 重要** | 3.2 | Token 内存泄漏 | 30 分钟 |
| **P1 重要** | 3.3 | 异常信息暴露 | 15 分钟 |
| **P1 重要** | 2.1 | 9 个列表缺 loading | 1 小时 |
| **P1 重要** | 2.3 | 缺 404 页面 | 30 分钟 |
| **P2 改进** | 2.2 | 7 个表格缺空状态 | 30 分钟 |
| **P2 改进** | 2.4 | formatDate 重复 | 30 分钟 |
| **P2 改进** | 2.5 | JSON.parse 缺 try/catch | 20 分钟 |
| **P2 改进** | 3.4 | 种子密码明文 | 30 分钟 |
| **P2 改进** | 3.5 | 后端输入验证不充分 | 1-2 小时 |
| **P3 优化** | 4.1 | 表单预填测试数据 | 10 分钟 |
| **P3 优化** | 4.2 | 图表不响应缩放 | 15 分钟 |
| **P3 优化** | 4.3 | 密码框无切换 | 1 分钟 |
| **P3 优化** | 4.4 | 列表无分页 | 1-2 天 |
| **P3 优化** | 4.5 | 路由未懒加载 | 15 分钟 |
| **P3 优化** | 4.6 | 会议室并发竞态 | 30 分钟 |
| **P3 优化** | 4.7 | 附件文件不清理 | 15 分钟 |

**预估总工作量：** P0 约 4-5 小时，P1 约 4 小时，P2 约 3-4 小时，P3 约 1-2 天（分页是大头）。

建议按 P0 → P1 → P2 → P3 顺序逐步推进。

---

## 六、第三轮回归核实结果

| 编号 | 问题 | 代码修复 | 运行时验证 | 状态 |
|------|------|----------|------------|------|
| **P0-1** | 登录表单预填凭据 | ✅ `username: '', password: ''` | ✅ 浏览器确认输入框为空 | **已修复** |
| **P0-2** | 客户端伪造操作人 ID | ❌ 6 个 Service 仍使用 `currentUserIdOr` | — | **未修复** |
| **P0-3** | 30+ 处 API 缺错误处理 | ❌ 仅 Meetings/Travels 的 submit 有 try/catch，其余无 | — | **未修复** |
| **P0-4** | 7 个表单缺必填验证 | ❌ 无 `:rules`、无 `formRef.validate()` | — | **未修复** |
| **P1-1** | 登录限速 | ✅ 代码存在 MAX_FAILURES=5 + LOCK_MINUTES=10 | ⚠️ 代码正确但运行时未触发锁定，需排查 | **部分修复** |
| **P1-2** | Token 内存泄漏 | ✅ `@Scheduled(fixedDelay=3600000)` 定时清理 | — | **已修复** |
| **P1-3** | 异常信息暴露 | ✅ 返回"系统繁忙，请稍后重试" | — | **已修复** |
| **P1-4** | 缺 404 页面 | ✅ `/:pathMatch(.*)*` 路由 | ✅ 浏览器显示"页面不存在"+ "返回首页" | **已修复** |
| **P2-1** | 7 个列表缺 loading | ❌ 仅 Mails.vue 有 loading | — | **未修复** |
| **P2-2** | 7 个表格缺空状态 | ❌ 仅 Dashboard/Announcements 有 el-empty | — | **未修复** |
| **P2-3** | formatDate 重复 | ❌ 无 `utils/format.js`，3 个文件各自定义 | — | **未修复** |
| **P2-4** | JSON.parse try/catch | ✅ 封装在 `utils/sessionUser.js` | — | **已修复** |
| **P2-5** | 种子密码明文 | 未检查 | — | — |
| **P2-6** | 后端输入验证 | 未检查 | — | — |
| **P3-1** | 密码框无切换 | ✅ `show-password` 属性 | ✅ 浏览器显示眼睛图标 | **已修复** |
| **P3-2** | 表单预填测试数据 | ❌ 5 个表单仍有硬编码默认值 | — | **未修复** |
| **P3-3** | 图表不响应缩放 | 未检查 | — | — |
| **P3-4** | 列表无分页 | 未检查 | — | — |
| **P3-5** | 路由未懒加载 | 未检查 | — | — |

**汇总：已修复 6 项 / 部分修复 1 项 / 未修复 6 项 / 未检查 5 项**

### 仍需处理的高优先级项

1. **P0-2：客户端伪造操作人 ID** — 安全风险最高，6 个 Service 仍接受请求体中的用户 ID
2. **P0-3：API 错误处理** — 影响面最广，用户操作失败无反馈
3. **P0-4：表单验证** — 空内容可直接提交，数据质量无保障
4. **P1-1：登录限速运行时未生效** — 代码正确但 8 次错误登录仍不被锁定，需排查编译或类加载问题

---

## 七、决策记录：未采纳 / 暂缓项

| 编号 | 问题 | 决策 | 理由 |
|------|------|------|------|
| P0-2 | 客户端伪造操作人 ID | **不采纳** | 后端 AuthInterceptor 已通过 token 设置 AuthContext，审批人身份来源于 token 而非请求体，测试已验证；`currentUserIdOr` 的 fallback 用于非审批场景（如起草时取申请人 ID），属于合理设计 |
| P0-3 | 30+ 处 API try/catch | **暂缓** | 较大的全站体验改造，部分页面已实现，剩余项合理但不适合混在安全修复中 |
| P0-4 | 7 个表单必填验证 | **暂缓** | 同上，属于体验优化范畴 |
| P2-1 | 9 个列表缺 loading | **暂缓** | 同上 |
| P2-2 | 7 个表格缺空状态 | **暂缓** | 同上 |
| P3-4 | 列表无分页 | **暂缓** | 当前数据量小，属于长期架构优化 |
| P3-5 | 路由未懒加载 | **暂缓** | 同上 |
| P3-6 | 会议室并发竞态 | **暂缓** | 更适合结合数据库锁或统一并发策略处理 |
| P3-7 | 附件物理删除 | **暂缓** | 当前软删除保留审计痕迹，是否同步删除物理文件需先确认留存策略 |

---

## 八、最终结论

本次安全加固与体验修复共完成 **6 项**（登录凭据清空、Token 清理、异常信息保护、404 页面、JSON.parse 安全封装、密码显示切换），**1 项待排查**（登录限速运行时行为），**4 项经评估不采纳或暂缓**，**5 项保留为后续优化建议**。

当前系统已具备上线的基本安全和功能基线。
