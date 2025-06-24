package com.example.student_management_system.model;

import lombok.Data;

@Data
public class Class {
    private Long id;
    private String name;
    private Long majorId;

    // 用于连接查询时显示专业名称
    private transient String majorName;
}