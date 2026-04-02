package org.lkf.agent.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 会话响应对象。
 */
@Schema(description = "会话响应对象")
public class ConversationResponseObject {

    /**
     * 会话ID。
     */
    @Schema(description = "会话ID", example = "conv_01HXYZ")
    private String id;

    /**
     * 会话标题。
     */
    @Schema(description = "会话标题", example = "产品需求讨论")
    private String title;

    /**
     * 关联Agent ID。
     */
    @Schema(description = "关联Agent ID", example = "agent_01HXYZ")
    private String agentId;

    /**
     * 默认构造器。
     */
    public ConversationResponseObject() {
    }

    /**
     * 构造器。
     *
     * @param id 会话ID
     * @param title 会话标题
     * @param agentId 关联Agent ID
     */
    public ConversationResponseObject(String id, String title, String agentId) {
        this.id = id;
        this.title = title;
        this.agentId = agentId;
    }

    /**
     * 获取会话ID。
     *
     * @return 会话ID
     */
    public String getId() {
        return id;
    }

    /**
     * 设置会话ID。
     *
     * @param id 会话ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 获取会话标题。
     *
     * @return 会话标题
     */
    public String getTitle() {
        return title;
    }

    /**
     * 设置会话标题。
     *
     * @param title 会话标题
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * 获取关联Agent ID。
     *
     * @return 关联Agent ID
     */
    public String getAgentId() {
        return agentId;
    }

    /**
     * 设置关联Agent ID。
     *
     * @param agentId 关联Agent ID
     */
    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }
}
