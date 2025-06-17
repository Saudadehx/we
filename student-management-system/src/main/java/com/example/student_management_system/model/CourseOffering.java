package com.example.student_management_system.model;

import lombok.Data;
import java.util.List;

@Data
public class CourseOffering {
    private Long id;
    private Long courseCatalogId;
    private Long teacherId;
    private Integer academicYear;
    private Integer semester;
    private Integer courseDay;
    private Integer courseTime;

    // --- 用于DTO和业务逻辑的临时字段 ---
    private transient String courseName;
    private transient String courseCode;
    private transient Double credits;
    private transient String teacherName;
    private transient List<MajorInfo> associatedMajors;

    @Data
    public static class MajorInfo {
        private Long majorId;
        private String majorName;
        private String courseType; // COMPULSORY or ELECTIVE
    }
}