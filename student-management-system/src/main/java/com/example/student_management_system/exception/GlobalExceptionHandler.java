package com.example.student_management_system.exception;

import com.example.student_management_system.dto.ApiResult;
import com.example.student_management_system.service.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResult<?> handleResourceNotFoundException(ResourceNotFoundException e) {
        log.warn("资源未找到: {}", e.getMessage());
        return ApiResult.failure(404, e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.CONFLICT) // 409 Conflict, 通常用于数据冲突，如“名称已存在”
    public ApiResult<?> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("无效参数: {}", e.getMessage());
        return ApiResult.failure(409, e.getMessage());
    }

    /**
     * 【新增】处理业务逻辑中不允许的操作，如“必修课无法退课”。
     * @param e 异常对象
     * @return 包含具体错误信息的API结果
     */
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 400 Bad Request, 表示一个不合法的操作请求
    public ApiResult<?> handleIllegalStateException(IllegalStateException e) {
        log.warn("非法状态/操作: {}", e.getMessage());
        // 直接将业务层抛出的具体错误信息返回给前端
        return ApiResult.failure(400, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResult<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.warn("参数校验失败: {}", errors);
        return ApiResult.failure(400, "提交的数据有误，请检查所有字段。", errors);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResult<?> handleGenericException(Exception e) {
        log.error("发生未知服务器内部错误", e);
        return ApiResult.failure(500, "服务器开小差了，请稍后再试~");
    }
}