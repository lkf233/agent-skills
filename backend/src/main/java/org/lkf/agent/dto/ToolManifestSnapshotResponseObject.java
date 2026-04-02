package org.lkf.agent.dto;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "工具Manifest快照响应对象")
public class ToolManifestSnapshotResponseObject {

    @Schema(description = "工具ID", example = "tool_01HXYZ")
    private String toolId;

    @Schema(description = "快照状态", example = "READY")
    private String status;

    @Schema(description = "错误信息", example = "")
    private String errorMessage;

    @Schema(description = "拉取时间")
    private LocalDateTime fetchedAt;

    @Schema(description = "过期时间")
    private LocalDateTime expireAt;

    @Schema(description = "tools/list快照内容")
    private JsonNode manifest;

    public ToolManifestSnapshotResponseObject() {
    }

    public ToolManifestSnapshotResponseObject(String toolId, String status, String errorMessage, LocalDateTime fetchedAt,
                                              LocalDateTime expireAt, JsonNode manifest) {
        this.toolId = toolId;
        this.status = status;
        this.errorMessage = errorMessage;
        this.fetchedAt = fetchedAt;
        this.expireAt = expireAt;
        this.manifest = manifest;
    }

    public String getToolId() {
        return toolId;
    }

    public void setToolId(String toolId) {
        this.toolId = toolId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getFetchedAt() {
        return fetchedAt;
    }

    public void setFetchedAt(LocalDateTime fetchedAt) {
        this.fetchedAt = fetchedAt;
    }

    public LocalDateTime getExpireAt() {
        return expireAt;
    }

    public void setExpireAt(LocalDateTime expireAt) {
        this.expireAt = expireAt;
    }

    public JsonNode getManifest() {
        return manifest;
    }

    public void setManifest(JsonNode manifest) {
        this.manifest = manifest;
    }
}
