package org.lkf.agent.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.lkf.agent.common.context.UserContext;
import org.lkf.agent.common.dto.ApiResponseObject;
import org.lkf.agent.dto.CreateKnowledgeBaseRequestObject;
import org.lkf.agent.dto.KnowledgeBaseFileResponseObject;
import org.lkf.agent.dto.KnowledgeBaseFileContentResponseObject;
import org.lkf.agent.dto.KnowledgeBaseResponseObject;
import org.lkf.agent.dto.KnowledgeBaseFilePageResponseObject;
import org.lkf.agent.service.KnowledgeBaseAppService;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 知识库控制器。
 */
@RestController
@Validated
@RequestMapping("/api/knowledge-bases")
@Tag(name = "知识库接口")
@SecurityRequirement(name = "BearerAuth")
public class KnowledgeBaseController {

    /**
     * 知识库应用服务。
     */
    private final KnowledgeBaseAppService knowledgeBaseAppService;

    /**
     * 构造器。
     *
     * @param knowledgeBaseAppService 知识库应用服务
     */
    public KnowledgeBaseController(KnowledgeBaseAppService knowledgeBaseAppService) {
        this.knowledgeBaseAppService = knowledgeBaseAppService;
    }

    /**
     * 创建知识库接口。
     *
     * @param requestObject 创建知识库请求对象
     * @return 知识库响应对象
     */
    @PostMapping
    @Operation(summary = "创建知识库", description = "为当前登录用户创建知识库")
    public ApiResponseObject<KnowledgeBaseResponseObject> create(@Valid @RequestBody CreateKnowledgeBaseRequestObject requestObject) {
        String username = UserContext.getCurrentUsername();
        return ApiResponseObject.success(knowledgeBaseAppService.createKnowledgeBase(username, requestObject));
    }

    /**
     * 查询知识库列表接口。
     *
     * @return 知识库响应对象列表
     */
    @GetMapping
    @Operation(summary = "查询知识库列表", description = "查询当前登录用户的知识库列表")
    public ApiResponseObject<List<KnowledgeBaseResponseObject>> list() {
        String username = UserContext.getCurrentUsername();
        return ApiResponseObject.success(knowledgeBaseAppService.listKnowledgeBases(username));
    }

    @PostMapping("/{kbId}/files")
    @Operation(summary = "上传知识库文件", description = "上传知识库文件并触发解析流程")
    public ApiResponseObject<KnowledgeBaseFileResponseObject> uploadFile(@PathVariable("kbId") String kbId,
                                                                         @RequestParam("file") MultipartFile file) {
        String username = UserContext.getCurrentUsername();
        return ApiResponseObject.success(knowledgeBaseAppService.uploadFile(username, kbId, file));
    }

    @GetMapping("/{kbId}/files")
    @Operation(summary = "查询知识库文件列表", description = "查询知识库文件上传与解析状态")
    public ApiResponseObject<KnowledgeBaseFilePageResponseObject> listFiles(@PathVariable("kbId") String kbId,
                                                                             @RequestParam(value = "parseStatus", required = false) String parseStatus,
                                                                             @RequestParam(value = "fileName", required = false) String fileName,
                                                                             @RequestParam(value = "sortBy", required = false) String sortBy,
                                                                             @RequestParam(value = "sortOrder", required = false) String sortOrder,
                                                                             @RequestParam(value = "page", required = false, defaultValue = "1") @Min(value = 1, message = "page必须大于等于1") Integer page,
                                                                             @RequestParam(value = "pageSize", required = false, defaultValue = "10") @Min(value = 1, message = "pageSize必须大于等于1") Integer pageSize) {
        String username = UserContext.getCurrentUsername();
        return ApiResponseObject.success(knowledgeBaseAppService.listFiles(
                username, kbId, parseStatus, fileName, sortBy, sortOrder, page, pageSize
        ));
    }

    @GetMapping("/{kbId}/files/{fileId}/content")
    @Operation(summary = "查询知识库文件内容", description = "在线预览txt和markdown文件内容")
    public ApiResponseObject<KnowledgeBaseFileContentResponseObject> getFileContent(@PathVariable("kbId") String kbId,
                                                                                     @PathVariable("fileId") String fileId) {
        String username = UserContext.getCurrentUsername();
        return ApiResponseObject.success(knowledgeBaseAppService.getFileContent(username, kbId, fileId));
    }
}
