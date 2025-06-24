package com.example.student_management_system.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class Student implements UserDetails {

    private Long id;
    private String password;
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
    private String phoneNumber;
    private String email;
    @NotBlank(message = "学号不能为空")
    @Size(min = 4, max = 20, message = "学号长度必须在4到20之间")
    private String studentId;
    private String college;

    // ✨【修改】不再是数据库的直接字段，而是用于接收JOIN查询结果
    private String className;

    private LocalDate enrollmentDate;
    private String studentStatus;
    private Double gpa;
    private String photoUrl;

    private Long majorId;
    private Long classId; // ✨【新增】用于存储班级外键

    private Integer academicYear;
    private Integer semester;

    // 用于连接查询时显示专业名称
    private transient String majorName;


    // --- 构造函数 ---
    public Student() {
    }

    public Student(String studentId, String name, String gender, LocalDate dateOfBirth) {
        this.studentId = studentId;
        this.name = name;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
    }

    // --- 手动实现的 Getters 和 Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public String getEthnicity() { return ethnicity; }
    public void setEthnicity(String ethnicity) { this.ethnicity = ethnicity; }
    public String getNativePlace() { return nativePlace; }
    public void setNativePlace(String nativePlace) { this.nativePlace = nativePlace; }
    public String getPoliticalStatus() { return politicalStatus; }
    public void setPoliticalStatus(String politicalStatus) { this.politicalStatus = politicalStatus; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getCollege() { return college; }
    public void setCollege(String college) { this.college = college; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public LocalDate getEnrollmentDate() { return enrollmentDate; }
    public void setEnrollmentDate(LocalDate enrollmentDate) { this.enrollmentDate = enrollmentDate; }
    public String getStudentStatus() { return studentStatus; }
    public void setStudentStatus(String studentStatus) { this.studentStatus = studentStatus; }
    public Double getGpa() { return gpa; }
    public void setGpa(Double gpa) { this.gpa = gpa; }
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public Long getMajorId() { return majorId; }
    public void setMajorId(Long majorId) { this.majorId = majorId; }
    public Integer getAcademicYear() { return academicYear; }
    public void setAcademicYear(Integer academicYear) { this.academicYear = academicYear; }
    public Integer getSemester() { return semester; }
    public void setSemester(Integer semester) { this.semester = semester; }

    // ✨【新增】classId的Getter和Setter
    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }

    // ✨【新增】majorName的Getter和Setter
    public String getMajorName() { return majorName; }
    public void setMajorName(String majorName) { this.majorName = majorName; }

    // --- UserDetails 接口实现 ---
    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_STUDENT"));
    }

    @Override
    public String getPassword() { return this.password; }
    public void setPassword(String password) { this.password = password; }

    @Override
    public String getUsername() { return this.studentId; }

    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return true; }

    // --- equals, hashCode, toString ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return Objects.equals(id, student.id) && Objects.equals(studentId, student.studentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, studentId);
    }

    @Override
    public String toString() {
        return "Student{" + "id=" + id + ", name='" + name + '\'' + ", studentId='" + studentId + '\'' + '}';
    }
}