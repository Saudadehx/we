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
    // --- 新增字段 ---
    private String courseType; // "COMPULSORY" (必修) 或 "ELECTIVE" (选修)
    private Long majorId;      // 关联的专业ID，选修课此项可为null
    private Integer academicYear; // 课程所属学年，如 1, 2, 3, 4
    private Integer semester;     // 课程所属学期，如 1 (上学期), 2 (下学期)

}