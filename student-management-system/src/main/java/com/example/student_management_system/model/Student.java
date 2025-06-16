package com.example.student_management_system.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;
import java.time.LocalDate;
import lombok.Data;

@Data
public class Student{
    private Long id;
    //账户信息
    private String password;

    @NotBlank(message = "姓名不能为空")
    @Size(min = 2, max = 50, message = "姓名长度必须在2到50之间")
    private String name;

    @NotBlank(message = "性别不能为空")
    private String gender; // 例如 "男", "女", "其他"

    @NotNull(message = "出生日期不能为空")
    @Past(message = "出生日期必须是过去的时间")
    private LocalDate dateOfBirth;

    private String ethnicity; // 民族
    private String nativePlace; // 籍贯
    private String politicalStatus; // 政治面貌

    // --- 联系方式 ---
    private String phoneNumber; // 手机号码
    private String email; // 电子邮箱

    @NotBlank(message = "学号不能为空")
    @Size(min = 4, max = 20, message = "学号长度必须在4到20之间")
    private String studentId;
    private String college;

    @NotBlank(message = "班级名称不能为空")
    @Size(max = 100, message = "班级名称长度不能超过100")
    private String className;

    @Size(max = 100, message = "专业列表长度不能超过100")
    private String major;

    private LocalDate enrollmentDate; // 入学日期
    private String studentStatus;

    private Double gpa;
    private String photoUrl;

    private Set<Grade> grades = new HashSet<>();

    public Student(String studentId, String name, String gender, LocalDate dateOfBirth, String className) {
        this.studentId = studentId;
        this.name = name;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.className = className;
    }

    public Student() {}

    // Getter 和 Setter 方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
    // ... (在现有的 getClassName() 和 setClassName() 方法后面添加)
    public String getMajor() {return major;}

    public void setMajor(String major) {this.major = major;}

    public Double getGpa() {return gpa;}

    public void setGpa(Double gpa) {this.gpa = gpa;}

    public String getPhotoUrl() {return photoUrl;}

    public void setPhotoUrl(String photoUrl) {this.photoUrl = photoUrl;}


    // (可选) toString, equals, hashCode 方法
    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", studentId='" + studentId + '\'' +
                ", name='" + name + '\'' +
                ", gender='" + gender + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", className='" + className + '\'' +
                '}';
    }

    public String getEthnicity() {return ethnicity;}

    public String getNativePlace() {return nativePlace;}

    public String getPoliticalStatus() {return politicalStatus;}

    public String getPhoneNumber() {return phoneNumber;}

    public String getEmail() {return email;}

    public String getCollege() {return college;}

    public LocalDate getEnrollmentDate() {return enrollmentDate;}

    public String getStudentStatus() {return studentStatus;}

    public void setPassword(String encode) {
    }
}
