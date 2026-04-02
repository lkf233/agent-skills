package org.lkf.agent.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "会话消息响应对象")
public class ConversationMessageResponseObject {

    @Schema(description = "消息ID", example = "msg_01HXYZ")
    private String id;

    @Schema(description = "消息角色", example = "USER")
    private String role;

    @Schema(description = "消息内容", example = "请帮我总结今天讨论重点")
    private String content;

    @Schema(description = "消息序号", example = "12")
    private Integer seqNo;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    public ConversationMessageResponseObject() {
    }

    public ConversationMessageResponseObject(String id, String role, String content, Integer seqNo, LocalDateTime createdAt) {
        this.id = id;
        this.role = role;
        this.content = content;
        this.seqNo = seqNo;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getSeqNo() {
        return seqNo;
    }

    public void setSeqNo(Integer seqNo) {
        this.seqNo = seqNo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
