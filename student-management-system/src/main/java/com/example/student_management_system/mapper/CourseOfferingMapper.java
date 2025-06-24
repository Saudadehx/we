package com.example.student_management_system.mapper;

import com.example.student_management_system.model.CourseOffering;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface CourseOfferingMapper {
    CourseOffering findById(Long id);
    List<CourseOffering> findAllWithDetails();
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

    List<CourseOffering> findOfferingsByClassroomAndTimetable(
            @Param("classroomId") Long classroomId,
            @Param("academicYear") int academicYear,
            @Param("semester") int semester,
            @Param("courseDay") int courseDay,
            @Param("courseTime") int courseTime,
            @Param("excludeOfferingId") Long excludeOfferingId
    );

    void disassociateTeacherFromOfferings(@Param("teacherId") Long teacherId);

    List<Long> findOfferingIdsByCatalogId(@Param("courseCatalogId") Long courseCatalogId);

    List<CourseOffering> findByCourseCatalogId(@Param("courseCatalogId") Long courseCatalogId);

    /**
     * 【代码新增】专用于更新排课结果的精准方法
     * @param id 要更新的课程安排ID
     * @param courseDay 新的星期几
     * @param courseTime 新的时间段
     * @param classroomId 新的教室ID
     */
    void updateSchedule(
            @Param("id") Long id,
            @Param("courseDay") Integer courseDay,
            @Param("courseTime") Integer courseTime,
            @Param("classroomId") Long classroomId
    );
}