package org.lkf.agent.controller;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.lkf.agent.common.context.UserContext;
import org.lkf.agent.common.dto.ApiResponseObject;
import org.lkf.agent.dto.ConversationMessagePageResponseObject;
import org.lkf.agent.dto.ConversationResponseObject;
import org.lkf.agent.dto.ConversationSummaryResponseObject;
import org.lkf.agent.dto.CreateConversationRequestObject;
import org.lkf.agent.dto.SendMessageRequestObject;
import org.lkf.agent.service.ChatRuntimeService;
import org.lkf.agent.service.ConversationAppService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * 会话控制器。
 */
@RestController
@Validated
@RequestMapping("/api/conversations")
@Tag(name = "会话接口")
@SecurityRequirement(name = "BearerAuth")
public class ConversationController {

    /**
     * 会话应用服务。
     */
    private final ConversationAppService conversationAppService;
    private final ChatRuntimeService chatRuntimeService;

    /**
     * 构造器。
     *
     * @param conversationAppService 会话应用服务
     */
    public ConversationController(ConversationAppService conversationAppService, ChatRuntimeService chatRuntimeService) {
        this.conversationAppService = conversationAppService;
        this.chatRuntimeService = chatRuntimeService;
    }

    /**
     * 创建会话接口。
     *
     * @param requestObject 创建会话请求对象
     * @return 会话响应对象
     */
    @PostMapping
    @Operation(summary = "创建会话", description = "为当前登录用户创建新的会话")
    public ApiResponseObject<ConversationResponseObject> create(@Valid @RequestBody CreateConversationRequestObject requestObject) {
        String username = UserContext.getCurrentUsername();
        return ApiResponseObject.success(conversationAppService.createConversation(username, requestObject));
    }

    /**
     * 查询会话列表接口。
     *
     * @return 会话响应对象列表
     */
    @GetMapping
    @Operation(summary = "查询会话列表", description = "查询当前登录用户的会话列表")
    public ApiResponseObject<List<ConversationResponseObject>> list(
            @RequestParam(value = "agentId", required = false) String agentId) {
        String username = UserContext.getCurrentUsername();
        return ApiResponseObject.success(conversationAppService.listConversations(username, agentId));
    }

    @GetMapping("/{conversationId}/messages")
    @Operation(summary = "查询会话消息", description = "分页查询指定会话历史消息")
    public ApiResponseObject<ConversationMessagePageResponseObject> listMessages(@PathVariable("conversationId") String conversationId,
                                                                                  @RequestParam(value = "page", required = false) Integer page,
                                                                                  @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        String username = UserContext.getCurrentUsername();
        return ApiResponseObject.success(conversationAppService.listMessages(username, conversationId, page, pageSize));
    }

    @GetMapping("/{conversationId}/summaries")
    @Operation(summary = "查询会话摘要", description = "查询会话历史压缩摘要列表")
    public ApiResponseObject<List<ConversationSummaryResponseObject>> listSummaries(@PathVariable("conversationId") String conversationId,
                                                                                     @RequestParam(value = "limit", required = false) Integer limit) {
        String username = UserContext.getCurrentUsername();
        return ApiResponseObject.success(conversationAppService.listSummaries(username, conversationId, limit));
    }

    @PostMapping("/{conversationId}/stream")
    @Operation(summary = "会话流式输出", description = "保存用户消息后触发Agent流式应答")
    public SseEmitter stream(@PathVariable("conversationId") String conversationId,
                             @Valid @RequestBody SendMessageRequestObject requestObject) {
        String username = UserContext.getCurrentUsername();
        return chatRuntimeService.streamConversation(username, conversationId, requestObject.getContent());
    }
}
