package com.example.student_management_system.dto;

import lombok.Data;

@Data
public class EnrollmentResponseDTO {
    private Long enrollmentId; // 选课记录ID
    private String studentName;
    private String studentId; // 学号
    private String courseId; // <-- 新增
    private Double credits;
    private String courseName;
    private Double score;
}