package com.example.student_management_system.model;

import lombok.Data;

@Data
public class SystemSetting {
    private Integer id;
    private String settingKey;
    private String settingValue;
    private String description;
}