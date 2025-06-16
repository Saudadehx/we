package com.example.student_management_system.model;

import lombok.Data;

@Data
public class Course {
    private Long id;
    private String courseId;
    private String courseName;
    private Double credits;
    private Long teacherId; // 注意这里是教师的数据库ID，而不是工号
}