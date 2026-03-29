package org.lkf.agent.controller;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.lkf.agent.common.context.UserContext;
import org.lkf.agent.common.dto.ApiResponseObject;
import org.lkf.agent.dto.ConversationResponseObject;
import org.lkf.agent.service.ConversationAppService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.lkf.agent.dto.CreateConversationRequestObject;

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

    /**
     * 构造器。
     *
     * @param conversationAppService 会话应用服务
     */
    public ConversationController(ConversationAppService conversationAppService) {
        this.conversationAppService = conversationAppService;
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
    public ApiResponseObject<List<ConversationResponseObject>> list() {
        String username = UserContext.getCurrentUsername();
        return ApiResponseObject.success(conversationAppService.listConversations(username));
    }
}
