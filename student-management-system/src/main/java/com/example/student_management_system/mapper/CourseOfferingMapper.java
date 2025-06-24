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

    /**
     * 【新增的方法声明】
     * 根据教室和时间安排查找课程，用于冲突检测。
     * 这就是解决您编译错误的关键所在。
     */
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
}