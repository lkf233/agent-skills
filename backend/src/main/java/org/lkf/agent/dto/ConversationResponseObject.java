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
     * 默认构造器。
     */
    public ConversationResponseObject() {
    }

    /**
     * 构造器。
     *
     * @param id 会话ID
     * @param title 会话标题
     */
    public ConversationResponseObject(String id, String title) {
        this.id = id;
        this.title = title;
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
}
