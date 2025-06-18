package com.example.student_management_system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * 这个类包含了所有可能从客户端接收到的用于创建或更新学生信息的字段。
 * Service 层将根据业务场景（如创建、管理员更新、学生更新个人资料）来决定使用哪些字段。
 */
@Data
public class StudentRequestDTO {

    // --- 账户信息 ---
    // 在创建时是必须的，在更新时是可选的
    private String password;

    // --- 基本信息 ---
    @NotBlank(message = "姓名不能为空")
    @Size(min = 2, max = 50, message = "姓名长度必须在2到50之间")
    private String name;

    private String gender;

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

    @Size(max = 100, message = "班级名称长度不能超过100")
    private String className;

    private LocalDate enrollmentDate;
    private String studentStatus;
    private Double gpa;
    private String photoUrl;

    private Long majorId;
    private Integer academicYear;
    private Integer semester;
}