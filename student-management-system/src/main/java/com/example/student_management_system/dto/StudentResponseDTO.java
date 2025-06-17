package com.example.student_management_system.dto;

import java.time.LocalDate;

public class StudentResponseDTO {
    private Long id;
    private String name;
    private String gender;
    private LocalDate dateOfBirth;
    private String ethnicity;
    private String nativePlace;
    private String politicalStatus;
    private String phoneNumber;
    private String email;
    private String studentId;
    private String college;
    private String className;
    private LocalDate enrollmentDate;
    private String studentStatus;
    private Double gpa;
    private String photoUrl;
    private Long majorId;
    private Integer academicYear;
    private String majorName;

    // 【新增】
    private Integer semester;

    // Getters and Setters
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
    public String getMajorName() { return majorName; }
    public void setMajorName(String majorName) { this.majorName = majorName; }

    // 【新增】
    public Integer getSemester() { return semester; }
    public void setSemester(Integer semester) { this.semester = semester; }
}