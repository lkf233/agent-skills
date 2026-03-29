# 📋 企业知识库 Agent - 开发任务拆解 (Tasks)

本项目将采用敏捷开发模式，划分为 4 个主要里程碑（Milestone）。

---

## Milestone 1: 基础设施与文档入库链路 (Ingestion Pipeline)
*目标：搭建项目骨架，实现文件的上传、解析、L1/L2/L3 三级分块与向量化存储。*

- [ ] **Task 1.1: 初始化项目与数据库**
  - [x] 使用 pnpm 初始化 NestJS 项目 (`pnpm dlx @nestjs/cli new backend --package-manager pnpm`).
  - [x] 本地原生安装 PostgreSQL 和 Redis，并为 PostgreSQL 配置 pgvector 扩展（不使用 Docker）。
  - [x] 配置 Prisma ORM 连接本地 PostgreSQL 数据库.
  - [x] 根据 `spec.md` 创建并迁移数据库表结构 (重点实现 `Node` 表的父子级联).
  - [x] 划分核心模块 (DocumentModule, ChatModule, RAGModule 等).
- [x] **Task 1.2: 文件上传与队列调度 (DocumentModule & BullMQ)**
  - 搭建 Redis 环境，集成 `@nestjs/bull`.
  - 实现 `POST /api/documents/upload` 接口，将文件解析任务推入队列.
  - 实现文档状态轮询接口 `GET /api/documents/status/:id`.
- [x] **Task 1.3: LlamaIndex.ts 解析器与三级分块**
  - [x] 集成 `LlamaIndex.ts` 读取 PDF/Excel/Markdown.
  - [x] 实现 `AutoMergingRetriever` 所需的树状切分逻辑：将文档切割为 L1(根) -> L2(中) -> L3(叶子) 节点.
  - [x] 调用 Embedding API 仅为 L3 节点生成 1536 维向量.
- [ ] **Task 1.4: 数据持久化与建立索引**
  - 将生成的 L1/L2/L3 节点及其关联关系写入 Prisma `Node` 表.
  - 确保 L3 节点的向量数据正确存入 pgvector 并建立 HNSW/IVFFlat 索引.

---

## Milestone 2: 高级 RAG 检索链路开发 (Retrieval Pipeline)
*目标：实现“搜得准、搜得全”的核心搜索逻辑，并集成打分与重写门控。*

- [ ] **Task 2.1: 混合检索与 Auto-merging (初次召回)**
  - 实现 Dense (pgvector) + Sparse (BM25) 混合检索逻辑.
  - 实现 RRF 融合算法对初次召回的 L3 节点进行排序.
  - 编写 Auto-merging 逻辑：统计 L3 命中率，若达标则从 DB 中捞出对应的 L2/L1 父节点.
- [ ] **Task 2.2: 引入重排序机制 (Reranking)**
  - 接入轻量级 Reranker 模型 API（如 Cohere 或 BGE）.
  - 对初次召回并 Auto-merge 后的候选集进行二次精排打分.
- [ ] **Task 2.3: 打分门控与查询重写 (Grade & Rewrite)**
  - 编写 Prompt 让 LLM 对精排后的 Context 进行 `yes/no` 相关性打分.
  - 若打分为 `no`，触发查询重写路由 (Query Rewrite / HyDE).
  - 封装完整的 `retrieve_pipeline`，支持二次召回并收集全链路 `rag_trace` 观测数据.

---

## Milestone 3: 基于 LangGraph 的多 Agent 编排与流式对话 (Agentic Workflow)
*目标：接入 LangGraph.js，通过状态机(StateGraph)实现复杂的 RAG 路由、工具调用和记忆管理。*

- [ ] **Task 3.1: 会话与记忆管理**
  - 实现 `Session` 和 `Message` 的数据库 CRUD。
  - 结合 LangChain 的 `BaseChatMessageHistory` 或自定义逻辑，实现历史记忆的加载。
- [ ] **Task 3.2: 构建 LangGraph 状态机与核心节点**
  - 引入 `@langchain/langgraph` 和 `@langchain/core`。
  - 定义 `AgentState` 接口（包含 messages, rag_context, grade_decision 等状态字段）。
  - 开发核心 Nodes：`retrieve_node` (初次召回)、`grade_node` (打分)、`rewrite_node` (重写)、`generate_node` (生成)。
  - 定义 Conditional Edges，实现 "打分不过关则重写，重写后重新找回" 的循环图逻辑。
  - 注册辅助 Tools（如 `web_search` 兜底）。
- [ ] **Task 3.3: SSE 流式接口开发与观测数据下发**
  - 开发 `POST /api/chat/stream` 接口。
  - 使用 LangGraph 的 `.streamEvents({ version: 'v2' })` 方法监听图执行生命周期。
  - 将 `on_chat_model_stream` 事件转化为文本 SSE (`event: text`)。
  - 将 Nodes 的流转事件转化为自定义的观测数据帧 (`event: trace`)，推送初次召回、打分、重写等中间状态给前端。
- [ ] **Task 3.4: 引用溯源格式化 (Citation)**
  - 优化大模型输出 Prompt，强制要求在回答后附上所引用的文档名称和片段。

---

## Milestone 4: 前端可视化与 MCP 协议暴露 (UI & Integration)
*目标：实现类似 Perplexity 的思考过程折叠面板，并暴露为外部插件。*

- [ ] **Task 4.1: 搭建 Next.js 前端应用**
  - 初始化前端页面，接入 `useChat` hook。
  - 解析 SSE 数据流中的文本与工具调用状态。
- [ ] **Task 4.2: 开发“思考过程”可视化面板**
  - 拦截流中的自定义 `e:` (Trace Data) 帧。
  - 渲染折叠面板，实时展示：检索文档数、打分结果、查询重写内容等。
  - 渲染底部的文档来源引用卡片 (Citations)。
- [ ] **Task 4.3: 搭建 MCP Server**
  - 引入 `@modelcontextprotocol/sdk`。
  - 创建一个独立的 Node.js 进程或路由，作为 MCP 服务端。
- [ ] **Task 4.4: 注册 MCP Tools 并测试**
  - 在 MCP Server 中注册 `query_enterprise_knowledge` 工具。
  - 在本地 Cursor/Trae 中配置 MCP Server 路径，验证编辑器直接提问。
