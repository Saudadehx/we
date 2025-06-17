package com.example.student_management_system.model;

import lombok.Data;

@Data
public class OfferingMajorLink {
    private Long id;
    private Long courseOfferingId;
    private Long majorId;
    private String courseType;
}