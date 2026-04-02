# Chat Runtime 端到端流程

## 1. MVC入口

- 前端调用 `GET /api/conversations/{conversationId}/stream`
- `ConversationController.stream` 进入 `ChatRuntimeService.streamConversation`
- `ChatRuntimeService` 在单类内统一处理异常与 SSE `error` 事件
- `ChatRuntimeService` 通过 `RuntimeMessageHandlerFactory` 选择 `AgentRuntimeMessageHandler` 执行具体会话逻辑

## 2. 运行时准备

- 加载用户、会话、Agent、最新用户问题
- 加载知识库ID与工具ID
- `RuntimeMcpToolProvider.refreshSnapshots` 刷新每个已绑定工具的 `tools/list` 快照
- 构造摘要上下文、历史上下文、工具目录上下文

## 3. LangChain4j Agent构建

- `AgentRuntimeMessageHandler` 按 `buildStreamingAgent` 方式构建：`AiServices.builder(...).streamingChatModel(...).chatMemory(...)`
- 内置工具通过 `@Tool` 方法对象注入 `agentService.tools(runtimeTools)`
- 外部工具通过 `RuntimeMcpAgentToolManager.createToolProvider(...)` 注入 `agentService.toolProvider(...)`
- 工具注册为两个显式工具：
  - `rag_search`：调用 `RuntimeRagToolProvider.search`
- MCP工具：由 `ToolProvider` 从 MCP 服务动态发现并注册，模型按工具名直接调用
- 模型根据系统提示和工具描述自主决定是否调用工具

## 4. 工具执行与回调

- 当模型触发工具调用时，`TokenStream.onToolExecuted` 回调执行
- 回调中统一完成：
  - SSE 事件推送 `tool_call/skill_exec`
  - `RuntimeToolCallHook` 落库 `tool_call_log`
- MCP 工具可用性来自每轮会话开始前的 `tools/list` 快照刷新

## 5. 最终回答

- Token 通过 `TokenStream.onPartialResponse` 持续推送 SSE `token`
- 会话完成后保存 assistant 消息并发送 SSE `done`
