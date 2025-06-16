package com.example.student_management_system.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Data;
// 这个类定义了前端在创建新学生时，必须提供的数据和其验证规则
@Data
public class StudentUpdateDTO {

    @NotBlank(message = "学号不能为空")
    @Size(min = 4, max = 20, message = "学号长度必须在4到20之间")
    private String studentId;

    @NotBlank(message = "姓名不能为空")
    @Size(min = 2, max = 50, message = "姓名长度必须在2到50之间")
    private String name;

    @NotBlank(message = "性别不能为空")
    private String gender;

    @NotNull(message = "出生日期不能为空")
    @Past(message = "出生日期必须是过去的时间")
    private LocalDate dateOfBirth;

    @NotBlank(message = "班级名称不能为空")
    @Size(max = 100, message = "班级名称长度不能超过100")
    private String className;

    @Size(max = 100, message = "专业列表长度不能超过100")
    private String major;

    private Double gpa;
    private String photoUrl;

    // --- Getters and Setters ---
    // ... (省略，与上面类似)
}