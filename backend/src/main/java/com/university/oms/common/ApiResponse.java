package com.university.oms.common;

/**
 * 统一API响应封装类，包含操作状态、消息和数据
 */
public class ApiResponse<T> {
    /** 请求是否成功 */
    private boolean success;
    /** 响应消息 */
    private String message;
    /** 响应数据 */
    private T data;

    public ApiResponse() {
    }

    /** 私有构造方法，通过静态工厂方法创建实例 */
    private ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    /** 构建成功响应 */
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<T>(true, "ok", data);
    }

    /** 构建失败响应 */
    public static <T> ApiResponse<T> fail(String message) {
        return new ApiResponse<T>(false, message, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
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
