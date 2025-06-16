// CourseResponseDTO.java
package com.example.student_management_system.dto;

import lombok.Data;

@Data
public class CourseResponseDTO {
    private Long id;
    private String courseId;
    private String courseName;
    private Double credits;
    private String teacherName; // 我们希望返回教师姓名，而不是ID
}
