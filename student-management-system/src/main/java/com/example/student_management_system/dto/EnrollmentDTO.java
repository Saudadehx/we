package com.example.student_management_system.dto;

import lombok.Data;

@Data
public class EnrollmentDTO {
    private String studentId; // 学生学号
    private String courseId; // 课程编号
}