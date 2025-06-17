package com.example.student_management_system.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CourseCatalogDTO {
    private Long id;

    @NotBlank(message = "课程代码不能为空")
    private String courseCode;

    @NotBlank(message = "课程名称不能为空")
    private String name;

    @NotNull(message = "学分不能为空")
    @DecimalMin(value = "0.5", message = "学分必须大于或等于0.5")
    private Double credits;
}