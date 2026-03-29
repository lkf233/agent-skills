package org.lkf.agent.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "通用响应对象")
public class ApiResponseObject<T> {

    @Schema(description = "业务码，0表示成功", example = "0")
    private int code;
    @Schema(description = "响应消息", example = "成功")
    private String message;
    @Schema(description = "响应数据")
    private T data;

    public ApiResponseObject() {
    }

    public ApiResponseObject(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponseObject<T> success(T data) {
        return new ApiResponseObject<>(0, "成功", data);
    }

    public static <T> ApiResponseObject<T> fail(int code, String message) {
        return new ApiResponseObject<>(code, message, null);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
