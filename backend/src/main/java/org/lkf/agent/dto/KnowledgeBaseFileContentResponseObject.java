package org.lkf.agent.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "知识库文件内容响应对象")
public class KnowledgeBaseFileContentResponseObject {

    @Schema(description = "文件ID")
    private String fileId;

    @Schema(description = "文件名")
    private String fileName;

    @Schema(description = "文件类型")
    private String mimeType;

    @Schema(description = "文件内容")
    private String content;

    public KnowledgeBaseFileContentResponseObject() {
    }

    public KnowledgeBaseFileContentResponseObject(String fileId, String fileName, String mimeType, String content) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.mimeType = mimeType;
        this.content = content;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
