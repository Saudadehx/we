package com.example.student_management_system.model;

import lombok.Data;

/**
 * 【新增】课程安排与班级的关联实体
 * 用于表示一个课程安排是为哪个班级设置的，以及是必修还是选修。
 */
@Data
public class OfferingClassLink {
    private Long id;
    private Long courseOfferingId;
    private Long classId; // 【核心修改】从 majorId 变更为 classId
    private String courseType; // 课程类型: COMPULSORY (必修) or ELECTIVE (选修)
}