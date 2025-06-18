// Description: 课程服务类，负责课程目录和课程安排的管理
package com.example.student_management_system.dto;

import lombok.Data;

@Data
public class ApiResult<T> {

    private boolean success; // 是否成功
    private int code; // 状态码
    private String message; // 响应消息
    private T data; // 响应数据

    private ApiResult() {}

    public static <T> ApiResult<T> success(T data) {
        ApiResult<T> result = new ApiResult<>(); // 创建一个新的 ApiResult 实例
        result.setSuccess(true); // 设置成功标志
        result.setCode(200); // 设置状态码为 200
        result.setMessage("操作成功");  // 设置默认成功消息
        result.setData(data); // 设置响应数据
        return result;
    }

    public static <T> ApiResult<T> success() {
        return success(null);
    }

    public static <T> ApiResult<T> failure(int code, String message) {
        ApiResult<T> result = new ApiResult<>();
        result.setSuccess(false);
        result.setCode(code);
        result.setMessage(message);
        result.setData(null);
        return result;
    }

    /**
     * 【新增】一个可以携带错误数据的失败响应工厂方法
     * @param code 状态码
     * @param message 错误主消息
     * @param data 错误的详细数据 (例如，字段验证错误映射)
     * @return 统一的API结果
     */
    public static <T> ApiResult<T> failure(int code, String message, T data) {
        ApiResult<T> result = new ApiResult<>();
        result.setSuccess(false);
        result.setCode(code);
        result.setMessage(message);
        result.setData(data); // 将详细错误信息放入data字段
        return result;
    }
}