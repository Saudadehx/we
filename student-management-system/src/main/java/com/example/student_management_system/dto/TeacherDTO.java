package com.example.student_management_system.dto;

import lombok.Data;

@Data
public class TeacherDTO {
    // 【新增】ID字段，用于前后端交互
    private Long id;
    private String teacherId;
    private String name;
    private String password; // 只在创建和修改时使用
}