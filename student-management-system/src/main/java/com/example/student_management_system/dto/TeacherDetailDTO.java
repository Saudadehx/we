package com.example.student_management_system.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class TeacherDetailDTO extends TeacherDTO {
    // ✨ 修改：将 List<String> 改为 String，以接收数据库 GROUP_CONCAT 的结果
    private String taughtCourses;
}