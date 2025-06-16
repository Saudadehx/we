package com.example.student_management_system.dto;

import lombok.Data;

@Data
public class CourseDTO {
    private String courseId;
    private String courseName;
    private Double credits;
    private String teacherId; // 此处使用教师的工号，更便于前端操作

    private Integer courseDay;
    private Integer courseTime;
}