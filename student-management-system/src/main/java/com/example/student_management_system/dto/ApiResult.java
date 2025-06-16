package com.example.student_management_system.dto;

import lombok.Data;

// 使用泛型，T 代表任何类型的数据
@Data
public class ApiResult<T> {

    private boolean success; // 操作是否成功
    private int code;        // 业务状态码 (例如 200, 404, 500)
    private String message;  // 返回的消息
    private T data;          // 返回的数据

    // 私有化构造函数，强制使用静态工厂方法创建实例
    private ApiResult() {}

    // 静态工厂方法：用于创建成功的响应
    public static <T> ApiResult<T> success(T data) {
        ApiResult<T> result = new ApiResult<>();
        result.setSuccess(true);
        result.setCode(200); // 约定200为成功
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    // 重载一个不带数据的成功响应
    public static <T> ApiResult<T> success() {
        return success(null);
    }

    // 静态工厂方法：用于创建失败的响应
    public static <T> ApiResult<T> failure(int code, String message) {
        ApiResult<T> result = new ApiResult<>();
        result.setSuccess(false);
        result.setCode(code);
        result.setMessage(message);
        result.setData(null); // 失败时数据为null
        return result;
    }
}