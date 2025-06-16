package com.example.student_management_system.model;

import lombok.Data;

@Data
public class Course {
    private Long id;
    private String courseId;
    private String courseName;
    private Double credits;
    private Long teacherId; //教师的数据库ID，而不是工号
    // 【新增】上课日 (1-7 代表周一到周日)
    private Integer courseDay;
    // 【新增】上课时间段 (例如 1-5 代表 1-5大节)
    private Integer courseTime;

}