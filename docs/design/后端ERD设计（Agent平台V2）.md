# 后端 ERD 设计（Agent 平台 V2）

## 1. 设计目标
- 用户先创建并配置 Agent，再与 Agent 对话。
- Skills、Tools、知识库均为用户私有资产，可复用到多个 Agent。
- 支持配置后的预览对话，正式对话与预览对话数据隔离。
- 表结构遵循项目规范：`del_flag` 伪删除、禁用物理外键、通过索引保障查询性能。

## 2. 核心实体分组

### 2.1 用户与身份
- `user_account`：用户基础信息（沿用现有表）。

### 2.2 Agent 配置域
- `agent`：Agent 主体配置。
- `agent_skill_rel`：Agent 与 Skill 多对多关系。
- `agent_tool_rel`：Agent 与 Tool 多对多关系。
- `agent_kb_rel`：Agent 与知识库多对多关系。

### 2.3 资产域（用户私有）
- `skill`：用户上传技能定义。
- `tool_def`：用户上传工具定义。
- `knowledge_base`：知识库主表。
- `kb_file`：知识库文件。
- `kb_chunk`：切片文本与向量。
- `kb_ingest_task`：异步入库任务。

### 2.4 对话与运行域
- `preview_session`：预览会话（不进入正式会话）。
- `conversation`：正式会话（新增 `agent_id`）。
- `message`：正式会话消息。
- `preview_message`：预览会话消息。
- `tool_call_log`：工具调用日志。
- `skill_exec_log`：技能执行日志。

## 3. 数据表字段草案

### 3.1 agent
- `id varchar(64) pk`
- `user_id bigint not null`
- `name varchar(128) not null`
- `description varchar(1024) not null default ''`
- `avatar_url varchar(512) not null default ''`
- `status varchar(32) not null`（`DRAFT`/`PUBLISHED`/`ARCHIVED`）
- `model_config_json jsonb not null`（模型、温度、上下文窗口等）
- `created_at timestamptz not null default now()`
- `updated_at timestamptz not null default now()`
- `del_flag smallint not null default 0`
- 关键索引：`(user_id, del_flag, updated_at desc)`、`(status, del_flag)`

### 3.2 skill
- `id varchar(64) pk`
- `user_id bigint not null`
- `name varchar(128) not null`
- `description varchar(1024) not null default ''`
- `version varchar(32) not null default 'v1'`
- `schema_json jsonb not null`（入参/出参声明）
- `runtime_config_json jsonb not null default '{}'::jsonb`
- `status varchar(32) not null`（`DRAFT`/`ACTIVE`/`DISABLED`）
- 时间字段与 `del_flag`
- 索引：`(user_id, del_flag, updated_at desc)`、`(user_id, name, version, del_flag)`

### 3.3 tool_def
- `id varchar(64) pk`
- `user_id bigint not null`
- `name varchar(128) not null`
- `description varchar(1024) not null default ''`
- `tool_type varchar(32) not null`（`MCP`/`HTTP`/`INTERNAL`）
- `config_json jsonb not null`
- `auth_config_json jsonb not null default '{}'::jsonb`
- `status varchar(32) not null`（`DRAFT`/`ACTIVE`/`DISABLED`）
- 时间字段与 `del_flag`
- 索引：`(user_id, del_flag, updated_at desc)`、`(user_id, tool_type, del_flag)`

### 3.4 knowledge_base
- `id varchar(64) pk`
- `user_id bigint not null`
- `name varchar(128) not null`
- `description varchar(1024) not null default ''`
- `embedding_provider varchar(64) not null`
- `embedding_model varchar(128) not null`
- `status varchar(32) not null`（`ACTIVE`/`DISABLED`）
- 时间字段与 `del_flag`
- 索引：`(user_id, del_flag, updated_at desc)`

### 3.5 kb_file
- `id varchar(64) pk`
- `kb_id varchar(64) not null`
- `user_id bigint not null`
- `file_name varchar(255) not null`
- `storage_path varchar(1024) not null`
- `mime_type varchar(128) not null`
- `size_bytes bigint not null`
- `parse_status varchar(32) not null`（`QUEUED`/`PARSING`/`READY`/`FAILED`）
- `error_message varchar(1024) not null default ''`
- 时间字段与 `del_flag`
- 索引：`(kb_id, del_flag, created_at desc)`、`(user_id, parse_status, del_flag)`

### 3.6 kb_chunk
- `id varchar(64) pk`
- `kb_id varchar(64) not null`
- `kb_file_id varchar(64) not null`
- `user_id bigint not null`
- `chunk_no int not null`
- `content text not null`
- `content_tsv tsvector not null`（关键词检索字段）
- `embedding vector(1024)`（pgvector向量检索字段）
- 时间字段与 `del_flag`
- 索引：`(kb_id, kb_file_id, del_flag)`、`gin(content_tsv)`

### 3.6.1 kb_ingest_task
- `id varchar(64) pk`
- `kb_file_id varchar(64) not null`
- `user_id bigint not null`
- `task_status varchar(32) not null`（`PENDING`/`PROCESSING`/`SUCCESS`/`FAILED`）
- `retry_count int not null default 0`
- `error_message varchar(1024) not null default ''`
- 时间字段与 `del_flag`

### 3.7 关联表
- `agent_skill_rel`：`id`、`agent_id`、`skill_id`、`priority`、`enabled`、时间字段、`del_flag`
- `agent_tool_rel`：`id`、`agent_id`、`tool_id`、`priority`、`enabled`、时间字段、`del_flag`
- `agent_kb_rel`：`id`、`agent_id`、`kb_id`、`priority`、`enabled`、时间字段、`del_flag`
- 索引统一：`(agent_id, del_flag, priority)` 与 `(关联对象id, del_flag)`

### 3.8 对话与日志
- `preview_session`：`id`、`user_id`、`agent_id`、`status`、`snapshot_json`、时间字段、`del_flag`
- `preview_message`：`id`、`preview_session_id`、`role`、`content`、`tool_call_json`、时间字段、`del_flag`
- `conversation`：在现有表新增 `agent_id varchar(64) not null`
- `message`：`id`、`conversation_id`、`user_id`、`agent_id`、`role`、`content`、`tool_call_json`、`trace_id`、时间字段、`del_flag`
- `tool_call_log`：`id`、`user_id`、`agent_id`、`conversation_id`、`tool_id`、`status`、`latency_ms`、`request_json`、`response_json`、时间字段、`del_flag`
- `skill_exec_log`：`id`、`user_id`、`agent_id`、`conversation_id`、`skill_id`、`status`、`latency_ms`、`input_json`、`output_json`、时间字段、`del_flag`

## 4. 关键业务约束
- 一个 Agent 仅允许绑定当前用户拥有的 skills/tools/kb。
- Agent 发布前必须通过配置校验：至少 1 个模型配置，且绑定资产全部 `ACTIVE`。
- 预览会话仅可访问 `DRAFT/PUBLISHED` 状态 Agent，不写入正式 `conversation/message`。
- 正式会话仅允许 `PUBLISHED` Agent。
- 删除资产采用伪删除，发布中的 Agent 若引用资产，需先解绑或替换。
- 文件上传成功后必须进入异步切片与向量化流程，`kb_file.parse_status` 由 `QUEUED -> PARSING -> READY/FAILED` 驱动。
- 用户查询必须执行“向量检索 + 关键词检索”双路召回，再执行 RRF 融合与 ReRank 精排后返回最终上下文。

## 4.1 检索流水线约束
- 阶段一：向量召回（pgvector topK）
- 阶段二：关键词召回（`content_tsv` 全文检索 topK）
- 阶段三：RRF 融合（统一候选池）
- 阶段四：ReRank 精排（产出最终 topN）
- 阶段五：写入检索日志（候选数量、耗时、命中来源、最终片段）

## 5. 状态机建议
- Agent：`DRAFT -> PUBLISHED -> ARCHIVED`
- Skill/Tool：`DRAFT -> ACTIVE -> DISABLED`
- KB 文件：`QUEUED -> PARSING -> READY/FAILED`
- 预览会话：`RUNNING -> CLOSED`
- 正式会话：`ACTIVE -> CLOSED`

## 6. 前后端契约建议
- 所有请求/响应对象必须加 OpenAPI 注解，并在 Knife4j 可见。
- 列表接口统一支持分页参数：`pageNo`、`pageSize`、`keyword`（可选）。
- 统一错误码分层：参数错误、权限错误、状态错误、外部依赖错误、系统错误。
