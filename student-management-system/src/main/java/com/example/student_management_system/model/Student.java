package com.example.student_management_system.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import java.time.LocalDate;

@Entity
@Table(name = "students") // 定义数据库中的表名
public class Student{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 主键自增
    private Long id;

    @NotBlank(message = "学号不能为空")
    @Size(min = 4, max = 20, message = "学号长度必须在4到20之间")
    @Column(name = "student_id", unique = true, nullable = false) // 定义列名，唯一且不能为空
    private String studentId;

    @NotBlank(message = "姓名不能为空")
    @Size(min = 2, max = 50, message = "姓名长度必须在2到50之间")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "性别不能为空")
    @Column(nullable = false)
    private String gender; // 例如 "男", "女", "其他"

    @NotNull(message = "出生日期不能为空")
    @Past(message = "出生日期必须是过去的时间")
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @NotBlank(message = "班级名称不能为空")
    @Size(max = 100, message = "班级名称长度不能超过100")
    @Column(name = "class_name", nullable = false)
    private String className;

    @Size(max = 100, message = "专业列表长度不能超过100")
    @Column(name = "majors")
    private String major;

    @Column(name = "gpa")
    private Double gpa;

    @Column(name = "photo_url")
    private String photoUrl;

    // 一对多关系：一个学生可以有多条成绩记录
    // CascadeType.ALL: 当我们保存/删除一个学生时，与他关联的成绩也会被一并处理
    // orphanRemoval = true: 如果从这个学生的成绩集合中移除一条成绩，那条成绩记录将从数据库中被删除
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Grade> grades = new HashSet<>();


    public Student(String studentId, String name, String gender, LocalDate dateOfBirth, String className) {
        this.studentId = studentId;
        this.name = name;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.className = className;
    }

    public Student() {

    }

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
}
