package com.example.student_management_system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClassDTO {
    private Long id;

    @NotBlank(message = "班级名称不能为空")
    private String name;

    @NotNull(message = "必须为班级关联一个专业")
    private Long majorId;

    // 用于前端显示
    private String majorName;
}