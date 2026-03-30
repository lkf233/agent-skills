# 前端架构设计（Agent 平台 V2）

## 1. 目标与边界

- 目标：在当前“后端先行、前端未落地”的现状下，给出可直接实施的前端工程架构，支撑 V2 业务域（认证、资产、Agent 配置、预览对话、正式对话）。
- 范围：仅覆盖 Web 前端（管理台 + 对话台），不含移动端与桌面端。
- 对齐约束：
  - 后端统一响应 `ApiResponseObject<T>`；
  - 接口基线以 `docs/design/OpenAPI契约冻结（Agent平台V2）.md` 为准；
  - 对话链路需支持 SSE 事件流。

## 2. 现状输入

- 仓库当前仅有后端工程与设计文档，暂无前端代码目录。
- 已有可联调后端接口：认证、会话、知识库基础能力。
- 冻结契约已定义完整 V2 资产与对话接口（包含 Agent/Skill/Tool/Preview/Conversation/Logs）。

## 3. 架构原则

- 分层清晰：页面编排、领域逻辑、基础设施分离，避免“页面直调接口”。
- 路径固定：`app -> components -> hooks/contexts -> lib`，降低新人理解成本。
- 契约驱动：前端模型与 API 类型以 OpenAPI 契约为单一事实源。
- 增量落地：先打通“可运行 MVP”，再扩展到完整 V2 域。
- 可观测与可回滚：所有请求携带 requestId 追踪；关键交互可降级。
- 安全默认：Token 最小暴露、输入兜底校验、上传类型与大小前置限制。

## 4. 技术选型

### 4.1 核心框架

- **Next.js（App Router）+ TypeScript**
  - 原因：与既有规划一致，具备路由分段、服务端渲染与流式能力。

### 4.2 UI 与交互

- **shadcn/ui + Radix + Tailwind**
  - shadcn/ui：提供原子组件模板，组件目录统一在 `components/ui`。
  - Radix：承载交互底座（弹层、折叠、选择器等无障碍能力）。
  - Tailwind：统一样式与主题变量，适配聊天与后台场景。

### 4.3 状态与数据

- **Context + hooks**：管理全局状态与业务复用逻辑（账号、工作区、RAG 对话）。
- **TanStack Query**：管理服务端状态（分页、缓存、失效、重试）。
- **React Hook Form + Zod**：表单状态与输入约束（与契约类型对齐）。

### 4.4 工程质量

- ESLint + Prettier：统一代码风格。
- Vitest + Testing Library：组件与 hooks 单测。
- Playwright：关键流程 E2E（登录、上传、流式对话）。

### 4.5 依赖管理

- **pnpm（唯一包管理器）**
  - 统一使用 `pnpm-lock.yaml` 锁定依赖版本。
  - 禁止提交 `package-lock.json` 与 `yarn.lock`。
  - 标准命令：`pnpm install`、`pnpm dev`、`pnpm build`、`pnpm test`。

## 5. 信息架构与路由

### 5.1 一级路由

- `/login`、`/register`
- `/workspace/agents`
- `/workspace/assets/knowledge-bases`
- `/workspace/assets/skills`
- `/workspace/assets/tools`
- `/workspace/preview/[agentId]`
- `/workspace/chat/[agentId]`
- `/workspace/logs/[agentId]`

### 5.2 路由分组约定

- 使用 `(auth)`、`(main)`、`widget` 分组组织路由。
- 根路由 `/` 默认重定向到主入口（例如 `/explore` 或工作台首页）。
- 主业务布局放在 `app/(main)/layout.tsx`，后台管理可再叠加 `admin/layout.tsx`。
- `middleware.ts` 当前只处理 `/login` 与 `/register` 的已登录重定向，不作为全站鉴权网关。

### 5.3 页面职责

- Agents：Agent 列表、创建编辑、绑定 Skills/Tools/KB、发布归档。
- Assets-KB：知识库 CRUD、文件上传、解析状态轮询与失败重试入口。
- Assets-Skills/Tools：资产管理与测试执行。
- Preview：草稿配置会话 + 预览消息流（不影响正式会话）。
- Chat：正式会话列表、消息发送、SSE 流展示、历史回放。
- Logs：工具调用与技能执行日志检索。

## 6. 目录与分层

```text
frontend/
  src/
    app/
      (auth)/
        login/page.tsx
        register/page.tsx
      (main)/
        layout.tsx
        explore/page.tsx
        workspace/[id]/page.tsx
        studio/[agentId]/page.tsx
        knowledge/[kbId]/page.tsx
        traces/[traceId]/page.tsx
      widget/
        page.tsx
    components/
      ui/
      knowledge/
      payment/
      rag-chat/
    hooks/
    contexts/
    lib/
      api/
      services/
      http-client.ts
      api-config.ts
```

### 6.1 分层职责

- `app`：路由与页面壳，仅做页面编排。
- `components`：原子组件与业务组件分层。
- `hooks/contexts`：状态管理与业务复用逻辑封装。
- `lib`：接口配置、服务层、HTTP 客户端与基础工具。

## 7. API 与数据流设计

### 7.1 API 客户端规范

- `lib/http-client.ts`
  - 注入 `Authorization`、`x-request-id`；
  - 统一解析 `ApiResponseObject<T>`，在 data 层抛业务异常；
  - 统一错误映射（401 跳登录，403 权限提示，5xx 系统兜底）。

### 7.2 查询与变更策略

- API 端点统一集中在 `lib/api-config.ts`。
- 业务调用按域拆分到 `lib/services/*-service.ts`。
- Query Key 规范：`['kb', 'list', params]`、`['agent', id]`、`['conversation', id, 'messages']`。
- 写操作后精确失效，避免全局刷新。
- 列表页优先分页查询；详情页按需懒加载。

### 7.3 SSE 事件通道

- 支持事件：`token`、`tool_call`、`skill_exec`、`done`、`error`。
- 组件职责：
  - `SseStreamClient`：连接与重连；
  - `StreamEventReducer`：事件归并为 UI 可渲染状态；
  - `MessageAssembler`：将 token 片段聚合为最终消息。
- 失败策略：
  - 网络中断：指数退避重连（上限次数）；
  - 业务失败：保留已到达片段并标记失败态；
  - 用户中断：主动关闭连接并落地当前草稿。

## 8. 认证与安全设计

- Token 生命周期：
  - 登录后写入内存态 + 持久态（可选）；
  - 请求拦截器自动带 Bearer Token；
  - 401 触发清理并跳转 `/login`。
- 路由守卫：
  - `(main)` 分组统一鉴权检查；
  - 未登录不可访问业务页面。
- 上传安全：
  - 前端先校验文件大小、后缀、MIME；
  - 显示扫描与解析状态，不在前端缓存原始敏感内容。

## 9. 与后端契约对齐策略

### 9.1 双轨能力

- **MVP 轨（立即可联调）**：使用当前已落地接口（认证/会话/知识库）。
- **V2 轨（契约完整形态）**：按冻结契约补齐 Agent/Skill/Tool/Preview/Logs。

### 9.2 类型对齐

- 所有请求与响应类型放在 `lib/types/api/`。
- 字段命名严格跟随契约；禁止页面层手写“临时字段”。
- 对不兼容字段通过 `lib/mappers/*` 做单点转换。

## 10. 迭代实施计划

### Phase 1：工程底座与认证

- 初始化 Next.js + TS + UI + Query + Context 基线。
- 实现登录注册、鉴权路由守卫、全局错误处理。

### Phase 2：资产与会话 MVP

- 知识库列表/创建、文件上传、文件状态列表。
- 会话创建与列表、消息历史查询（先非流式可用，再接流式）。

### Phase 3：流式对话与预览

- 对接 Preview/Conversation SSE，完成 token/tool/skill 事件呈现。
- 增加中断、重试、失败追踪能力。

### Phase 4：Agent 配置与资产绑定

- Agents CRUD、绑定 Skills/Tools/KB、发布归档。
- 形成“配置-预览-发布-正式对话”闭环。

### Phase 5：可观测与质量

- 日志检索页、关键交互埋点、错误归因看板。
- 补齐单测与 E2E 冒烟，纳入 CI。

## 11. 风险与治理

- 契约漂移风险：以冻结契约为基线，新增字段仅走向后兼容。
- SSE 稳定性风险：建立统一连接层，禁止页面直接创建 EventSource。
- 状态失控风险：服务端状态统一交给 Query，本地状态仅保留会话性 UI。
- 页面耦合风险：业务动作只能经 `hooks/contexts + lib/services` 发起，页面层不直连 API。

## 12. 验收标准

- 可用性：登录后可完成知识库上传、会话创建、消息流式展示。
- 一致性：接口字段与契约一致，统一响应与错误处理生效。
- 可维护性：目录分层稳定，新增业务功能无需跨层大改。
- 可测试性：核心流程具备单测与 E2E 冒烟。
