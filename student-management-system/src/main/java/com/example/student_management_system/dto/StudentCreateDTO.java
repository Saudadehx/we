package com.example.student_management_system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Data;

@Data // 【关键】确保这个注解存在，它会自动生成所有字段的get/set方法
public class StudentCreateDTO {

    // --- 账户信息 ---
    private String password;

    // --- 基本信息 ---
    @NotBlank(message = "姓名不能为空")
    @Size(min = 2, max = 50, message = "姓名长度必须在2到50之间")
    private String name;

    @NotBlank(message = "性别不能为空")
    private String gender;

    @NotNull(message = "出生日期不能为空")
    @Past(message = "出生日期必须是过去的时间")
    private LocalDate dateOfBirth;

    private String ethnicity;
    private String nativePlace;
    private String politicalStatus;

    // --- 联系方式 ---
    private String phoneNumber;
    private String email;

    // --- 学籍信息 ---
    @NotBlank(message = "学号不能为空")
    @Size(min = 4, max = 20, message = "学号长度必须在4到20之间")
    private String studentId;

    private String college;

    @NotBlank(message = "班级名称不能为空")
    @Size(max = 100, message = "班级名称长度不能超过100")
    private String className;

    @Size(max = 100, message = "专业列表长度不能超过100")
    private String major;

    private LocalDate enrollmentDate;
    private String studentStatus;
    private Double gpa;
    private String photoUrl;
}