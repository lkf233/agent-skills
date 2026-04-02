package org.lkf.agent.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "发送消息请求对象")
public class SendMessageRequestObject {

    @NotBlank(message = "消息内容不能为空")
    @Schema(description = "消息内容", example = "请帮我总结今天讨论重点")
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
