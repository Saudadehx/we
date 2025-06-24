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

    // --- 【修改】用于DTO和业务逻辑的临时字段 ---
    private transient String courseName;
    private transient String courseCode;
    private transient Double credits;
    private transient String teacherName;
    // 【修改】从 MajorInfo 列表变更为 ClassInfo 列表
    private transient List<ClassInfo> associatedClasses;

    /**
     * 【新增】内部类，用于承载关联的班级信息
     */
    @Data
    public static class ClassInfo {
        private Long classId;
        private String className;
        private String courseType; // COMPULSORY or ELECTIVE
    }
}