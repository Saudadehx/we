package com.example.student_management_system.mapper;

import com.example.student_management_system.model.CourseOffering;
import com.example.student_management_system.model.OfferingClassLink;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface OfferingClassLinkMapper {
    void insert(OfferingClassLink link);
    void deleteByOfferingId(Long offeringId);
    List<CourseOffering> findOfferingsByClassAndTimetable(
            @Param("classId") Long classId,
            @Param("academicYear") int academicYear,
            @Param("semester") int semester,
            @Param("courseDay") int courseDay,
            @Param("courseTime") int courseTime,
            @Param("excludeOfferingId") Long excludeOfferingId
    );

    /**
     * 【新增】根据班级ID和学年学期，查找所有必修课程。
     * @param classId 班级ID
     * @param academicYear 学年
     * @param semester 学期
     * @return 相关的课程安排列表
     */
    List<CourseOffering> findCompulsoryOfferingsForClass(
            @Param("classId") Long classId,
            @Param("academicYear") Integer academicYear,
            @Param("semester") Integer semester
    );
}