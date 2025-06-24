package com.example.student_management_system.model;

import lombok.Data;

@Data
public class CourseCatalog {
    private Long id;
    private String courseCode;
    private String name;
    private Double credits;
    private String requiredClassroomType;
    private Integer lessonsPerWeek; // 【代码新增】
}