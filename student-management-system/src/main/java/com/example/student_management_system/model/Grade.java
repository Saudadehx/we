package com.example.student_management_system.model;

import com.fasterxml.jackson.annotation.JsonIgnore;


public class Grade {

    private Long id;
    private String subjectName; // 科目名称
    private Double score; // 分数

    @JsonIgnore
    private Student student;

}