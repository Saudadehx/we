package com.example.student_management_system.dto;

import lombok.Data;

@Data
public class EnrollmentResponseDTO {
    private Long enrollmentId; // 选课记录ID
    private String studentName;  // 学生姓名
    private String studentId; // 学号
    private String courseId; // 课程编号
    private Double credits; // 学分
    private String courseName;  // 课程名称
    private Double score;  // 分数
    private Integer courseDay;  // 上课日 (1-7 代表周一到周日)
    private Integer courseTime;  // 上课时间段 (例如 1-5 代表 1-5大节)
}