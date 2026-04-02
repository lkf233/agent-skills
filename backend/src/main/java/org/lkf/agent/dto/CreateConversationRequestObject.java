package org.lkf.agent.dto;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 创建会话请求对象。
 */
@Schema(description = "创建会话请求对象")
public class CreateConversationRequestObject {

    /**
     * 会话标题。
     */
    @NotBlank(message = "会话标题不能为空")
    @Schema(description = "会话标题", example = "产品需求讨论")
    private String title;

    @NotBlank(message = "agentId不能为空")
    @Schema(description = "Agent ID", example = "agent_01HXYZ")
    private String agentId;

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

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }
}
