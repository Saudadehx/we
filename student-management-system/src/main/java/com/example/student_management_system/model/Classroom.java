package com.example.student_management_system.model;

import lombok.Data;

@Data
public class Classroom {
    private Long id;
    private String name; // 例如: "教A-101", "计算机实验中心2号机房"
    private String type; // 例如: "标准教室", "计算机实验室", "阶梯教室"
    private int capacity; // 教室容量
}