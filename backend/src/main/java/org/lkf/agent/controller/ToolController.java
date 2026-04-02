package org.lkf.agent.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.lkf.agent.common.context.UserContext;
import org.lkf.agent.common.dto.ApiResponseObject;
import org.lkf.agent.dto.CreateToolRequestObject;
import org.lkf.agent.dto.ToolManifestSnapshotResponseObject;
import org.lkf.agent.dto.ToolResponseObject;
import org.lkf.agent.dto.ToolRemoteToolsResponseObject;
import org.lkf.agent.dto.ToolTestRequestObject;
import org.lkf.agent.dto.ToolTestResponseObject;
import org.lkf.agent.dto.UpdateToolRequestObject;
import org.lkf.agent.service.ToolAppService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/tools")
@Tag(name = "工具接口")
@SecurityRequirement(name = "BearerAuth")
public class ToolController {

    private final ToolAppService toolAppService;

    public ToolController(ToolAppService toolAppService) {
        this.toolAppService = toolAppService;
    }

    @PostMapping
    @Operation(summary = "创建工具", description = "创建远程MCP工具配置")
    public ApiResponseObject<ToolResponseObject> create(@Valid @RequestBody CreateToolRequestObject requestObject) {
        String username = UserContext.getCurrentUsername();
        return ApiResponseObject.success(toolAppService.createTool(username, requestObject));
    }

    @GetMapping
    @Operation(summary = "查询工具列表", description = "查询当前用户工具列表，不分页返回")
    public ApiResponseObject<List<ToolResponseObject>> list(@RequestParam(value = "keyword", required = false) String keyword,
                                                            @RequestParam(value = "toolType", required = false) String toolType,
                                                            @RequestParam(value = "status", required = false) String status) {
        String username = UserContext.getCurrentUsername();
        return ApiResponseObject.success(toolAppService.listTools(username, keyword, toolType, status));
    }

    @GetMapping("/{toolId}")
    @Operation(summary = "查询工具详情", description = "查询单个工具详情")
    public ApiResponseObject<ToolResponseObject> detail(@PathVariable("toolId") String toolId) {
        String username = UserContext.getCurrentUsername();
        return ApiResponseObject.success(toolAppService.getTool(username, toolId));
    }

    @PatchMapping("/{toolId}")
    @Operation(summary = "更新工具", description = "更新工具配置和状态")
    public ApiResponseObject<Void> update(@PathVariable("toolId") String toolId,
                                          @RequestBody UpdateToolRequestObject requestObject) {
        String username = UserContext.getCurrentUsername();
        toolAppService.updateTool(username, toolId, requestObject);
        return ApiResponseObject.success(null);
    }

    @DeleteMapping("/{toolId}")
    @Operation(summary = "删除工具", description = "删除工具（伪删除）")
    public ApiResponseObject<Void> delete(@PathVariable("toolId") String toolId) {
        String username = UserContext.getCurrentUsername();
        toolAppService.deleteTool(username, toolId);
        return ApiResponseObject.success(null);
    }

    @PostMapping("/{toolId}/test")
    @Operation(summary = "测试工具连通性", description = "测试远程MCP工具连通情况")
    public ApiResponseObject<ToolTestResponseObject> test(@PathVariable("toolId") String toolId,
                                                          @RequestBody(required = false) ToolTestRequestObject requestObject) {
        String username = UserContext.getCurrentUsername();
        return ApiResponseObject.success(toolAppService.testTool(username, toolId, requestObject));
    }

    @GetMapping("/{toolId}/remote-tools")
    @Operation(summary = "查询远程MCP工具列表", description = "验证远程MCP服务有效性并返回tools/list工具清单")
    public ApiResponseObject<ToolRemoteToolsResponseObject> listRemoteTools(@PathVariable("toolId") String toolId) {
        String username = UserContext.getCurrentUsername();
        return ApiResponseObject.success(toolAppService.listRemoteTools(username, toolId));
    }

    @GetMapping("/{toolId}/manifest-snapshot")
    @Operation(summary = "查询工具清单快照", description = "查询tools/list快照，支持按需刷新过期缓存")
    public ApiResponseObject<ToolManifestSnapshotResponseObject> getManifestSnapshot(@PathVariable("toolId") String toolId,
                                                                                      @RequestParam(value = "refreshIfExpired", required = false) Boolean refreshIfExpired) {
        String username = UserContext.getCurrentUsername();
        boolean shouldRefreshIfExpired = refreshIfExpired == null || refreshIfExpired;
        return ApiResponseObject.success(toolAppService.getToolManifestSnapshot(username, toolId, shouldRefreshIfExpired));
    }

    @PostMapping("/{toolId}/manifest-snapshot/refresh")
    @Operation(summary = "刷新工具清单快照", description = "主动刷新远程MCP tools/list并覆盖本地快照")
    public ApiResponseObject<ToolManifestSnapshotResponseObject> refreshManifestSnapshot(@PathVariable("toolId") String toolId) {
        String username = UserContext.getCurrentUsername();
        return ApiResponseObject.success(toolAppService.refreshToolManifestSnapshot(username, toolId));
    }
}
