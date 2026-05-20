# 高校办公管理系统

这是根据 `doc/需求.md` 和 `doc/设计.md` 开发的课程项目。现在工程已经拆成清晰的前后端目录：

- `backend/`：Java 后端，Spring Boot + Maven + MySQL/JDBC
- `frontend/`：Vue 3 前端，Vite + Element Plus + Axios + ECharts
- `doc/`：需求、设计、接口、数据库初始化等文档

## 项目结构

```text
OfficeManagementSystem
├─ backend
│  ├─ pom.xml
│  ├─ src/main/java/com/university/oms
│  │  ├─ common
│  │  ├─ config
│  │  ├─ controller
│  │  ├─ design
│  │  ├─ dto
│  │  ├─ model
│  │  ├─ repository
│  │  └─ service
│  ├─ sql
│  │  ├─ schema.sql
│  │  └─ data.sql
│  └─ scripts
│     └─ init-mysql.ps1
├─ frontend
│  ├─ package.json
│  └─ src
└─ doc
```

## 后端启动

内存模式启动：

```powershell
cd backend
D:\dev\maven\apache-maven-3.9.14\bin\mvn.cmd spring-boot:run
```

MySQL 模式启动：

```powershell
cd backend
D:\dev\maven\apache-maven-3.9.14\bin\mvn.cmd spring-boot:run -Dspring-boot.run.profiles=mysql
```

后端地址：

```text
http://localhost:8080
```

## 前端启动

```powershell
cd frontend
npm install
npm run dev
```

前端地址：

```text
http://localhost:5173
```

Vite 已配置 `/api` 代理到 `http://localhost:8080`。

## MySQL 初始化

```powershell
cd backend
powershell -ExecutionPolicy Bypass -File .\scripts\init-mysql.ps1 -User root -Password 123456
```

如 MySQL 客户端路径不同：

```powershell
cd backend
powershell -ExecutionPolicy Bypass -File .\scripts\init-mysql.ps1 `
  -MysqlBin D:\dev\mysql8\mysql-8.0.39-winx64\bin\mysql.exe `
  -User root `
  -Password 123456
```

MySQL 连接配置：

```text
backend/src/main/resources/application-mysql.properties
```

## 测试

后端测试：

```powershell
cd backend
D:\dev\maven\apache-maven-3.9.14\bin\mvn.cmd test
```

前端构建：

```powershell
cd frontend
npm run build
```

## 演示账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | 123456 | 系统管理员 |
| user | 123456 | 普通办公人员 |
| head | 123456 | 部门负责人 |
| leader | 123456 | 校级领导 |
| office | 123456 | 党办校办人员 |
| finance | 123456 | 财务人员 |
| security | 123456 | 保卫人员 |
| keeper | 123456 | 印章保管人 |

## 已完成模块

- 公文管理：起草、提交、AI 起草、AI 审核、涉密 AI 拦截。
- 印章管理：印章台账、用印申请、材料必填、用印登记、归还确认。
- 会议管理：会议室维护、会议室推荐、冲突检测、大型活动安全材料校验。
- 请示报告：提交、保密审查状态、审批记录。
- 差旅审批：预算申请、住宿/伙食/市内交通补助测算、超标提示。
- 通用审批：统一审批历史、通过/退回。
- 统计报表：公文、用印、会议、大型活动、差旅、请示报告等指标。
- 数据持久化：支持内存模式和 MySQL profile，MySQL 模式会写库并在重启时回读。
