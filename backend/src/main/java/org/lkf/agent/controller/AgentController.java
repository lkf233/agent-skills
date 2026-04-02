package org.lkf.agent.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.lkf.agent.common.context.UserContext;
import org.lkf.agent.common.dto.ApiResponseObject;
import org.lkf.agent.dto.AgentResponseObject;
import org.lkf.agent.dto.BindKnowledgeBasesRequestObject;
import org.lkf.agent.dto.BindToolsRequestObject;
import org.lkf.agent.dto.CreateAgentRequestObject;
import org.lkf.agent.dto.UpdateAgentRequestObject;
import org.lkf.agent.service.AgentAppService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/agents")
@Tag(name = "Agent接口")
@SecurityRequirement(name = "BearerAuth")
public class AgentController {

    private final AgentAppService agentAppService;

    public AgentController(AgentAppService agentAppService) {
        this.agentAppService = agentAppService;
    }

    @PostMapping
    @Operation(summary = "创建Agent", description = "创建Agent并初始化绑定工具和知识库")
    public ApiResponseObject<AgentResponseObject> create(@Valid @RequestBody CreateAgentRequestObject requestObject) {
        String username = UserContext.getCurrentUsername();
        return ApiResponseObject.success(agentAppService.createAgent(username, requestObject));
    }

    @GetMapping
    @Operation(summary = "查询Agent列表", description = "查询当前用户Agent列表")
    public ApiResponseObject<List<AgentResponseObject>> list() {
        String username = UserContext.getCurrentUsername();
        return ApiResponseObject.success(agentAppService.listAgents(username));
    }

    @GetMapping("/{agentId}")
    @Operation(summary = "查询Agent详情", description = "查询Agent基础信息和已绑定资产")
    public ApiResponseObject<AgentResponseObject> detail(@PathVariable("agentId") String agentId) {
        String username = UserContext.getCurrentUsername();
        return ApiResponseObject.success(agentAppService.getAgent(username, agentId));
    }

    @PatchMapping("/{agentId}")
    @Operation(summary = "更新Agent基础信息", description = "更新Agent名称、描述、系统提示词、头像")
    public ApiResponseObject<Void> update(@PathVariable("agentId") String agentId,
                                          @RequestBody UpdateAgentRequestObject requestObject) {
        String username = UserContext.getCurrentUsername();
        agentAppService.updateAgent(username, agentId, requestObject);
        return ApiResponseObject.success(null);
    }

    @DeleteMapping("/{agentId}")
    @Operation(summary = "删除Agent", description = "删除Agent（伪删除）")
    public ApiResponseObject<Void> delete(@PathVariable("agentId") String agentId) {
        String username = UserContext.getCurrentUsername();
        agentAppService.deleteAgent(username, agentId);
        return ApiResponseObject.success(null);
    }

    @PutMapping("/{agentId}/tools")
    @Operation(summary = "绑定Agent工具", description = "覆盖绑定Agent工具集合")
    public ApiResponseObject<Void> bindTools(@PathVariable("agentId") String agentId,
                                             @RequestBody(required = false) BindToolsRequestObject requestObject) {
        String username = UserContext.getCurrentUsername();
        agentAppService.bindTools(username, agentId, requestObject);
        return ApiResponseObject.success(null);
    }

    @PutMapping("/{agentId}/knowledge-bases")
    @Operation(summary = "绑定Agent知识库", description = "覆盖绑定Agent知识库集合")
    public ApiResponseObject<Void> bindKnowledgeBases(@PathVariable("agentId") String agentId,
                                                      @RequestBody(required = false) BindKnowledgeBasesRequestObject requestObject) {
        String username = UserContext.getCurrentUsername();
        agentAppService.bindKnowledgeBases(username, agentId, requestObject);
        return ApiResponseObject.success(null);
    }
}
