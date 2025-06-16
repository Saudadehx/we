package com.example.student_management_system.dto;

import lombok.Data;

@Data
public class TeacherDTO {
    private String teacherId;
    private String name;
    private String password; // 只在创建时使用
}