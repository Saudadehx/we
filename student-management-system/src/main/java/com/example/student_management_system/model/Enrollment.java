package com.example.student_management_system.model;

import lombok.Data;

@Data
public class Enrollment {
    private Long id;
    private Long studentId;
    private Long courseOfferingId; // <-- 修改此字段
    private Double score;
}