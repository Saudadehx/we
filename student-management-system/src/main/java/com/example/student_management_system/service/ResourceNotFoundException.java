package com.example.student_management_system.service;

// 简单的自定义异常类，实际项目中可能会有更完善的异常处理机制
// Making this a public class so it can be potentially accessed by other services if needed,
// or easily refactored into its own file later.
// For now, it remains in this file for simplicity of the current script.
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
