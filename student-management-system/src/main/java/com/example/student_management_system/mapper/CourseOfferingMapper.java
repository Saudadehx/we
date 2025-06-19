package com.example.student_management_system.mapper;

import com.example.student_management_system.model.CourseOffering;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface CourseOfferingMapper {
    CourseOffering findById(Long id);
    List<CourseOffering> findAllWithDetails();
    // 新增方法：根据教师ID查找其所有课程安排
    List<CourseOffering> findOfferingsByTeacherId(@Param("teacherId") Long teacherId);
    void insert(CourseOffering offering);
    int update(CourseOffering offering);
    int countByCourseCatalogId(@Param("courseCatalogId") Long courseCatalogId);
    void deleteById(Long id);
    List<CourseOffering> findOfferingsByTeacherAndTimetable(
            @Param("teacherId") Long teacherId,
            @Param("academicYear") int academicYear,
            @Param("semester") int semester,
            @Param("courseDay") int courseDay,
            @Param("courseTime") int courseTime,
            @Param("excludeOfferingId") Long excludeOfferingId
    );
    // 新增方法：解除教师与课程安排的关联
    void disassociateTeacherFromOfferings(@Param("teacherId") Long teacherId);

    /**
     * 【新增方法】根据课程目录ID查找所有课程安排的ID列表。
     * 这是实现级联删除的关键步骤。
     * @param courseCatalogId 课程目录ID
     * @return 相关的课程安排ID列表
     */
    List<Long> findOfferingIdsByCatalogId(@Param("courseCatalogId") Long courseCatalogId);
}