package com.example.student_management_system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;

/**
 * 统一的学生数据传输对象 (DTO)
 * 包含了用于创建、更新和响应的所有字段。
 */
@Data
public class StudentDTO {

    // --- 响应字段 ---
    private Long id;

    // --- 请求与响应共享字段 (带校验) ---
    @NotBlank(message = "姓名不能为空")
    @Size(min = 2, max = 50, message = "姓名长度必须在2到50之间")
    private String name;

    private String gender;

    @Past(message = "出生日期必须是过去的时间")
    private LocalDate dateOfBirth;

    private String ethnicity;
    private String nativePlace;
    private String politicalStatus;
    private String phoneNumber;
    private String email;

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

    // --- 关联信息 ---
    private Long majorId;
    private Integer academicYear;
    private Integer semester;

    // --- 仅用于响应的关联名称 ---
    private String majorName;

    // --- 仅用于请求的密码字段 ---
    // 在序列化为JSON（即响应给前端）时，此字段将被忽略。
    @com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
    private String password;
}