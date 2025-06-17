package com.example.student_management_system.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true) // 确保继承的字段也被包含在 equals 和 hashCode 中
public class TeacherDetailDTO extends TeacherDTO {
    private List<String> taughtCourses; // 只包含课程名称列表，保持DTO轻量
}