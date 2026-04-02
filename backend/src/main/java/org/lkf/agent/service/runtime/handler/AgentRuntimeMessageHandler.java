package org.lkf.agent.service.runtime.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.ToolProvider;
import org.lkf.agent.common.exception.BusinessException;
import org.lkf.agent.dto.ToolManifestSnapshotResponseObject;
import org.lkf.agent.entity.AgentEntity;
import org.lkf.agent.entity.ConversationEntity;
import org.lkf.agent.entity.ConversationMessageEntity;
import org.lkf.agent.entity.ConversationSummaryEntity;
import org.lkf.agent.entity.UserAccountEntity;
import org.lkf.agent.mapper.AgentMapper;
import org.lkf.agent.mapper.ConversationMessageMapper;
import org.lkf.agent.mapper.ConversationMapper;
import org.lkf.agent.mapper.ConversationSummaryMapper;
import org.lkf.agent.service.AuthAppService;
import org.lkf.agent.service.OpenAiChatService;
import org.lkf.agent.service.runtime.hook.RuntimeToolCallHook;
import org.lkf.agent.service.runtime.tool.RuntimeMcpAgentToolManager;
import org.lkf.agent.service.runtime.tool.RuntimeMcpToolProvider;
import org.lkf.agent.service.runtime.tool.RuntimeRagToolProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class AgentRuntimeMessageHandler implements RuntimeMessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(AgentRuntimeMessageHandler.class);
    private static final int MAX_SUMMARY_CHARS = 2500;
    private static final int MAX_HISTORY_CHARS = 3000;

    private final AuthAppService authAppService;
    private final ConversationMapper conversationMapper;
    private final ConversationMessageMapper conversationMessageMapper;
    private final ConversationSummaryMapper conversationSummaryMapper;
    private final AgentMapper agentMapper;
    private final OpenAiChatService openAiChatService;
    private final ObjectMapper objectMapper;
    private final RuntimeMcpAgentToolManager runtimeMcpAgentToolManager;
    private final RuntimeMcpToolProvider runtimeMcpToolProvider;
    private final RuntimeRagToolProvider runtimeRagToolProvider;
    private final RuntimeToolCallHook runtimeToolCallHook;

    public AgentRuntimeMessageHandler(AuthAppService authAppService,
                                      ConversationMapper conversationMapper,
                                      ConversationMessageMapper conversationMessageMapper,
                                      ConversationSummaryMapper conversationSummaryMapper,
                                      AgentMapper agentMapper,
                                      OpenAiChatService openAiChatService,
                                      ObjectMapper objectMapper,
                                      RuntimeMcpAgentToolManager runtimeMcpAgentToolManager,
                                      RuntimeMcpToolProvider runtimeMcpToolProvider,
                                      RuntimeRagToolProvider runtimeRagToolProvider,
                                      RuntimeToolCallHook runtimeToolCallHook) {
        this.authAppService = authAppService;
        this.conversationMapper = conversationMapper;
        this.conversationMessageMapper = conversationMessageMapper;
        this.conversationSummaryMapper = conversationSummaryMapper;
        this.agentMapper = agentMapper;
        this.openAiChatService = openAiChatService;
        this.objectMapper = objectMapper;
        this.runtimeMcpAgentToolManager = runtimeMcpAgentToolManager;
        this.runtimeMcpToolProvider = runtimeMcpToolProvider;
        this.runtimeRagToolProvider = runtimeRagToolProvider;
        this.runtimeToolCallHook = runtimeToolCallHook;
    }

    @Override
    public String key() {
        return "agent";
    }

    @Override
    public void handle(String username, String conversationId, SseEmitter emitter) throws IOException {
        UserAccountEntity user = authAppService.getUserByUsername(username);
        ConversationEntity conversation = mustFindConversation(conversationId, user.getId());
        AgentEntity agent = mustFindAgent(conversation.getAgentId(), user.getId());
        String userQuestion = latestUserQuestion(conversationId);
        if (userQuestion.isBlank()) {
            throw new BusinessException("未找到用户消息");
        }
        List<String> kbIds = agentMapper.listKnowledgeBaseIdsByAgentId(agent.getId());
        List<String> toolIds = agentMapper.listToolIdsByAgentId(agent.getId());
        Map<String, ToolManifestSnapshotResponseObject> toolSnapshotMap = runtimeMcpToolProvider.refreshSnapshots(username, toolIds);
        String toolCatalogContext = runtimeMcpToolProvider.buildToolCatalogContext(toolSnapshotMap);
        MessageWindowChatMemory memory = buildMemory(agent, conversationId, toolCatalogContext, userQuestion);
        RuntimeTools runtimeTools = new RuntimeTools(username, conversationId, kbIds, toolIds, toolSnapshotMap);
        RuntimeAgent runtimeAgent = buildStreamingAgent(memory, runtimeTools);
        String answer = streamWithAgent(runtimeAgent, runtimeTools, userQuestion, conversationId, emitter);
        persistAssistantMessage(conversationId, user.getId(), answer);
        sendEvent(emitter, "done", objectMapper.createObjectNode().put("status", "DONE"));
        emitter.complete();
    }

    private RuntimeAgent buildStreamingAgent(MessageWindowChatMemory memory, RuntimeTools runtimeTools) {
        ToolProvider toolProvider = provideTools(runtimeTools);
        AiServices<RuntimeAgent> agentService = AiServices.builder(RuntimeAgent.class)
                .streamingChatModel(openAiChatService.buildStreamingChatModel())
                .chatMemory(memory)
                .tools(runtimeTools);
        if (toolProvider != null) {
            agentService.toolProvider(toolProvider);
        }
        return agentService.build();
    }

    private ToolProvider provideTools(RuntimeTools runtimeTools) {
        return runtimeMcpAgentToolManager.createToolProvider(runtimeTools.username, runtimeTools.toolIds);
    }

    private ConversationEntity mustFindConversation(String conversationId, Long userId) {
        ConversationEntity conversation = conversationMapper.findByIdAndUserId(conversationId, userId);
        if (conversation == null) {
            throw new BusinessException("会话不存在");
        }
        return conversation;
    }

    private AgentEntity mustFindAgent(String agentId, Long userId) {
        if (agentId == null || agentId.isBlank()) {
            throw new BusinessException("会话未绑定Agent");
        }
        AgentEntity agent = agentMapper.findByIdAndUserId(agentId, userId);
        if (agent == null) {
            throw new BusinessException("Agent不存在");
        }
        return agent;
    }

    private String latestUserQuestion(String conversationId) {
        List<ConversationMessageEntity> latest = conversationMessageMapper.listByConversationId(conversationId, 0, 20);
        if (latest == null || latest.isEmpty()) {
            return "";
        }
        for (ConversationMessageEntity message : latest) {
            if ("USER".equalsIgnoreCase(message.getRole())) {
                return message.getContent() == null ? "" : message.getContent().trim();
            }
        }
        return "";
    }

    private String buildSummaryContext(String conversationId) {
        List<ConversationSummaryEntity> summaries = conversationSummaryMapper.listByConversationId(conversationId, 3);
        if (summaries == null || summaries.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (ConversationSummaryEntity summary : summaries) {
            builder.append(summary.getSummaryText() == null ? "" : summary.getSummaryText()).append("\n");
        }
        return truncate(builder.toString().trim(), MAX_SUMMARY_CHARS);
    }

    private String buildHistoryContext(String conversationId) {
        List<ConversationMessageEntity> messages = conversationMessageMapper.listByConversationId(conversationId, 0, 12);
        if (messages == null || messages.isEmpty()) {
            return "";
        }
        Collections.reverse(messages);
        StringBuilder builder = new StringBuilder();
        for (ConversationMessageEntity message : messages) {
            builder.append(normalizeRole(message.getRole())).append(": ").append(message.getContent() == null ? "" : message.getContent()).append("\n");
        }
        return truncate(builder.toString().trim(), MAX_HISTORY_CHARS);
    }

    private void persistAssistantMessage(String conversationId, Long userId, String content) {
        Integer maxSeqNo = conversationMessageMapper.findMaxSeqNo(conversationId);
        int seqNo = (maxSeqNo == null ? 0 : maxSeqNo) + 1;
        ConversationMessageEntity entity = new ConversationMessageEntity();
        entity.setId(UUID.randomUUID().toString().replace("-", ""));
        entity.setConversationId(conversationId);
        entity.setUserId(userId);
        entity.setRole("ASSISTANT");
        entity.setContent(content);
        entity.setMetadataJson("{}");
        entity.setTokenInput(0);
        entity.setTokenOutput(0);
        entity.setSeqNo(seqNo);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setDelFlag(0);
        conversationMessageMapper.insert(entity);
    }

    private void sendEvent(SseEmitter emitter, String event, JsonNode data) throws IOException {
        emitter.send(SseEmitter.event().name(event).data(data));
    }

    private String streamWithAgent(RuntimeAgent runtimeAgent, RuntimeTools runtimeTools, String userQuestion,
                                   String conversationId, SseEmitter emitter) {
        StringBuilder builder = new StringBuilder();
        AtomicReference<Throwable> errorRef = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        TokenStream tokenStream = runtimeAgent.chat(userQuestion);

        tokenStream.onPartialResponse(token -> {
            builder.append(token);
            try {
                sendEvent(emitter, "token", objectMapper.createObjectNode().put("content", token));
            } catch (IOException exception) {
                errorRef.set(exception);
            }
        });

        tokenStream.onToolExecuted(toolExecution -> {
            RuntimeTools.ToolLogData logData = runtimeTools.extractToolLog(toolExecution.request().name(), toolExecution.request().arguments(), toolExecution.result());
            runtimeToolCallHook.onToolCallCompleted(
                    conversationId,
                    logData.toolDefId(),
                    logData.toolName(),
                    logData.request(),
                    logData.response(),
                    true,
                    logData.latencyMs()
            );
            sendToolEvent(emitter, logData.toolName(), "SUCCESS");
        });

        tokenStream.onCompleteResponse(chatResponse -> latch.countDown());

        tokenStream.onError(throwable -> {
            errorRef.set(throwable);
            latch.countDown();
        });
        tokenStream.start();

        awaitLatch(latch);
        if (errorRef.get() != null) {
            throw new BusinessException("流式输出失败: " + errorRef.get().getMessage());
        }
        String answer = builder.toString().trim();
        if (answer.isBlank()) {
            throw new BusinessException("模型未返回有效内容");
        }
        return answer;
    }

    private void awaitLatch(CountDownLatch latch) {
        try {
            boolean completed = latch.await(120, TimeUnit.SECONDS);
            if (!completed) {
                throw new BusinessException("流式输出超时");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new BusinessException("流式输出中断");
        }
    }

    private void sendToolEvent(SseEmitter emitter, String toolName, String status) {
        try {
            sendEvent(emitter, "tool_call", objectMapper.createObjectNode().put("tool", toolName).put("status", status));
            sendEvent(emitter, "skill_exec", objectMapper.createObjectNode().put("name", toolName).put("status", status));
        } catch (IOException exception) {
            logger.warn("发送工具事件失败", exception);
        }
    }

    private MessageWindowChatMemory buildMemory(AgentEntity agent, String conversationId, String toolCatalogContext, String userQuestion) {
        MessageWindowChatMemory memory = MessageWindowChatMemory.builder()
                .maxMessages(100)
                .chatMemoryStore(new InMemoryChatMemoryStore())
                .build();
        String summaryContext = buildSummaryContext(conversationId);
        String historyContext = buildHistoryContext(conversationId);
        String systemPrompt = """
                %s

                工具调用规则：
                - 需要检索知识库时调用工具 rag_search
                - 需要调用外部系统时优先调用可用MCP工具目录中的具体工具名
                - 能直接回答时不要调用工具

                可用MCP工具目录：
                %s

                历史摘要：
                %s

                历史消息：
                %s
                """.formatted(safeText(agent.getSystemPrompt()),
                safeText(toolCatalogContext),
                safeText(summaryContext),
                safeText(historyContext));
        memory.add(SystemMessage.from(systemPrompt));
        List<ConversationMessageEntity> messages = conversationMessageMapper.listByConversationId(conversationId, 0, 20);
        if (messages == null || messages.isEmpty()) {
            return memory;
        }
        Collections.reverse(messages);
        boolean skippedLatestUser = false;
        for (int i = messages.size() - 1; i >= 0; i--) {
            ConversationMessageEntity message = messages.get(i);
            if ("USER".equalsIgnoreCase(message.getRole())) {
                String content = message.getContent() == null ? "" : message.getContent().trim();
                if (!skippedLatestUser && content.equals(userQuestion)) {
                    skippedLatestUser = true;
                    continue;
                }
                break;
            }
        }
        for (ConversationMessageEntity message : messages) {
            String role = normalizeRole(message.getRole());
            String content = message.getContent() == null ? "" : message.getContent().trim();
            if (content.isBlank()) {
                continue;
            }
            if ("USER".equals(role)) {
                if (!skippedLatestUser && content.equals(userQuestion)) {
                    skippedLatestUser = true;
                    continue;
                }
                memory.add(UserMessage.from(content));
                continue;
            }
            if ("ASSISTANT".equals(role)) {
                memory.add(AiMessage.from(content));
            }
        }
        return memory;
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return "USER";
        }
        return role.trim().toUpperCase(Locale.ROOT);
    }

    private String safeText(String value) {
        return value == null ? "" : value.trim();
    }

    private String truncate(String value, int maxChars) {
        String text = value == null ? "" : value;
        if (maxChars <= 0 || text.length() <= maxChars) {
            return text;
        }
        return text.substring(0, maxChars);
    }

    private interface RuntimeAgent {
        TokenStream chat(@dev.langchain4j.service.UserMessage String userMessage);
    }

    private class RuntimeTools {

        private final String username;
        private final List<String> kbIds;
        private final List<String> toolIds;
        private final Map<String, ToolManifestSnapshotResponseObject> toolSnapshotMap;
        private final AtomicReference<Long> startedAtRef = new AtomicReference<>(0L);

        RuntimeTools(String username, String conversationId, List<String> kbIds, List<String> toolIds,
                     Map<String, ToolManifestSnapshotResponseObject> toolSnapshotMap) {
            this.username = username;
            this.kbIds = kbIds;
            this.toolIds = toolIds;
            this.toolSnapshotMap = toolSnapshotMap;
        }

        @Tool("搜索知识库并返回相关片段")
        public String rag_search(String query) {
            startedAtRef.set(System.currentTimeMillis());
            String effectiveQuery = query == null || query.isBlank() ? "" : query.trim();
            JsonNode result = runtimeRagToolProvider.search(kbIds, effectiveQuery);
            return result.toString();
        }

        ToolLogData extractToolLog(String requestName, String requestArguments, String resultText) {
            long latencyMs = System.currentTimeMillis() - startedAtRef.get();
            JsonNode requestNode = parseJson(requestArguments);
            JsonNode responseNode = parseJson(resultText);
            if ("rag_search".equals(requestName)) {
                return new ToolLogData("RAG", "rag_search", requestNode, responseNode, latencyMs);
            }
            return new ToolLogData(findToolDefIdByRemoteName(requestName), requestName, requestNode, responseNode, latencyMs);
        }

        private String findToolDefIdByRemoteName(String remoteToolName) {
            if (remoteToolName == null || remoteToolName.isBlank() || toolSnapshotMap == null || toolSnapshotMap.isEmpty()) {
                return "";
            }
            for (Map.Entry<String, ToolManifestSnapshotResponseObject> entry : toolSnapshotMap.entrySet()) {
                ToolManifestSnapshotResponseObject snapshot = entry.getValue();
                if (snapshot == null || snapshot.getManifest() == null || !snapshot.getManifest().isArray()) {
                    continue;
                }
                for (JsonNode toolNode : snapshot.getManifest()) {
                    if (toolNode != null && toolNode.get("name") != null && remoteToolName.equals(toolNode.get("name").asText())) {
                        return entry.getKey();
                    }
                }
            }
            return "";
        }

        private JsonNode parseJson(String raw) {
            try {
                if (raw == null || raw.isBlank()) {
                    return objectMapper.createObjectNode();
                }
                JsonNode node = objectMapper.readTree(raw);
                return node == null ? objectMapper.createObjectNode() : node;
            } catch (Exception exception) {
                return objectMapper.createObjectNode().put("raw", raw == null ? "" : raw);
            }
        }

        record ToolLogData(String toolDefId, String toolName, JsonNode request, JsonNode response, long latencyMs) {
        }
    }
}
