// Description: 课程服务类，负责课程目录和课程安排的管理
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
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ... 其他 handle 方法保持不变 ...
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResult<?> handleResourceNotFoundException(ResourceNotFoundException e) {
        log.warn("资源未找到: {}", e.getMessage());
        return ApiResult.failure(404, e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResult<?> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("无效参数: {}", e.getMessage());
        return ApiResult.failure(409, e.getMessage());
    }


    /**
     * 【重大优化】处理 @Valid 注解校验失败的异常，返回结构化的错误信息
     * @param e 异常对象
     * @return 包含错误字段映射的API结果
     */
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

        // 返回一个通用的主消息，并将详细的字段错误放在 data 字段中
        return ApiResult.failure(400, "提交的数据有误，请检查所有字段。", errors);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResult<?> handleGenericException(Exception e) {
        log.error("发生未知服务器内部错误", e);
        return ApiResult.failure(500, "服务器开小差了，请稍后再试~");
    }
}