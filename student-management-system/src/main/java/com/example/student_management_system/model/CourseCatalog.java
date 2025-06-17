package com.example.student_management_system.model;

import lombok.Data;

@Data
public class CourseCatalog {
    private Long id;
    private String courseCode;
    private String name;
    private Double credits;
}