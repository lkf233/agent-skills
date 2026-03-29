# V3-V8 增量 DDL 规划（Agent 平台 V2）

## 1. 规划原则
- 严格增量：仅新增 `Vx__*.sql`，不覆盖历史脚本。
- 与文档同步：每个版本的表变更同时更新 `docs/design`。
- 禁用物理外键：通过索引 + 业务校验保证一致性。
- 统一字段：新表默认包含 `created_at`、`updated_at`、`del_flag`。

## 2. 版本拆分

### V3：Agent 主体与关系表
- 新增表：`agent`、`agent_skill_rel`、`agent_tool_rel`、`agent_kb_rel`
- 变更：`conversation` 新增 `agent_id`
- 索引：
  - `idx_agent_user_del_updated`
  - `idx_agent_status_del`
  - `idx_agent_skill_rel_agent_priority`
  - `idx_agent_tool_rel_agent_priority`
  - `idx_agent_kb_rel_agent_priority`
  - `idx_conversation_agent_id`

### V4：Skills 与 Tools 资产表
- 新增表：`skill`、`tool_def`
- 索引：
  - `idx_skill_user_del_updated`
  - `idx_skill_user_name_version`
  - `idx_tool_user_del_updated`
  - `idx_tool_user_type_del`

### V5：知识库主表与文件表
- 新增表：`knowledge_base`、`kb_file`
- 索引：
  - `idx_kb_user_del_updated`
  - `idx_kb_file_kb_del_created`
  - `idx_kb_file_user_parse_del`

### V6：向量切片与异步入库任务
- 新增表：`kb_chunk`、`kb_ingest_task`
- 索引：
  - `idx_kb_chunk_kb_file_del`
  - `idx_kb_chunk_kb_del`
  - `idx_kb_chunk_content_tsv_gin`
  - `idx_kb_ingest_task_status_updated`
- 说明：
  - `kb_chunk` 增加 `content_tsv` 字段用于关键词检索
  - `kb_file.parse_status` 从 `QUEUED` 进入异步处理流程
  - 查询链路采用“向量检索 + 关键词检索 + RRF + ReRank”统一流程

### V7：pgvector 向量列升级
- 扩展：`create extension if not exists vector`
- 变更：`kb_chunk` 新增 `embedding vector(1024)`
- 索引：
  - `idx_kb_chunk_embedding_ivfflat`

### V8：清理旧向量字段
- 变更：`kb_chunk` 删除 `embedding_json`
- 说明：
  - 所有向量仅通过 `embedding vector(1024)` 存储
  - 向量化结果不再保留 JSON 冗余列

## 3. 脚本命名建议
- `V3__agent_core_and_relations.sql`
- `V4__skill_and_tool_assets.sql`
- `V5__knowledge_base_and_files.sql`
- `V6__kb_chunk_and_ingest_task.sql`
- `V7__pgvector_embedding_upgrade.sql`
- `V8__drop_embedding_json_column.sql`

## 4. 回滚策略建议
- 每个版本额外提供同名回滚草案文档（不进 Flyway 执行）：`Rx__*.sql.md`。
- 回滚优先顺序：日志表 -> 对话表 -> 关系表 -> 主体表。
- 涉及数据迁移字段（如 `conversation.agent_id`）必须提供兜底默认值策略。

## 5. 验收清单
- `flyway migrate` 在空库与已有 V1/V2 库均可成功执行。
- 新增索引命中关键查询（Agent 列表、资产列表、消息列表、向量检索）。
- 预览与正式对话数据互不串表。
- 任意资产伪删除后，不影响历史日志可读性。
