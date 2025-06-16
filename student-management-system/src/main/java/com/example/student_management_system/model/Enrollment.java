package com.example.student_management_system.model;

import lombok.Data;

@Data
public class Enrollment {
    private Long id;
    private Long studentId;
    private Long courseId;
    private Double score;
}