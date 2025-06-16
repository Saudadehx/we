package com.example.student_management_system.dto;

import lombok.Data;

@Data
public class CourseResponseDTO {
    private Long id;
    private String courseId;
    private String courseName;
    private Double credits;
    private String teacherName; // 我们希望返回教师姓名
    private String teacherId;   // 【新增】返回教师工号，用于编辑时回显
    private Integer courseDay;
    private Integer courseTime;
}