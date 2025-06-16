package com.example.student_management_system.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data // 使用Lombok简化代码
public class Grade {

    private Long id;
    private String subjectName; // 科目名称
    private Double score; // 分数

    @JsonIgnore // 【核心】在序列化为JSON时，忽略掉student对象
    private Student student;
}