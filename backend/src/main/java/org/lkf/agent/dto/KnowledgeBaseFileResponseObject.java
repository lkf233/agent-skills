package org.lkf.agent.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "知识库文件响应对象")
public class KnowledgeBaseFileResponseObject {

    @Schema(description = "文件ID", example = "file_01HXYZ")
    private String id;

    @Schema(description = "文件名", example = "产品需求文档.md")
    private String fileName;

    @Schema(description = "文件类型", example = "text/markdown")
    private String mimeType;

    @Schema(description = "文件大小", example = "10240")
    private Long sizeBytes;

    @Schema(description = "解析状态", example = "READY")
    private String parseStatus;

    @Schema(description = "错误信息", example = "")
    private String errorMessage;

    public KnowledgeBaseFileResponseObject() {
    }

    public KnowledgeBaseFileResponseObject(String id, String fileName, String mimeType, Long sizeBytes, String parseStatus,
                                           String errorMessage) {
        this.id = id;
        this.fileName = fileName;
        this.mimeType = mimeType;
        this.sizeBytes = sizeBytes;
        this.parseStatus = parseStatus;
        this.errorMessage = errorMessage;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(Long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    public String getParseStatus() {
        return parseStatus;
    }

    public void setParseStatus(String parseStatus) {
        this.parseStatus = parseStatus;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
