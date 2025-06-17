package com.example.student_management_system.dto;

import lombok.Data;

@Data
public class StudentProfileUpdateDTO {
    // 账户信息
    private String password;

    // 联系方式
    private String phoneNumber;
    private String email;

    // 【新增】允许学生修改的个人信息
    private String ethnicity;
    private String nativePlace;
    private String politicalStatus;
}