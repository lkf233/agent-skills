# Agent 全栈项目技术架构规划（V1）

## 1. Summary

- 目标：规划一个面向中小团队/企业个人的 Agent 全栈系统，V1 聚焦“登录注册 + 对话会话 + 历史记忆 + 知识库 + 工具调用”。
- 技术约束：前端 Next.js；后端 Java（Spring Boot + Maven + PostgreSQL）；后端采用 Controller/Service/Mapper 三层架构。
- 关键选择：MCP 在 V1 采用“内置工具 + 用户配置工具”的混合模式；API 风格采用 REST + SSE；向量检索采用 PostgreSQL + pgvector；部署采用 Docker Compose。
- 安全策略：V1 采用轻量鉴权（JWT + 拦截器）与基础输入校验，不引入复杂 Security 鉴权链路与安全监测平台。
- 交付物：提供可直接进入开发的技术架构、模块拆分、数据模型、接口规划、失败处理、测试验收与里程碑实施顺序。

## 2. Current State Analysis

- 当前仓库状态：`d:\front_mianshi\agent-chat` 为空目录，未发现现有前后端代码、配置、CI、文档或数据库脚本。
- 结论：本次规划按“从 0 到 1”架构设计输出，不依赖既有实现；所有文件路径为待创建目标路径。
- 风险与机会：
  - 风险：无现有脚手架，若不先统一分层与接口约束，后续会出现前后端联调和模块耦合问题。
  - 机会：可从一开始统一 MVC 三层边界与模块边界，避免后续大规模重构。

## 3. Proposed Changes

### 3.1 仓库与模块结构（单仓）

- 目标文件/目录（待创建）：
  - `frontend/`：Next.js 控制台与对话界面
  - `backend/`：Spring Boot 服务（单体分模块）
  - `deploy/docker-compose.yml`：本地一键部署
  - `docs/architecture/`：架构、接口、ER 图、序列图
- Why：
  - 中小团队阶段以“单仓 + 单体分层”降低复杂度，保证开发效率。
- How：
  - 前后端分目录，后端先单体模块化，后续可按边界拆微服务。

### 3.2 后端分层与模块边界（Spring Boot 三层）

- 目标文件/目录（待创建）：
  - `backend/src/main/java/.../controller/`
  - `backend/src/main/java/.../service/`
  - `backend/src/main/java/.../mapper/`
  - `backend/src/main/java/.../dto/`
  - `backend/src/main/java/.../config/`
- 模块拆分（包级）：
  - `auth`：登录注册、Token 刷新、密码安全
  - `chat`：对话会话、流式输出、消息持久化
  - `memory`：短期记忆摘要、长期记忆提炼与检索
  - `kb`：知识库、文档、切分、索引任务
  - `skills`：技能定义、分级披露规则、技能执行路由
  - `planner`：可控规划执行（工作流优先）
  - `mcp`：内置 MCP 适配、用户自定义工具注册、调用适配、超时重试
  - `llm`：模型路由与统一调用抽象
  - `common`：异常、日志、审计、基础组件
- Why：
  - 避免以技术层横切导致业务耦合，保证每个领域可独立演进。
- How：
  - 每个模块保持 Controller/Service/Mapper 三层，`dto` 负责接口与服务层数据传输，`entity` 负责持久化映射。

### 3.3 核心能力设计

- 用户登录注册（V1）：
  - 支持邮箱/手机号 + 密码注册登录，密码强哈希存储（BCrypt/Argon2）。
  - 支持 JWT 鉴权与主动登出；V1 不引入复杂权限模型与多层安全策略。
- 用户对话与会话管理（V1）：
  - 支持会话创建、重命名、归档、删除；按用户隔离会话数据。
  - 对话接口支持 SSE 流式输出，返回 token 片段、步骤事件、工具调用事件。
- 对话历史与记忆管理（V1）：
  - 保留完整 message 历史，支持分页查询、关键词过滤、按会话回放。
  - 记忆分层：短期记忆（会话窗口摘要）+ 长期记忆（跨会话偏好/事实）。
  - 长期记忆写入采用“显式规则 + 评分阈值”，避免噪声污染。
- 知识库（V1）：
  - 支持文件上传、文本抽取、chunk 切分、向量化入库、混合检索（关键词 + 向量）。
  - 首版采用同步上传 + 异步索引任务（状态可查）。
- MCP 工具调用（V1 混合模式）：
  - 内置工具：由系统预置并写入代码，默认可用，适合高频稳定场景。
  - 用户配置工具：用户通过配置页面录入工具定义（名称、协议、地址、鉴权、schema），经校验后启用。
  - 提供统一 `McpClient` 接口，业务仅依赖抽象，不直接耦合第三方协议细节。
  - 对用户配置工具执行连通性检测、参数 schema 校验、超时与熔断策略。
- Skills 渐进式披露：
  - Skill 分三级：L1 原子技能、L2 组合技能、L3 规划型技能。
  - V1 先按“任务复杂度 + 页面入口”披露，不做权限体系。
- 规划执行引擎（可控工作流优先）：
  - 固定模板：检索 -> 计划 -> 执行 -> 汇总。
  - 对高风险步骤加“确认点”接口，支持人工接管。

### 3.4 数据模型（PostgreSQL + pgvector）

- 目标文件（待创建）：
  - `backend/src/main/resources/db/migration/V1__init.sql`
  - `backend/src/main/resources/db/migration/V2__kb_vector.sql`
- 核心表（V1）：
  - `user_account`、`user_credential`、`user_token_session`
  - `conversation`、`message`
  - `conversation_memory`、`user_memory_profile`
  - `knowledge_base`、`document`、`document_chunk`
  - `embedding`（vector 列）
  - `skill`、`skill_version`
  - `plan_run`、`plan_step`
  - `mcp_builtin_tool`、`mcp_user_tool`
  - `mcp_tool_call_log`
- 表设计约束（当前版本）：
  - 所有核心业务表使用伪删除字段 `del_flag`（0-未删除，1-已删除）。
  - 用户ID使用数据库自增主键（bigint）。
  - 会话ID使用字符串主键（全局唯一ID，服务端生成）。
  - Java 时间类型统一使用 `LocalDateTime`。
  - 数据库表禁止使用物理外键，通过业务逻辑维护关联关系。
  - 对高频查询字段建立索引并通过增量DDL持续维护。
  - 当前用户信息统一通过 `UserContext` 在应用层获取，由拦截器注入并在请求结束后清理。
  - Token 生成与解析统一由 `JwtTokenUtil` 工具类封装，避免散落在 Service/Controller。
- Why：
  - 先保证“可追踪 + 可回放 + 可分析”，支撑质量迭代。
- How：
  - 对 `message`、`plan_step`、`mcp_tool_call_log` 建立时间索引；对 `embedding` 建向量索引；保留引用关系支持答案溯源。

### 3.5 API 契约（REST + SSE）

- 目标文件（待创建）：
  - `docs/architecture/api-contract.md`
  - `backend/src/main/java/.../controller/*`
- 核心接口（V1）：
  - `POST /api/auth/register`、`POST /api/auth/login`、`POST /api/auth/refresh`、`POST /api/auth/logout`
  - `POST /api/conversations`、`GET /api/conversations`、`PATCH /api/conversations/{id}`、`DELETE /api/conversations/{id}`
  - `POST /api/chat/completions`（SSE 流式）
  - `GET /api/conversations/{id}/messages`
  - `GET /api/memories`、`POST /api/memories/rebuild`
  - `POST /api/kb`、`POST /api/kb/{id}/documents`
  - `POST /api/skills/execute`
  - `POST /api/plans/run`、`GET /api/plans/run/{runId}`
  - `GET /api/mcp/tools`、`POST /api/mcp/tools`、`PATCH /api/mcp/tools/{id}`、`POST /api/mcp/tools/{id}/test`
- Why：
  - 先统一接口契约，前后端并行开发，降低联调成本。
- How：
  - 响应统一 envelope（code/message/data/requestId）；SSE 事件类型标准化（token/step/tool/error/done）。

### 3.6 前端信息架构（Next.js）

- 目标文件/目录（待创建）：
  - `frontend/src/app/chat/page.tsx`
  - `frontend/src/app/kb/page.tsx`
  - `frontend/src/app/skills/page.tsx`
  - `frontend/src/app/plans/page.tsx`
  - `frontend/src/components/sse-chat/`
- 页面与交互：
  - Auth：注册、登录、Token 续期与登出
  - Chat：流式对话、引用片段、执行步骤面板
  - Session：会话列表、搜索、归档与删除
  - KB：知识库列表、文档上传、索引状态
  - Skills：技能列表、分级披露展示、执行入口
  - MCP Tools：内置工具展示、用户工具配置、连通性测试
  - Plans：规划任务状态、步骤日志、失败重试
- Why：
  - 管理台与执行台一体化，便于团队试用与反馈收集。
- How：
  - 优先实现最小可用页面，不引入复杂低代码编辑器。

### 3.7 可观测性与失败处理

- 目标文件（待创建）：
  - `backend/src/main/java/.../common/exception/*`
  - `backend/src/main/java/.../common/logging/*`
  - `docs/architecture/observability.md`
- 机制：
  - 请求链路 requestId 贯穿 Controller -> Service -> MCP/LLM。
  - 失败分类：模型失败、检索失败、工具失败、超时中断。
  - 重试策略：仅对幂等步骤自动重试；对非幂等步骤要求人工确认。

### 3.8 部署与运行（Compose）

- 目标文件（待创建）：
  - `deploy/docker-compose.yml`
  - `deploy/.env.example`
- 组件：
  - `app-backend`、`app-frontend`、`postgres`。
- Why：
  - 中小团队优先快速试用，降低初期部署门槛。
- How：
  - 提供单命令启动、健康检查、数据库初始化脚本挂载。

## 4. Assumptions & Decisions

- 已确认决策：
  - 面向中小团队；
  - V1 核心能力：登录注册、对话会话、历史记忆、知识库、工具调用；
  - 后端 Java Spring Boot + Maven + PostgreSQL，三层架构；
  - MCP 采用内置工具 + 用户配置工具混合模式；
  - API 采用 REST + SSE；
  - 向量检索采用 pgvector；
  - 部署采用 Docker Compose；
  - 权限体系 V1 暂不实现，后续补齐。
  - V1 不建设安全监测平台，不引入复杂 Security 鉴权框架。
- 规划内显式假设：
  - V1 不引入多租户隔离与复杂 RBAC，仅保留后续扩展位。
  - V1 先支持文本类文档检索，图片/表格高级解析后置。
  - V1 的 skills 披露与工具使用基于产品规则，不依赖权限系统。
  - 后续所有架构与数据库变更必须同步记录到后端技术文档与增量DDL。

## 5. Verification Steps

- 设计验收：
  - 接口契约文档与实体关系图完成后，前后端可独立开发且无关键阻塞。
  - 所有核心流程具备时序图：对话、检索、规划执行、MCP 调用。
- 工程验收（进入实施后）：
  - 用户可完成注册、登录、续期、登出，且密码不明文存储。
  - 本地 Compose 可一键启动，前后端可联通。
  - 会话可创建、查询、删除；消息历史可分页回放。
  - Chat SSE 流式可正常输出 token 与步骤事件。
  - 记忆摘要可自动生成，跨会话长期记忆可查询。
  - 知识库上传后可检索并返回引用来源。
  - 规划执行流程可查看 run 与 step 状态，失败可追踪。
  - MCP 内置工具与用户配置工具各至少一个可稳定调用，日志可回放。

## 6. 实施顺序建议（执行阶段参考）

- Step 1：初始化仓库结构与后端骨架（分包 + 三层基线）。
- Step 2：落地登录注册、JWT 轻量鉴权与基础输入校验。
- Step 3：落地会话管理与 SSE 对话链路。
- Step 4：落地对话历史存储与记忆管理。
- Step 5：落地知识库入库与 pgvector 检索。
- Step 6：接入内置 MCP + 用户配置 MCP 与统一调用抽象。
- Step 7：实现 Skills 分级披露、规划执行与运行日志。
- Step 8：补齐 Compose 与最小可观测能力。
