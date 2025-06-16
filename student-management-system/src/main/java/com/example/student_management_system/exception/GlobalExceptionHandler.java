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

import java.util.stream.Collectors;

@Slf4j // 使用 Lombok 的日志注解，方便打印日志
@RestControllerAdvice // 这个注解表示这是一个全局的Controller增强器，专门处理异常
public class GlobalExceptionHandler {

    // 处理我们自定义的“资源未找到”异常
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND) // 返回 404 状态码
    public ApiResult<?> handleResourceNotFoundException(ResourceNotFoundException e) {
        log.warn("资源未找到: {}", e.getMessage());
        return ApiResult.failure(404, e.getMessage());
    }

    // 处理因参数问题（如学号重复）抛出的异常
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.CONFLICT) // 返回 409 状态码，表示冲突
    public ApiResult<?> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("无效参数: {}", e.getMessage());
        return ApiResult.failure(409, e.getMessage());
    }

    // 处理 @Valid 注解校验失败的异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 返回 400 状态码
    public ApiResult<?> handleValidationExceptions(MethodArgumentNotValidException e) {
        // 将所有字段的错误信息拼接成一个字符串
        String message = e.getBindingResult().getAllErrors().stream()
                .map(error -> ((FieldError) error).getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败: {}", message);
        return ApiResult.failure(400, message);
    }

    // 最后的防线：处理所有其他未捕获的异常
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // 返回 500 状态码
    public ApiResult<?> handleGenericException(Exception e) {
        // 在生产环境中，不应将详细的异常堆栈暴露给用户
        log.error("发生未知服务器内部错误", e); // 使用 log.error 记录完整堆栈
        return ApiResult.failure(500, "服务器开小差了，请稍后再试~");
    }
}