package com.example.student_management_system.dto;

import lombok.Data;

@Data
public class StudentProfileUpdateDTO {
    // 学生只能修改自己的密码、电话和邮箱
    private String password;
    private String phoneNumber;
    private String email;
}