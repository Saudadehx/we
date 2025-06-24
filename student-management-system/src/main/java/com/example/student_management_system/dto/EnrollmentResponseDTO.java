package com.example.student_management_system.dto;

import lombok.Data;

@Data
public class EnrollmentResponseDTO {
    private Long enrollmentId;
    private String studentName;
    private String studentId;
    private String courseId;
    private Double credits;
    private String courseName;
    private Double score;
    private Integer courseDay;
    private Integer courseTime;
    private String teacherName;
    private Long courseOfferingId;
    private String classroomName; // 【代码新增】
}