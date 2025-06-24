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
    // --- 新增字段 ---
    private Integer capacity; // 课程容量
    private Long classroomId; // 关联的教室ID

    // --- 用于DTO和业务逻辑的临时字段 ---
    private transient String courseName;
    private transient String courseCode;
    private transient Double credits;
    private transient String teacherName;
    private transient List<ClassInfo> associatedClasses;
    // --- 新增的临时字段 ---
    private transient String classroomName; // 教室名称
    private transient int currentEnrollment; // 当前已选人数 (由Service层填充)

    @Data
    public static class ClassInfo {
        private Long classId;
        private String className;
        private String courseType; // COMPULSORY or ELECTIVE
    }
}