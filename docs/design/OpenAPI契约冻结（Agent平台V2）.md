# OpenAPI 契约冻结（Agent 平台 V2）

## 1. 目标
- 冻结后端与前端联调所需的接口契约，避免联调期间字段频繁变化。
- 契约范围覆盖认证、资产管理、Agent 配置、预览对话、正式对话。
- 所有接口统一使用 `ApiResponseObject<T>` 响应包装。

## 2. 全局约定
- Base URL：`/api`
- 认证方式：`Authorization: Bearer {token}`
- 时间字段：ISO-8601 字符串（UTC）
- 分页请求：`pageNo`（>=1）、`pageSize`（1~100）、`keyword`（可选）
- 分页响应：`PageResponseObject<T>`，字段为 `total`、`pageNo`、`pageSize`、`records`
- 统一响应：
  - 成功：`code=0`
  - 失败：`code!=0`

## 3. 认证接口

### 3.1 注册
- `POST /api/auth/register`
- 请求体：`RegisterRequestObject`
  - `username string`
  - `password string`
- 响应体：`ApiResponseObject<Void>`

### 3.2 登录
- `POST /api/auth/login`
- 请求体：`LoginRequestObject`
  - `username string`
  - `password string`
- 响应体：`ApiResponseObject<TokenResponseObject>`
  - `accessToken string`

## 4. 资产管理接口

### 4.1 知识库
- `POST /api/knowledge-bases`
  - 请求：`CreateKnowledgeBaseRequestObject{name,description,embeddingProvider,embeddingModel}`
  - 响应：`ApiResponseObject<KnowledgeBaseResponseObject>`
- `GET /api/knowledge-bases`
  - 查询：`pageNo,pageSize,keyword`
  - 响应：`ApiResponseObject<PageResponseObject<KnowledgeBaseResponseObject>>`
- `GET /api/knowledge-bases/{kbId}`
  - 响应：`ApiResponseObject<KnowledgeBaseDetailResponseObject>`
- `PATCH /api/knowledge-bases/{kbId}`
  - 请求：`UpdateKnowledgeBaseRequestObject{name,description,status}`
  - 响应：`ApiResponseObject<Void>`
- `DELETE /api/knowledge-bases/{kbId}`
  - 响应：`ApiResponseObject<Void>`

### 4.2 知识库文件
- `POST /api/knowledge-bases/{kbId}/files`（multipart/form-data）
  - 表单：`file`
  - 响应：`ApiResponseObject<KbFileResponseObject>`
- `GET /api/knowledge-bases/{kbId}/files`
  - 查询：`pageNo,pageSize,parseStatus`
  - 响应：`ApiResponseObject<PageResponseObject<KbFileResponseObject>>`
- `GET /api/knowledge-bases/{kbId}/files/{fileId}`
  - 响应：`ApiResponseObject<KbFileDetailResponseObject>`
- `DELETE /api/knowledge-bases/{kbId}/files/{fileId}`
  - 响应：`ApiResponseObject<Void>`

### 4.3 Skills
- `POST /api/skills`
  - 请求：`CreateSkillRequestObject{name,description,version,schemaJson,runtimeConfigJson}`
  - 响应：`ApiResponseObject<SkillResponseObject>`
- `GET /api/skills`
  - 查询：`pageNo,pageSize,keyword,status`
  - 响应：`ApiResponseObject<PageResponseObject<SkillResponseObject>>`
- `GET /api/skills/{skillId}`
  - 响应：`ApiResponseObject<SkillDetailResponseObject>`
- `PATCH /api/skills/{skillId}`
  - 请求：`UpdateSkillRequestObject{name,description,status,runtimeConfigJson}`
  - 响应：`ApiResponseObject<Void>`
- `DELETE /api/skills/{skillId}`
  - 响应：`ApiResponseObject<Void>`

### 4.4 Tools
- `POST /api/tools`
  - 请求：`CreateToolRequestObject{name,description,toolType,configJson,authConfigJson}`
  - 响应：`ApiResponseObject<ToolResponseObject>`
- `GET /api/tools`
  - 查询：`keyword,toolType,status`
  - 响应：`ApiResponseObject<List<ToolResponseObject>>`
- `GET /api/tools/{toolId}`
  - 响应：`ApiResponseObject<ToolDetailResponseObject>`
- `PATCH /api/tools/{toolId}`
  - 请求：`UpdateToolRequestObject{name,description,status,configJson,authConfigJson}`
  - 响应：`ApiResponseObject<Void>`
- `DELETE /api/tools/{toolId}`
  - 响应：`ApiResponseObject<Void>`
- `POST /api/tools/{toolId}/test`
  - 请求：`ToolTestRequestObject{inputJson}`
  - 响应：`ApiResponseObject<ToolTestResponseObject>`

## 5. Agent 配置接口

### 5.1 Agent 基础
- `POST /api/agents`
  - 请求：`CreateAgentRequestObject{name,description,avatarUrl,modelConfigJson}`
  - 响应：`ApiResponseObject<AgentResponseObject>`
- `GET /api/agents`
  - 查询：`pageNo,pageSize,keyword,status`
  - 响应：`ApiResponseObject<PageResponseObject<AgentResponseObject>>`
- `GET /api/agents/{agentId}`
  - 响应：`ApiResponseObject<AgentDetailResponseObject>`
- `PATCH /api/agents/{agentId}`
  - 请求：`UpdateAgentRequestObject{name,description,avatarUrl,modelConfigJson}`
  - 响应：`ApiResponseObject<Void>`
- `DELETE /api/agents/{agentId}`
  - 响应：`ApiResponseObject<Void>`

### 5.2 Agent 绑定资产
- `PUT /api/agents/{agentId}/skills`
  - 请求：`BindSkillsRequestObject{skillIds[]}`
  - 响应：`ApiResponseObject<Void>`
- `PUT /api/agents/{agentId}/tools`
  - 请求：`BindToolsRequestObject{toolIds[]}`
  - 响应：`ApiResponseObject<Void>`
- `PUT /api/agents/{agentId}/knowledge-bases`
  - 请求：`BindKnowledgeBasesRequestObject{knowledgeBaseIds[]}`
  - 响应：`ApiResponseObject<Void>`
- `POST /api/agents/{agentId}/publish`
  - 响应：`ApiResponseObject<Void>`
- `POST /api/agents/{agentId}/archive`
  - 响应：`ApiResponseObject<Void>`

## 6. 预览对话接口
- `POST /api/agents/{agentId}/preview-sessions`
  - 请求：`CreatePreviewSessionRequestObject{draftConfigOverrideJson}`
  - 响应：`ApiResponseObject<PreviewSessionResponseObject>`
- `POST /api/preview-sessions/{previewSessionId}/messages`
  - 请求：`SendPreviewMessageRequestObject{content}`
  - 响应：`ApiResponseObject<PreviewMessageResponseObject>`
- `GET /api/preview-sessions/{previewSessionId}/messages`
  - 查询：`pageNo,pageSize`
  - 响应：`ApiResponseObject<PageResponseObject<PreviewMessageResponseObject>>`
- `GET /api/preview-sessions/{previewSessionId}/stream`（SSE）
  - 事件：`token`、`tool_call`、`skill_exec`、`done`、`error`
- `POST /api/preview-sessions/{previewSessionId}/close`
  - 响应：`ApiResponseObject<Void>`

## 7. 正式对话接口
- `POST /api/agents/{agentId}/conversations`
  - 请求：`CreateConversationRequestObject{title}`
  - 响应：`ApiResponseObject<ConversationResponseObject>`
- `GET /api/agents/{agentId}/conversations`
  - 查询：`pageNo,pageSize,keyword`
  - 响应：`ApiResponseObject<PageResponseObject<ConversationResponseObject>>`
- `POST /api/conversations/{conversationId}/messages`
  - 请求：`SendMessageRequestObject{content}`
  - 响应：`ApiResponseObject<MessageResponseObject>`
- `GET /api/conversations/{conversationId}/messages`
  - 查询：`pageNo,pageSize`
  - 响应：`ApiResponseObject<PageResponseObject<MessageResponseObject>>`
- `GET /api/conversations/{conversationId}/stream`（SSE）
  - 事件：`token`、`tool_call`、`skill_exec`、`done`、`error`
- `POST /api/conversations/{conversationId}/close`
  - 响应：`ApiResponseObject<Void>`

## 8. 运行日志接口
- `GET /api/agents/{agentId}/tool-call-logs`
  - 查询：`pageNo,pageSize,status`
  - 响应：`ApiResponseObject<PageResponseObject<ToolCallLogResponseObject>>`
- `GET /api/agents/{agentId}/skill-exec-logs`
  - 查询：`pageNo,pageSize,status`
  - 响应：`ApiResponseObject<PageResponseObject<SkillExecLogResponseObject>>`

## 9. 变更控制
- 冻结版本：`v2.0.0-contract`
- 允许变更：
  - 新增可选字段
  - 新增不影响现有调用的接口
- 禁止变更：
  - 删除字段
  - 修改字段含义或类型
  - 修改既有接口路径与方法
