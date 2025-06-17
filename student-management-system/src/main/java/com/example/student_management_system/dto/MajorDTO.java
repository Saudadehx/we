package com.example.student_management_system.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class MajorDTO {
    private Long id;

    @NotBlank(message = "专业名称不能为空")
    private String name;
    private String description;
}